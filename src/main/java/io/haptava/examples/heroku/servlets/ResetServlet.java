/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.servlets;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import com.google.common.net.HttpHeaders;
import com.sudothought.http.HttpConstants;
import io.haptava.api.jmxproxy.JmxProxy;
import io.haptava.examples.heroku.mbeans.DynoWatcherMXBean;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static com.sudothought.util.StringUtils.quote;
import static io.haptava.examples.heroku.Constants.DYNO_WATCHER_JMX_PROXY;
import static io.haptava.examples.heroku.Constants.DYNO_WATCHER_MBEAN_PROXY;
import static io.haptava.examples.heroku.Constants.getDynoWatcherMBeanObjectName;
import static java.lang.String.format;

public class ResetServlet
    extends ServletWithMetrics {

  private static final long serialVersionUID = 8652730110987874083L;

  public ResetServlet(final MetricRegistry metricRegistry) {
    super(metricRegistry, "reset");
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

    final Stopwatch sw = Stopwatch.createStarted();

    response.setContentType(HttpConstants.HTML_CONTENT);
    response.setHeader(HttpHeaders.CACHE_CONTROL, HttpConstants.NO_CACHE);
    response.setStatus(HttpServletResponse.SC_OK);

    try {
      PrintWriter writer = response.getWriter();
      writer.println("<!DOCTYPE html>\n<html>\n<body>");

      ServletContext servletContext = request.getSession().getServletContext();
      JmxProxy dynoWatcherJmxProxy = (JmxProxy) servletContext.getAttribute(DYNO_WATCHER_JMX_PROXY);
      DynoWatcherMXBean dynoWatcherProxy = (DynoWatcherMXBean) servletContext.getAttribute(DYNO_WATCHER_MBEAN_PROXY);

      if (dynoWatcherProxy == null) {
        writer.println("<p>DynoWatcher JmxProxy not available</p>");
      }
      else {
        // Invoke DynoWatcher MBean proxy
        dynoWatcherProxy.resetDynos();
        writer.println(format("<p>Invoked MBeanServer %s MBean %s%n</p>",
                              quote(dynoWatcherJmxProxy.getServerName()),
                              quote(getDynoWatcherMBeanObjectName())));
        writer.println("<p>Reset requests for all web dynos.</p>");
      }

      writer.println("<br/><button type='button' onclick=\"window.location.href='/';\">Return</button>");
      writer.println("</body>\n</html>");
      writer.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    this.markMetrics(sw);
  }
}
