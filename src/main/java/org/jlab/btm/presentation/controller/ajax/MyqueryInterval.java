package org.jlab.btm.presentation.controller.ajax;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.jlab.smoothness.business.service.SettingsService;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ServletUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "MyqueryInterval",
    urlPatterns = {"/myquery/interval"})
public class MyqueryInterval extends HttpServlet {

  public static final List<String> ALLOWED_CHANNELS =
      Arrays.asList("IBC1H04CRCUR2", "IBC2C24CRCUR3", "IBC3H00CRCUR4", "IBCAD00CRCUR6");

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String channel = request.getParameter("c");
    String begin = request.getParameter("b");
    String end = request.getParameter("e");
    Date endDate;

    if (!ALLOWED_CHANNELS.contains(channel)) {
      throw new ServletException("Channel not allowed: " + channel);
    }

    // Let's make sure the dates are formatted correctly
    try {
      ParamConverter.convertISO8601Date(request, "b");
      endDate = ParamConverter.convertISO8601Date(request, "e");
    } catch (Exception e) {
      throw new ServletException("Date format invalid");
    }

    String MYQUERY_SERVER_URL = SettingsService.cachedSettings.get("MYQUERY_SERVER_URL");

    ParamBuilder builder = new ParamBuilder();
    builder.add("c", channel);
    builder.add("b", begin);
    builder.add("e", end);
    builder.add(
        "m", "ops"); // history deployment goes further back, but doesn't have current month!
    builder.add("u", "on");
    builder.add("a", "on");
    builder.add("p", "on"); // Include Previous point
    builder.add("i", "on"); // Integrate
    builder.add("l", "100"); // Limit Results / Downsample
    builder.add("t", "graphical"); // Sampling Algorithm

    String queryString = ServletUtil.buildQueryString(builder.getParams(), "UTF-8");

    String uriStr = MYQUERY_SERVER_URL + "/myquery/interval" + queryString;

    try {
      HttpRequest proxyRequest =
          HttpRequest.newBuilder(new URI(uriStr)).header("Accept", "application/json").build();

      CompletableFuture<HttpResponse<String>> future =
          HttpClient.newHttpClient().sendAsync(proxyRequest, HttpResponse.BodyHandlers.ofString());

      HttpResponse<String> proxyResponse = future.get();

      // Cache logic resides in setContentType so order matters
      response.setContentType("application/json");

      // App auto-truncates end date to today if past today.  But if end date is today (after
      // yesterday) then
      // don't cache! Otherwise, Cache!
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -1);
      Date yesterday = cal.getTime();

      if (endDate.before(yesterday)) {
        System.err.println("Cache!");

        // If end date in the past, we need to override default CacheFilter behavior of NOT caching
        // application/json
        request.setAttribute("CACHEABLE_RESPONSE", true);

        response.setDateHeader("Expires", System.currentTimeMillis() + 31536000000L);
        response.setHeader(
            "Cache-Control", null); // Remove header automatically added by SSL/TLS container module
        response.setHeader(
            "Pragma", null); // Remove header automatically added by SSL/TLS container module

      } else {
        System.err.println("Don't cache!");

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Proxies
      }

      response.getWriter().println(proxyResponse.body());
    } catch (URISyntaxException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
      throw new ServletException("Unable to query myquery", e);
    }
  }
}
