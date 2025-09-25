package org.jlab.btm.presentation.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InternalHtmlRequestExecutor {

  public static String execute(
      String path, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    InternalResponseWrapper responseWrapper = new InternalResponseWrapper(response);
    request.getServletContext().getRequestDispatcher(path).include(request, responseWrapper);
    return responseWrapper.getHTML();
  }
}
