package org.jlab.btm.presentation.controller.reports.audit;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.List;
import org.jlab.btm.business.service.audit.CcAccHourAudService;
import org.jlab.btm.persistence.entity.audit.CcAccHourAud;
import org.jlab.btm.persistence.enumeration.DurationUnits;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "CcAccHourAudController",
    urlPatterns = {"/reports/activity-audit/cc-acc-hour"})
public class CcAccHourAudController extends HttpServlet {

  @EJB CcAccHourAudService audService;

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

    List<CcAccHourAud> entityList = null;
    Long totalRecords = 0L;
    String selectionMessage = "";

    if (entityId != null) {
      entityList = audService.filterList(entityId, revisionId, offset, maxPerPage);
      totalRecords = audService.countFilterList(entityId, revisionId);
      SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH z");
      selectionMessage = format.format(entityList.get(0).getDayAndHour()) + " (" + entityId + ")";
    }

    Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

    request.setAttribute("entityList", entityList);
    request.setAttribute("paginator", paginator);
    request.setAttribute("durationUnits", DurationUnits.HOURS);
    request.setAttribute("selectionMessage", selectionMessage);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/activity-audit/cc-acc-hour.jsp")
        .forward(request, response);
  }
}
