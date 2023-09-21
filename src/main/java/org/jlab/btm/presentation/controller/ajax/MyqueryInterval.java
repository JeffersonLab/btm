package org.jlab.btm.presentation.controller.ajax;

import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ServletUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author ryans
 */
@WebServlet(name = "MyqueryInterval", urlPatterns = {"/myquery/interval"})
public class MyqueryInterval extends HttpServlet {

    public static final List<String> ALLOWED_CHANNELS = Arrays.asList("IBC1H04CRCUR2", "IBC2C24CRCUR3", "IBC3H00CRCUR4", "IBCAD00CRCUR6");

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String channel = request.getParameter("c");
        String begin = request.getParameter("b");
        String end = request.getParameter("e");

        if(!ALLOWED_CHANNELS.contains(channel)) {
            throw new ServletException("Channel not allowed: " + channel);
        }

        // Let's make sure the dates are formatted correctly
        try {
            ParamConverter.convertISO8601Date(request, "b");
            ParamConverter.convertISO8601Date(request, "e");
        } catch(ParseException e) {
            throw new ServletException("Date format invalid");
        }

        String MYQUERY_SERVER_URL = "https://epicsweb.jlab.org";

        ParamBuilder builder = new ParamBuilder();
        builder.add("c", channel);
        builder.add("b", begin);
        builder.add("e", end);
        builder.add("m", "history");
        builder.add("u", "on");
        builder.add("a", "on");
        builder.add("p", "on"); // Include Previous point
        builder.add("i", "on"); // Integrate
        builder.add("l", "100"); // Limit Results / Downsample
        builder.add("t", "graphical"); // Sampling Algorithm

        String queryString = ServletUtil.buildQueryString(builder.getParams(), "UTF-8");

        String uriStr = MYQUERY_SERVER_URL + "/myquery/interval" + queryString;

        try {
            HttpRequest proxyRequest = HttpRequest.newBuilder(new URI(uriStr))
                    .header("Accept", "application/json")
                    .build();

            CompletableFuture<HttpResponse<String>> future = HttpClient.newHttpClient()
                    .sendAsync(proxyRequest, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> proxyResponse = future.get();

            response.getWriter().println(proxyResponse.body());
        } catch(URISyntaxException | InterruptedException | ExecutionException e) {
            throw new ServletException("Unable to query myquery", e);
        }
    }
}
