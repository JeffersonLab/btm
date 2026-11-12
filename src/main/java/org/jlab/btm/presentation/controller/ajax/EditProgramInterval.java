package org.jlab.btm.presentation.controller.ajax;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/** Changes the program or start of a State Timeline interval. */
@WebServlet(
    name = "EditProgramInterval",
    urlPatterns = {"/ajax/edit-program-interval"})
public class EditProgramInterval extends ProgramIntervalServlet {

  @Override
  void change(HttpServletRequest request) throws UserFriendlyException {
    programService.edit(
        ParamConverter.convertBigInteger(request, "id"), program(request), start(request));
  }
}
