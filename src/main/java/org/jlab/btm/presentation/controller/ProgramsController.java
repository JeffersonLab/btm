package org.jlab.btm.presentation.controller;

import org.jlab.btm.business.service.ExpProgramService;
import org.jlab.btm.persistence.entity.ExpProgram;
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
    ExpProgramService programService;

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

        List<ExpProgram> hallAPurposeList = programService.findByHall(Hall.A, null);
        List<ExpProgram> hallBPurposeList = programService.findByHall(Hall.B, null);
        List<ExpProgram> hallCPurposeList = programService.findByHall(Hall.C, null);
        List<ExpProgram> hallDPurposeList = programService.findByHall(Hall.D, null);

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
