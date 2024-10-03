package org.jlab.btm.presentation.controller.reports.audit;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.btm.business.service.audit.ExpShiftAudService;
import org.jlab.btm.business.util.BtmTimeUtil;
import org.jlab.btm.persistence.entity.audit.ExpShiftAud;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExpShiftAudController",
    urlPatterns = {"/reports/activity-audit/exp-shift"})
public class ExpShiftAudController extends HttpServlet {

  @EJB ExpShiftAudService audService;

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

    BigInteger entityId = ParamConverter.convertBigInteger(request, "entityId");
    BigInteger revisionId = ParamConverter.convertBigInteger(request, "revisionId");

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = 5;

    List<ExpShiftAud> entityList = null;
    Long totalRecords = 0L;
    String selectionMessage = "";
    Hall hall = null;

    if (entityId != null) {
      entityList = audService.filterList(entityId, revisionId, offset, maxPerPage);

      if (entityList != null && !entityList.isEmpty()) {
        hall = entityList.get(0).getHall();
      }

      totalRecords = audService.countFilterList(entityId, revisionId);
      SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
      selectionMessage =
          (hall == null ? "" : hall.name() + " ")
              + format.format(entityList.get(0).getStartDayAndHour())
              + " "
              + BtmTimeUtil.calculateExperimenterShift(entityList.get(0).getStartDayAndHour())
                  .name()
              + " ("
              + entityId
              + ")";
    }

    Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

    request.setAttribute("entityList", entityList);
    request.setAttribute("paginator", paginator);
    request.setAttribute("selectionMessage", selectionMessage);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/activity-audit/exp-shift.jsp")
        .forward(request, response);
  }
}
