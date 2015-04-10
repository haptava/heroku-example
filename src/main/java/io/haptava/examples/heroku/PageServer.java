/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.sudothought.metrics.MetricUtils;
import com.sudothought.metrics.ObjectNameWithFolders;
import io.haptava.examples.heroku.servlets.DefaultServlet;
import io.haptava.examples.heroku.servlets.DynoContextListener;
import io.haptava.examples.heroku.servlets.ResetServlet;
import io.haptava.examples.heroku.servlets.SummaryServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class PageServer {

  public static void main(final String[] argv)
      throws Exception {

    MetricRegistry metricRegistry = new MetricRegistry();

    JmxReporter.forRegistry(metricRegistry)
               .inDomain("io.haptava.heroku")
               .createsObjectNamesWith(ObjectNameWithFolders.OBJECT_NAMES_WITH_FOLDERS)
               .convertRatesTo(TimeUnit.MINUTES)
               .convertDurationsTo(TimeUnit.MILLISECONDS)
               .registerWith(ManagementFactory.getPlatformMBeanServer())
               .build()
               .start();

    MetricUtils.newJvmMetricCollection(metricRegistry, ManagementFactory.getPlatformMBeanServer())
               .register();

    Context context = new Context(Context.SESSIONS);
    context.setContextPath("/");
    context.addEventListener(new DynoContextListener());
    context.addServlet(new ServletHolder(new ResetServlet(metricRegistry)), "/reset");
    context.addServlet(new ServletHolder(new SummaryServlet(metricRegistry)), "/summary");
    context.addServlet(new ServletHolder(new DefaultServlet(metricRegistry)), "/*");

    String portVal = System.getenv("PORT");
    int port = portVal != null ? Integer.valueOf(portVal) : 8000;
    Server server = new Server(port);
    server.setHandler(context);
    server.start();
    server.join();
  }
}
