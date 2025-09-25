package org.jlab.btm.presentation.controller.reports;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jlab.btm.business.service.epics.PVCache;

/**
 * @author ryans
 */
@WebServlet(
    name = "CAStatus",
    urlPatterns = {"/reports/ca-status"})
public class CAStatusReport extends HttpServlet {

  @EJB PVCache cache;

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

    request.setAttribute("cache", cache);

    request.getRequestDispatcher("/WEB-INF/views/reports/ca-status.jsp").forward(request, response);
  }
}
