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

    final MetricRegistry metricRegistry = new MetricRegistry();

    JmxReporter.forRegistry(metricRegistry)
               .inDomain("io.haptava.heroku")
               .createsObjectNamesWith(ObjectNameWithFolders.OBJECT_NAMES_WITH_FOLDERS)
               .convertRatesTo(TimeUnit.SECONDS)
               .convertDurationsTo(TimeUnit.MILLISECONDS)
               .registerWith(ManagementFactory.getPlatformMBeanServer())
               .build()
               .start();

    MetricUtils.newJvmMetricCollection(metricRegistry, ManagementFactory.getPlatformMBeanServer())
               .register();

    final Context context = new Context(Context.SESSIONS);
    context.setContextPath("/");
    context.addEventListener(new DynoContextListener());
    context.addServlet(new ServletHolder(new ResetServlet(metricRegistry)), "/reset");
    context.addServlet(new ServletHolder(new SummaryServlet(metricRegistry)), "/summary");
    context.addServlet(new ServletHolder(new DefaultServlet(metricRegistry)), "/*");

    int port = Integer.valueOf(System.getenv("PORT"));
    final Server server = new Server(port);
    server.setHandler(context);
    server.start();
    server.join();
  }
}
