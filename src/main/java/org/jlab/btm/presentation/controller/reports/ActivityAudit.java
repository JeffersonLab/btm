package org.jlab.btm.presentation.controller.reports;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.btm.business.params.ActivityAuditParams;
import org.jlab.btm.business.service.RevisionInfoService;
import org.jlab.btm.persistence.entity.RevisionInfo;
import org.jlab.btm.persistence.enumeration.TimesheetType;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Shift;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "ActivityAudit",
    urlPatterns = {"/reports/activity-audit"})
public class ActivityAudit extends HttpServlet {

  @EJB RevisionInfoService revisionService;

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

    ActivityAuditParams params = convert(request);

    validate(params);

    List<RevisionInfo> revisionList = revisionService.filterList(params);
    Long totalRecords = revisionService.countFilterList(params);

    Paginator paginator =
        new Paginator(totalRecords.intValue(), params.getOffset(), params.getMaxPerPage());

    String selectionMessage = getSelectionMessage(params, paginator);

    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("revisionList", revisionList);
    request.setAttribute("paginator", paginator);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/activity-audit.jsp")
        .forward(request, response);
  }

  public ActivityAuditParams convert(HttpServletRequest request) {
    ActivityAuditParams params = new ActivityAuditParams();
    Date modifiedStart, modifiedEnd;

    try {
      modifiedStart = ParamConverter.convertFriendlyDateTime(request, "start");
      modifiedEnd = ParamConverter.convertFriendlyDateTime(request, "end");
    } catch (Exception e) {
      throw new RuntimeException("Date format error", e);
    }

    Date timesheetDate;
    try {
      timesheetDate = BtmParamConverter.convertISO8601Date(request, "date", null);
    } catch (ParseException ex) {
      throw new RuntimeException("Unable to parse date", ex);
    }

    TimesheetType type = BtmParamConverter.convertTimesheetType(request, "type");
    Shift shift = BtmParamConverter.convertShift(request, "shift", null);

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = ParamUtil.convertAndValidateNonNegativeInt(request, "maxPerPage", 10);

    params.setModifiedStart(modifiedStart);
    params.setModifiedEnd(modifiedEnd);
    params.setType(type);
    params.setTimesheetDate(timesheetDate);
    params.setShift(shift);
    params.setOffset(offset);
    params.setMaxPerPage(maxPerPage);

    return params;
  }

  public void validate(ActivityAuditParams params) {
    if (params.getModifiedStart() != null && params.getModifiedEnd() != null) {
      if (params.getModifiedEnd().before(params.getModifiedStart())) {
        throw new RuntimeException("End date must not come before start date");
      }
    }

    if (params.getShift() != null
        && (params.getType() == null || params.getTimesheetDate() == null)) {
      throw new RuntimeException("Shift requires timesheet type and day");
    }

    if (params.getTimesheetDate() != null
        && (params.getType() == null || params.getShift() == null)) {
      throw new RuntimeException("Timesheet Date requires timesheet type and shift");
    }
  }

  public String getSelectionMessage(ActivityAuditParams params, Paginator paginator) {
    List<String> filters = new ArrayList<>();

    if (params.getModifiedStart() != null && params.getModifiedEnd() != null) {
      filters.add(
          TimeUtil.formatSmartRangeSeparateTime(
              params.getModifiedStart(), params.getModifiedEnd()));
    } else if (params.getModifiedStart() != null) {
      filters.add("Starting " + TimeUtil.formatSmartSingleTime(params.getModifiedStart()));
    } else if (params.getModifiedEnd() != null) {
      filters.add("Before " + TimeUtil.formatSmartSingleTime(params.getModifiedEnd()));
    }

    if (params.getType() != null) {
      filters.add("Type \"" + params.getType().getLabel() + "\"");
    }

    if (params.getTimesheetDate() != null) {
      filters.add(
          "Timesheet Date \"" + TimeUtil.formatSmartSingleTime(params.getTimesheetDate()) + "\"");
    }

    if (params.getShift() != null) {
      filters.add("Shift \"" + params.getShift().getLabel() + "\"");
    }

    String filterMessage = "";

    if (!filters.isEmpty()) {
      filterMessage = filters.get(0);

      for (int i = 1; i < filters.size(); i++) {
        String filter = filters.get(i);
        filterMessage += " and " + filter;
      }
    }

    DecimalFormat formatter = new DecimalFormat("###,###");

    String selectionMessage = "All Transactions ";

    if (filterMessage.length() > 0) {
      selectionMessage = filterMessage;
    }

    if (paginator.getTotalRecords() < paginator.getMaxPerPage() && paginator.getOffset() == 0) {
      selectionMessage =
          selectionMessage + " {" + formatter.format(paginator.getTotalRecords()) + "}";
    } else {
      selectionMessage =
          selectionMessage
              + " {"
              + formatter.format(paginator.getStartNumber())
              + " - "
              + formatter.format(paginator.getEndNumber())
              + " of "
              + formatter.format(paginator.getTotalRecords())
              + "}";
    }

    return selectionMessage;
  }
}
