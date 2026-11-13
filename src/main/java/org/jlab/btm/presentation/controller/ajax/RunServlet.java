package org.jlab.btm.presentation.controller.ajax;

import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Date;
import org.jlab.btm.business.service.RunService;
import org.jlab.btm.business.util.RunCalculator;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/** A change to the runs, from the Runs setup page. */
abstract class RunServlet extends ChangeServlet {

  @EJB RunService runService;

  @Override
  String subject() {
    return "the runs";
  }

  static LocalDate day(HttpServletRequest request, String name) throws UserFriendlyException {
    Date date = ParamConverter.convertFriendlyDate(request, name);

    return date == null ? null : RunCalculator.toLocalDate(date);
  }
}
