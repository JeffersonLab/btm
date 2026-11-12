package org.jlab.btm.presentation.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.jlab.btm.business.service.CcProgramIntervalService;
import org.jlab.btm.business.service.epics.PVCache;
import org.jlab.btm.business.service.epics.PVCacheEntry;
import org.jlab.btm.persistence.entity.CcProgramInterval;
import org.jlab.btm.persistence.enumeration.TimelineProgram;
import org.jlab.btm.persistence.epics.Constant;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamUtil;

/** Shows the current accelerator program and the history of program intervals. */
@WebServlet(
    name = "StateTimelineController",
    urlPatterns = {"/state-timeline"})
public class StateTimelineController extends HttpServlet {

  private static final int MAX_PER_PAGE = 25;

  @EJB CcProgramIntervalService programService;
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
    int offset;

    try {
      offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    } catch (RuntimeException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    CcProgramInterval current = programService.findCurrent();
    List<CcProgramInterval> intervalList = programService.findPage(offset, MAX_PER_PAGE);
    Paginator paginator = new Paginator(programService.count(), offset, MAX_PER_PAGE);

    String programPv = Constant.programPv();
    PVCacheEntry programPvEntry = programPv == null ? null : cache.get(programPv);
    String programPvValue = null;

    if (programPvEntry != null) {
      try {
        programPvValue = programPvEntry.getString();
      } catch (IllegalStateException e) {
        programPvValue = String.valueOf(programPvEntry.getValue());
      }
    }

    boolean editable = request.isUserInRole("btm-admin") || request.isUserInRole("cc");

    request.setAttribute("current", current);
    request.setAttribute("intervalList", intervalList);
    request.setAttribute("paginator", paginator);
    request.setAttribute("programList", TimelineProgram.values());
    request.setAttribute("programPv", programPv);
    request.setAttribute("programPvEntry", programPvEntry);
    request.setAttribute("programPvValue", programPvValue);
    request.setAttribute(
        "programPvRecognized", TimelineProgram.fromPvValue(programPvValue) != null);
    request.setAttribute("editable", editable);
    request.setAttribute("now", new Date());

    request.getRequestDispatcher("/WEB-INF/views/state-timeline.jsp").forward(request, response);
  }
}
