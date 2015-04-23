/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.servlets;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.net.HttpHeaders;
import com.sudothought.http.HttpConstants;
import io.haptava.api.jmxproxy.JmxProxy;
import io.haptava.examples.heroku.mbeans.Dyno;
import io.haptava.examples.heroku.mbeans.DynoWatcherMXBean;
import io.haptava.examples.heroku.mbeans.Request;
import io.haptava.mbeans.server.mbeanserver.MBeanServerAdminMXBean;
import io.haptava.mbeans.server.mbeanserver.MBeanServerData;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

import static com.sudothought.util.StringUtils.quote;
import static com.sudothought.util.TimeUtils.formatInterval;
import static io.haptava.examples.heroku.Constants.ADMIN_OBJECT_NAME;
import static io.haptava.examples.heroku.Constants.DYNO_WATCHER_JMX_PROXY;
import static io.haptava.examples.heroku.Constants.DYNO_WATCHER_MBEAN_PROXY;
import static io.haptava.examples.heroku.Constants.MBEANSERVER_ADMIN_PROXY;
import static io.haptava.examples.heroku.Constants.USERGROUP_JMX_PROXY;
import static io.haptava.examples.heroku.Constants.getApplicationId;
import static io.haptava.examples.heroku.Constants.getDynoWatcherMBeanObjectName;
import static java.lang.String.format;

public class SummaryServlet
    extends ServletWithMetrics {

  private static final long serialVersionUID = 8169409140957634977L;

  public SummaryServlet(final MetricRegistry metricRegistry, final String metricName) {
    super(metricRegistry, metricName);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

    final Stopwatch sw = Stopwatch.createStarted();

    response.setContentType(HttpConstants.PLAIN_CONTENT);
    response.setHeader(HttpHeaders.CACHE_CONTROL, HttpConstants.NO_CACHE);
    response.setStatus(HttpServletResponse.SC_OK);

    try {
      PrintWriter writer = response.getWriter();
      ServletContext servletContext = request.getSession().getServletContext();
      JmxProxy userGroupJmxProxy = (JmxProxy) servletContext.getAttribute(USERGROUP_JMX_PROXY);
      JmxProxy dynoWatcherJmxProxy = (JmxProxy) servletContext.getAttribute(DYNO_WATCHER_JMX_PROXY);
      MBeanServerAdminMXBean mbeanServerAdminProxy = (MBeanServerAdminMXBean) servletContext.getAttribute(MBEANSERVER_ADMIN_PROXY);
      DynoWatcherMXBean dynoWatcherProxy = (DynoWatcherMXBean) servletContext.getAttribute(DYNO_WATCHER_MBEAN_PROXY);

      if (mbeanServerAdminProxy == null) {
        writer.println("MBeanServerAdmin JmxProxy not available");
      }
      else {
        writer.println(format("Invoked MBeanServer %s MBean %s%n",
                              quote(userGroupJmxProxy.getServerName()),
                              quote(ADMIN_OBJECT_NAME)));

        // Show only Heroku-related servers
        List<MBeanServerData> mbeanServers = mbeanServerAdminProxy.getServers(format("*-%s-*", getApplicationId()));
        int cnt = mbeanServers.size();
        writer.println(format("There are %d Heroku-related MBeanServer%s:", cnt, cnt == 1 ? "" : "s"));
        for (MBeanServerData mbeanServer : mbeanServers)
          writer.println(format("%s [%s]", mbeanServer.getName(), mbeanServer.getStatus()));
      }

      writer.println(format("%n=======================================%n"));

      if (dynoWatcherProxy == null) {
        writer.println("DynoWatcher JmxProxy not available");
      }
      else {
        // Invoke DynoWatcher MBean proxy
        List<Dyno> unsortedDynos = dynoWatcherProxy.getDynos();

        if (unsortedDynos == null || unsortedDynos.size() == 0) {
          writer.println("No web dynos registered");
        }
        else {
          // Sort dynos with most recent activity first
          List<Dyno> sortedDynos =
              FluentIterable
                  .from(unsortedDynos)
                  .toSortedList(new Comparator<Dyno>() {
                    @Override
                    public int compare(final Dyno d0, final Dyno d1) {
                      return
                          ComparisonChain
                              .start()
                              .compare(d1.getLastActivityMillis(), d0.getLastActivityMillis())
                              .compare(d0.getCreateTimeMillis(), d1.getCreateTimeMillis())
                              .result();
                    }
                  });

          writer.println(format("Invoked MBeanServer %s MBean %s%n",
                                quote(dynoWatcherJmxProxy.getServerName()),
                                quote(getDynoWatcherMBeanObjectName())));
          writer.println(format("Web dyno request summary for Application ID %s (%d Web Dyno%s):%n",
                                getApplicationId(), sortedDynos.size(), sortedDynos.size() == 1 ? "" : "s"));

          for (Dyno dyno : sortedDynos) {
            writer.println(format("Request count for %s: %d [uptime: %s] [%s]",
                                  dyno.getName(),
                                  dyno.getRequestCount(),
                                  formatInterval(dyno.getCreateTimeMillis(), false),
                                  dyno.getHostName()));
            if (dyno.getRequests().size() > 0) {
              writer.println("Recent requests:");
              for (final Request dynoRequest : dyno.getRequests())
                writer.println(dynoRequest);
            }
            writer.println();
          }
        }
      }

      writer.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    this.markMetrics(sw);
  }
}
