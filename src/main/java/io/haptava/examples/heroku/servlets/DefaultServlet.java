/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.servlets;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import com.google.common.net.HttpHeaders;
import com.sudothought.http.HttpConstants;
import com.sudothought.util.Utils;
import io.haptava.api.jmxproxy.JmxProxy;
import io.haptava.examples.heroku.mbeans.DynoMXBean;
import io.haptava.examples.heroku.mbeans.Request;

import javax.servlet.ServletContext;
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
    extends ServletWithMetrics {

  private static final long serialVersionUID = 3290741662122411590L;

  public DefaultServlet(final MetricRegistry metricRegistry, final String metricName) {
    super(metricRegistry, metricName);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

    final Stopwatch sw = Stopwatch.createStarted();

    response.setContentType(HttpConstants.HTML_CONTENT);
    response.setHeader(HttpHeaders.CACHE_CONTROL, HttpConstants.NO_CACHE);
    response.setStatus(HttpServletResponse.SC_OK);

    try {
      PrintWriter writer = response.getWriter();
      writer.println("<!DOCTYPE html>\n<html>"
                         + "\n<head><meta name=\"blitz\" content=\"mu-dd4bffbb-ff2e9926-5a80952c-1c6cb64d\"></head>"
                         + "\n<body>");
      writer.println("<a href='https://api.haptava.io/webdav/MBeanServers/System/Launchers' " +
                         "target='_blank'>View MBeanServers</a><br/>");
      writer.println(format("<br/><button type='button' onclick=\"window.location.href='/%s';\">" +
                                "Make Request</button>", format("request-%d.html", new Random().nextInt(100))));
      writer.println("<button type='button' onclick=\"window.location.href='/reset';\">Reset All Web Dynos</button>");

      ServletContext servletContext = request.getSession().getServletContext();
      JmxProxy watcherJmxProxy = (JmxProxy) servletContext.getAttribute(DYNO_WATCHER_JMX_PROXY);
      DynoMXBean dynoProxy = (DynoMXBean) servletContext.getAttribute(DYNO_PROXY);
      String dynoName = (String) servletContext.getAttribute(DYNO_NAME);

      if (dynoProxy == null) {
        writer.println("<p>Dyno JmxProxy not available</p>");
      }
      else {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String url = isNullOrEmpty(queryString) ? uri : format("%s?%s", uri, queryString);

        // Invoke Dyno MBean proxy methods
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

      // Display summary of all the dynos in an iframe
      writer.println("<br/><p>Summary of all web dynos:</p>");
      writer.println("<iframe src='/summary' width='100%' height='800'>></iframe>");
      writer.println("</body>\n</html>");
      writer.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    this.markMetrics(sw);
  }
}
