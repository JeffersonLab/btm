package org.jlab.btm.presentation.controller.ajax;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/** Adds a run by hand. */
@WebServlet(
    name = "AddRun",
    urlPatterns = {"/setup/ajax/add-run"})
public class AddRun extends RunServlet {

  @Override
  void change(HttpServletRequest request) throws UserFriendlyException {
    runService.add(day(request, "firstDay"), day(request, "lastDay"));
  }
}
