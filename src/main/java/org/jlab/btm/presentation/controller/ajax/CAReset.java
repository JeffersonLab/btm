package org.jlab.btm.presentation.controller.ajax;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;
import org.jlab.btm.business.service.epics.PVMonitorManager;

/**
 * @author ryans
 */
@WebServlet(
    name = "CAReset",
    urlPatterns = {"/ajax/ca-reset"})
public class CAReset extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(CAReset.class.getName());

  @EJB PVMonitorManager manager;

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    manager.reset();

    response.sendRedirect(request.getContextPath() + "/reports/ca-status");
  }
}
