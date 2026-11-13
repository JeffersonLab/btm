package org.jlab.btm.presentation.controller.ajax;

import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.Instant;
import org.jlab.btm.business.service.CcProgramIntervalService;
import org.jlab.btm.persistence.enumeration.TimelineProgram;
import org.jlab.btm.presentation.util.BtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/** A change to the State Timeline. */
abstract class ProgramIntervalServlet extends ChangeServlet {

  @EJB CcProgramIntervalService programService;

  @Override
  String subject() {
    return "the state timeline";
  }

  static TimelineProgram program(HttpServletRequest request) throws UserFriendlyException {
    String value = request.getParameter("program");

    if (value == null || value.isEmpty()) {
      return null;
    }

    try {
      return TimelineProgram.valueOf(value);
    } catch (IllegalArgumentException e) {
      throw new UserFriendlyException("Unknown program: " + value);
    }
  }

  static Instant start(HttpServletRequest request) throws UserFriendlyException {
    try {
      return BtmParamConverter.convertJLabDateTimeWithSeconds(request, "start");
    } catch (ParseException e) {
      throw new UserFriendlyException(
          "Start must be like 31-Jan-2026 13:45 or 31-Jan-2026 13:45:59");
    }
  }
}
