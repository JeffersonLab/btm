package org.jlab.btm.presentation.util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InternalHtmlRequestExecutor {

    public static String execute(String path, HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException, IOException {
        InternalResponseWrapper responseWrapper = new InternalResponseWrapper(
                response);
        request.getServletContext().getRequestDispatcher(
                path).include(request,
                responseWrapper);
        return responseWrapper.getHTML();
    }
}
