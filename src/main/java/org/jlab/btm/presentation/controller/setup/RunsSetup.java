package org.jlab.btm.presentation.controller.setup;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jlab.btm.business.service.RunService;
import org.jlab.btm.persistence.entity.Run;

/**
 * Shows the runs, which reports offer as date ranges, and recalculates them from the published
 * schedules on request. Admins only, as all setup pages.
 */
@WebServlet(
    name = "RunsSetup",
    urlPatterns = {"/setup/runs"})
public class RunsSetup extends HttpServlet {

  @EJB RunService runService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    List<Run> runList = runService.findAllWithScheduled();
    Run currentRun = runService.getCurrentRun();

    request.setAttribute("runList", runList);
    request.setAttribute("currentRun", currentRun);
    request.setAttribute("previousRun", runService.getRunBefore(currentRun));

    request.getRequestDispatcher("/WEB-INF/views/setup/runs.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if ("recalculate".equals(request.getParameter("action"))) {
      runService.recalculate();
    }

    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/setup/runs"));
  }
}
