package org.jlab.btm.business.service.epics;

import com.cosylab.epics.caj.cas.CAJServerContext;
import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import gov.aps.jca.CAException;
import gov.aps.jca.configuration.ConfigurationException;
import gov.aps.jca.configuration.DefaultConfiguration;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CAJ's in-process Channel Access server with in-memory double PVs, bound to loopback on the given
 * ports. It stands in for an IOC in tests, so they contact no EPICS service outside the test JVM.
 */
final class SyntheticIoc implements AutoCloseable {
  private static final Duration STOP_WAIT = Duration.ofSeconds(30);

  private final DefaultServerImpl server = new DefaultServerImpl();
  private final Map<String, MemoryProcessVariable> pvs = new ConcurrentHashMap<>();
  private final CAJServerContext context;
  private final Thread thread;
  private boolean closed = false;

  SyntheticIoc(int serverPort, int beaconPort) throws CAException {
    DefaultConfiguration config = new DefaultConfiguration("test-ioc");
    config.setAttribute("server_port", Integer.toString(serverPort));
    config.setAttribute("beacon_addr_list", "127.0.0.1");
    config.setAttribute("auto_beacon_addr_list", "false");
    config.setAttribute("beacon_port", Integer.toString(beaconPort));

    // Configured before initialize(): JCALibrary.createServerContext() configures after it, when
    // the server has already bound the default port 5064.
    context = new CAJServerContext();

    try {
      context.configure(config);
    } catch (ConfigurationException e) {
      throw new CAException("Unable to configure synthetic IOC", e);
    }

    context.initialize(server);

    // The TCP acceptor falls back to a random port if the configured one is taken
    int tcpPort = context.getTcpServerPort();
    int udpPort = context.getUdpServerPort();

    if (tcpPort != serverPort || udpPort != serverPort) {
      context.destroy();
      throw new CAException(
          "Synthetic IOC bound TCP port "
              + tcpPort
              + " and UDP port "
              + udpPort
              + ", not "
              + serverPort);
    }

    thread =
        new Thread(
            () -> {
              try {
                context.run(0);
              } catch (CAException e) {
                throw new IllegalStateException(e);
              }
            },
            "synthetic-ioc");
    thread.setDaemon(true);
    thread.start();
  }

  /** A port free for both TCP and UDP on loopback, as a CA server binds both. */
  static int freePort() throws IOException {
    for (int attempt = 0; attempt < 20; attempt++) {
      int port;

      try (ServerSocket tcp = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
        port = tcp.getLocalPort();
      }

      try (DatagramSocket udp =
          new DatagramSocket(new InetSocketAddress(InetAddress.getLoopbackAddress(), port))) {
        return port;
      } catch (IOException e) {
        // Taken for UDP; try another
      }
    }

    throw new IOException("No free port");
  }

  void add(String name, double value) {
    pvs.put(name, server.createMemoryProcessVariable(name, DBRType.DOUBLE, new double[] {value}));
  }

  void write(String name, double value) throws CAException {
    pvs.get(name).write(new DBR_Double(new double[] {value}), null);
  }

  @Override
  public void close() {
    try {
      if (closed) {
        return;
      }

      closed = true;
      context.destroy();

      thread.join(STOP_WAIT.toMillis());
    } catch (CAException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
