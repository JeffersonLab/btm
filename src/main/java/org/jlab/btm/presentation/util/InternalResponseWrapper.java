package org.jlab.btm.presentation.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class InternalResponseWrapper extends HttpServletResponseWrapper {

  private final StringWriter stringWriter = new StringWriter();
  private final PrintWriter printWriter = new PrintWriter(stringWriter);
  private final ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
  private final ServletOutputStream soStream =
      new ServletOutputStream() {
        @Override
        public void write(int b) throws IOException {
          baoStream.write(b);
        }

        @Override
        public boolean isReady() {
          throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
          throw new UnsupportedOperationException("Not supported yet.");
        }
      };

  public InternalResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public PrintWriter getWriter() {
    return printWriter;
  }

  @Override
  public ServletOutputStream getOutputStream() {
    return soStream;
  }

  public String getHTML() {
    return stringWriter.toString();
  }

  public ByteArrayOutputStream getBinary() {
    return baoStream;
  }
}
