/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku;

import io.haptava.examples.heroku.servlets.DefaultServlet;
import io.haptava.examples.heroku.servlets.DynoContextListener;
import io.haptava.examples.heroku.servlets.ResetServlet;
import io.haptava.examples.heroku.servlets.SummaryServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class PageServer {

  public static void main(final String[] argv)
      throws Exception {

    final Context context = new Context(Context.SESSIONS);
    context.setContextPath("/");
    context.addEventListener(new DynoContextListener());
    context.addServlet(new ServletHolder(new ResetServlet()), "/reset");
    context.addServlet(new ServletHolder(new SummaryServlet()), "/summary");
    context.addServlet(new ServletHolder(new DefaultServlet()), "/*");

    int port = Integer.valueOf(System.getenv("PORT"));
    final Server server = new Server(port);
    server.setHandler(context);
    server.start();
    server.join();
  }
}
