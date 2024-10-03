package org.jlab.btm.presentation.controller;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.btm.business.service.ExpProgramService;

/**
 * @author ryans
 */
@WebServlet(
    name = "MetricsController",
    urlPatterns = {"/metrics"})
public class MetricsController extends HttpServlet {

  @EJB ExpProgramService programService;

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

    request.getRequestDispatcher("/WEB-INF/views/metrics.jsp").forward(request, response);
  }
}
