package org.jlab.btm.presentation.util;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InternalHtmlRequestExecutor {

  public static String execute(
      String path, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    InternalResponseWrapper responseWrapper = new InternalResponseWrapper(response);
    request.getServletContext().getRequestDispatcher(path).include(request, responseWrapper);
    return responseWrapper.getHTML();
  }
}
