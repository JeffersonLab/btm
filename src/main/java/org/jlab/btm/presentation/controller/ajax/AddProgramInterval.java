package org.jlab.btm.presentation.controller.ajax;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/** Adds a program change to the State Timeline. */
@WebServlet(
    name = "AddProgramInterval",
    urlPatterns = {"/ajax/add-program-interval"})
public class AddProgramInterval extends ProgramIntervalServlet {

  @Override
  void change(HttpServletRequest request) throws UserFriendlyException {
    programService.add(program(request), start(request));
  }
}
