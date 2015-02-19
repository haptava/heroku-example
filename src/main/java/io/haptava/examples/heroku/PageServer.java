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

    int port = Integer.valueOf(System.getenv("PORT"));

    new Server(port) {{
      setHandler(
          new Context(Context.SESSIONS) {{
            setContextPath("/");
            addEventListener(new DynoContextListener());
            addServlet(new ServletHolder(new ResetServlet()), "/reset");
            addServlet(new ServletHolder(new SummaryServlet()), "/summary");
            addServlet(new ServletHolder(new DefaultServlet()), "/*");
          }});
      start();
      join();
    }};
  }
}
