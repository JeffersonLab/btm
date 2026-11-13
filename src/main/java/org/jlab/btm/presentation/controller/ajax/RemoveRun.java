package org.jlab.btm.presentation.controller.ajax;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/** Removes a run set by hand, so the published schedules apply to its days again. */
@WebServlet(
    name = "RemoveRun",
    urlPatterns = {"/setup/ajax/remove-run"})
public class RemoveRun extends RunServlet {

  @Override
  void change(HttpServletRequest request) throws UserFriendlyException {
    runService.remove(ParamConverter.convertBigInteger(request, "id"));
  }
}
