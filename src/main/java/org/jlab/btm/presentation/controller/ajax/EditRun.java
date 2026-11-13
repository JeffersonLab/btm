package org.jlab.btm.presentation.controller.ajax;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/** Sets a run's days by hand. */
@WebServlet(
    name = "EditRun",
    urlPatterns = {"/setup/ajax/edit-run"})
public class EditRun extends RunServlet {

  @Override
  void change(HttpServletRequest request) throws UserFriendlyException {
    runService.edit(
        ParamConverter.convertBigInteger(request, "id"),
        day(request, "firstDay"),
        day(request, "lastDay"));
  }
}
