package org.jlab.btm.presentation.controller;

import org.jlab.btm.business.service.ExpShiftPurposeService;
import org.jlab.btm.persistence.entity.ExpShiftPurpose;
import org.jlab.smoothness.persistence.enumeration.Hall;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author ryans
 */
@WebServlet(name = "ProgramsController", urlPatterns = {"/programs"})
public class ProgramsController extends HttpServlet {

    @EJB
    ExpShiftPurposeService purposeService;

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<ExpShiftPurpose> hallAPurposeList = purposeService.findByHall(Hall.A, null);
        List<ExpShiftPurpose> hallBPurposeList = purposeService.findByHall(Hall.B, null);
        List<ExpShiftPurpose> hallCPurposeList = purposeService.findByHall(Hall.C, null);
        List<ExpShiftPurpose> hallDPurposeList = purposeService.findByHall(Hall.D, null);

        boolean editable = request.isUserInRole("schcom")
                || request.isUserInRole("btm-admin") || request.isUserInRole("cc");

        request.setAttribute("hallAPurposeList", hallAPurposeList);
        request.setAttribute("hallBPurposeList", hallBPurposeList);
        request.setAttribute("hallCPurposeList", hallCPurposeList);
        request.setAttribute("hallDPurposeList", hallDPurposeList);
        request.setAttribute("editable", editable);

        request.getRequestDispatcher("/WEB-INF/views/programs.jsp").forward(request, response);
    }
}
