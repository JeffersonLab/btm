package org.jlab.btm.business.service;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.*;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.jlog.Body;
import org.jlab.jlog.Library;
import org.jlab.jlog.LogEntry;
import org.jlab.jlog.LogEntryAdminExtension;
import org.jlab.jlog.exception.LogCertificateException;
import org.jlab.jlog.exception.LogIOException;
import org.jlab.smoothness.persistence.enumeration.Hall;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LogbookService {

  private static final Logger logger = Logger.getLogger(LogbookService.class.getName());

  @Resource private SessionContext context;

  @PermitAll
  public String checkAuthorized() {
    String username = context.getCallerPrincipal().getName();
    if (username == null || username.isEmpty() || username.equalsIgnoreCase("ANONYMOUS")) {
      throw new EJBAccessException(
          "You must be authenticated to perform the requested operation.  Your session may have expired.  Please re-login.");
    }

    return username;
  }

  @PermitAll
  public long createCrewChiefTimesheetEntry(String subject, String html)
      throws IOException, LogIOException, LogCertificateException {

    String username = checkAuthorized(); /*Must be logged in*/

    String logbookServerUrl = getLogbookServerUrl();
    String logbooks = crewChiefLogbook();
    String tags = getTags();

    Properties config = Library.getConfiguration();

    config.setProperty("SUBMIT_URL", logbookServerUrl + "/incoming");

    logger.log(Level.INFO, "Sending elog to logbook: {0}", logbooks);

    LogEntry entry = new LogEntry(subject, logbooks);

    entry.setBody(html, Body.ContentType.HTML);

    entry.setTags(tags);

    LogEntryAdminExtension extension = new LogEntryAdminExtension(entry);
    extension.setAuthor(username);

    return entry.submitNow();
  }

  @PermitAll
  public long createHallLogEntry(Hall hall, String subject, String html)
      throws IOException, LogIOException, LogCertificateException {

    String username = checkAuthorized(); /*Must be logged in*/

    String logbookServerUrl = getLogbookServerUrl();
    String logbooks = hallLogbook(hall);
    String tags = getTags();

    logger.log(Level.INFO, "Sending elog to logbook: {0}", logbooks);

    Properties config = Library.getConfiguration();

    config.setProperty("SUBMIT_URL", logbookServerUrl + "/incoming");

    LogEntry entry = new LogEntry(subject, logbooks);

    Body.ContentType type = Body.ContentType.HTML;

    entry.setBody(html, type);

    entry.setTags(tags);

    LogEntryAdminExtension extension = new LogEntryAdminExtension(entry);
    extension.setAuthor(username);

    return entry.submitNow();
  }

  /**
   * The logbook Crew Chief timesheets are posted to.
   *
   * @return ELOG, or TLOG when LOGBOOK_DEBUG is true
   */
  public static String crewChiefLogbook() {
    return isDebug() ? "TLOG" : "ELOG";
  }

  /**
   * The logbook a hall's experimenter timesheets are posted to.
   *
   * @param hall The hall
   * @return The hall's logbook, or TLOG when LOGBOOK_DEBUG is true
   */
  public static String hallLogbook(Hall hall) {
    if (isDebug()) {
      return "TLOG";
    }

    switch (hall) {
      case A:
        return "HALOG";
      case B:
        return "HBLOG";
      case C:
        return "HCLOG";
      case D:
        return "HDLOG";
      default:
        return "ELOG";
    }
  }

  // In the absence of a test server an alternative is to use production server,
  // but route entries to TLOG
  private static boolean isDebug() {
    return "true".equals(System.getenv("LOGBOOK_DEBUG"));
  }

  private static String getTags() {
    return isDebug() ? null : "BeamAccounting";
  }

  private static String getLogbookServerUrl() throws IOException {
    String logbookServerUrl = System.getenv("LOGBOOK_SERVER_URL");

    if (logbookServerUrl == null) {
      throw new IOException("logbook server not configured");
    }

    return logbookServerUrl;
  }
}
