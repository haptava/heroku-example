package io.haptava.examples.heroku.servlets;

import com.google.common.net.HttpHeaders;
import com.sudothought.http.HttpConstants;
import com.sudothought.util.IoUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class HtmlPage
    extends HttpServlet {

  private final String filename;

  public HtmlPage(final String filename) {
    this.filename = filename;
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

    response.setContentType(HttpConstants.HTML_CONTENT);
    response.setHeader(HttpHeaders.CACHE_CONTROL, HttpConstants.NO_CACHE);
    response.setStatus(HttpServletResponse.SC_OK);

    try {
      PrintWriter writer = response.getWriter();
      final String file = IoUtils.readFile(this.filename);
      writer.print(file);
      writer.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
