package org.jlab.btm.presentation.controller.ajax;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/** Removes a State Timeline interval. */
@WebServlet(
    name = "RemoveProgramInterval",
    urlPatterns = {"/ajax/remove-program-interval"})
public class RemoveProgramInterval extends ProgramIntervalServlet {

  @Override
  void change(HttpServletRequest request) throws UserFriendlyException {
    programService.remove(ParamConverter.convertBigInteger(request, "id"));
  }
}
