package org.jlab.btm.presentation.controller.reports.audit;

import org.jlab.btm.business.service.audit.CcShiftAudService;
import org.jlab.btm.persistence.entity.audit.CcShiftAud;
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
import java.math.BigInteger;
import java.util.List;

/**
 *
 * @author ryans
 */
@WebServlet(name = "CcShiftAudController", urlPatterns = {"/reports/activity-audit/cc-shift"})
public class CcShiftAudController extends HttpServlet {

    @EJB
    CcShiftAudService audService;
    
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

        BigInteger entityId = ParamConverter.convertBigInteger(request, "entityId");
        BigInteger revisionId = ParamConverter.convertBigInteger(request, "revisionId");
        
        int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
        int maxPerPage = 5;

        List<CcShiftAud> entityList = null;
        Long totalRecords = 0L;
        
        if(entityId != null) {
            entityList = audService.filterList(entityId, revisionId, offset, maxPerPage);
            totalRecords = audService.countFilterList(entityId, revisionId);
        }
        
        Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

        
        request.setAttribute("entityList", entityList);
        request.setAttribute("paginator", paginator);        
        
        request.getRequestDispatcher("/WEB-INF/views/reports/activity-audit/cc-shift.jsp").forward(request, response);
    }
}
