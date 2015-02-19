/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.servlets;

import com.sudothought.http.HttpConstants;
import com.sudothought.util.Utils;
import io.haptava.api.jmxproxy.JmxProxy;
import io.haptava.examples.heroku.mbeans.DynoMXBean;
import io.haptava.examples.heroku.mbeans.Request;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.sudothought.util.StringUtils.quote;
import static io.haptava.examples.heroku.Constants.DYNO_NAME;
import static io.haptava.examples.heroku.Constants.DYNO_PROXY;
import static io.haptava.examples.heroku.Constants.DYNO_WATCHER_JMX_PROXY;
import static io.haptava.examples.heroku.Constants.getDynoMBeanObjectName;
import static java.lang.String.format;

public class DefaultServlet
    extends HttpServlet {

  private static final long serialVersionUID = 3290741662122411590L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

    response.setContentType(HttpConstants.HTML_CONTENT);
    response.setStatus(HttpServletResponse.SC_OK);

    try {
      PrintWriter writer = response.getWriter();
      writer.println("<!DOCTYPE html>\n<html>\n<body>");
      writer.println(format("<br/><button type='button' onclick=\"window.location.href='/%s';\">" +
                                "Make Request</button>", format("request-%d.html", new Random().nextInt(100))));
      writer.println("<button type='button' onclick=\"window.location.href='/reset';\">Reset All Web Dynos</button>");

      ServletContext servletContext = request.getSession().getServletContext();
      JmxProxy watcherJmxProxy = (JmxProxy) servletContext.getAttribute(DYNO_WATCHER_JMX_PROXY);
      String dynoName = (String) servletContext.getAttribute(DYNO_NAME);
      DynoMXBean dynoProxy = (DynoMXBean) servletContext.getAttribute(DYNO_PROXY);

      if (dynoProxy == null) {
        writer.println("<p>Dyno JmxProxy not available</p>");
      }
      else {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String url = isNullOrEmpty(queryString) ? uri : format("%s?%s", uri, queryString);

        // Invoke Dyno MBean proxies
        dynoProxy.recordRequest(url);
        int count = dynoProxy.getRequestCount();
        List<Request> requests = dynoProxy.getRequests(5);

        writer.println(format("<p>Invoked MBeanServer %s MBean %s%n</p>",
                              quote(watcherJmxProxy.getServerName()),
                              quote(getDynoMBeanObjectName(dynoName))));
        writer.println(format("<p>Request count for %s: %d [%s]</p>", dynoName, count, Utils.getHostName()));
        if (requests != null && requests.size() > 0) {
          writer.println("<p>\nRecent requests:</p><ul>");
          for (final Request getRequest : requests)
            writer.println("<li>" + getRequest + "</li>");
          writer.println("</ul>");
        }
      }

      writer.println("<br/><p>Summary of all web dynos:</p>");
      writer.println("<iframe src='/summary' width='100%' height='800'>></iframe>");

      writer.println("</body>\n</html>");
      writer.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
