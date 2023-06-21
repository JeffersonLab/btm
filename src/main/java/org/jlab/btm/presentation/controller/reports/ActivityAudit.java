package org.jlab.btm.presentation.controller.reports;

import org.jlab.btm.business.service.RevisionInfoService;
import org.jlab.btm.persistence.entity.RevisionInfo;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ryans
 */
@WebServlet(name = "ActivityAudit", urlPatterns = {"/reports/activity-audit"})
public class ActivityAudit extends HttpServlet {

    @EJB
    RevisionInfoService revisionService;

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Date modifiedStart, modifiedEnd;

        try {
            modifiedStart = ParamConverter.convertFriendlyDateTime(request, "start");
            modifiedEnd = ParamConverter.convertFriendlyDateTime(request, "end");
        } catch (ParseException e) {
            throw new RuntimeException("Date format error", e);
        }

        int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
        int maxPerPage = 10;

        List<RevisionInfo> revisionList = revisionService.filterList(modifiedStart, modifiedEnd, offset, maxPerPage);
        Long totalRecords = revisionService.countFilterList(modifiedStart, modifiedEnd);
        
        Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

        request.setAttribute("revisionList", revisionList);
        request.setAttribute("paginator", paginator);

        request.getRequestDispatcher("/WEB-INF/views/reports/activity-audit.jsp").forward(request, response);
    }
}
