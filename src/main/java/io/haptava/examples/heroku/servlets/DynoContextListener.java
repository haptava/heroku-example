/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.servlets;

import com.sudothought.config.ConfigurationException;
import com.sudothought.util.TimeUtils;
import com.sudothought.util.Utils;
import io.haptava.api.common.LauncherException;
import io.haptava.api.jmxproxy.JmxProxy;
import io.haptava.api.jmxproxy.JmxProxyLauncher;
import io.haptava.examples.heroku.mbeans.DynoMXBean;
import io.haptava.examples.heroku.mbeans.DynoWatcherMXBean;
import io.haptava.mbeans.server.mbeanserver.MBeanServerAdminMXBean;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.haptava.examples.heroku.Constants.*;
import static java.lang.String.format;

public class DynoContextListener
    implements ServletContextListener {

  private JmxProxyLauncher       launcher              = null;
  private JmxProxy               userGroupJmxProxy     = null;
  private JmxProxy               dynoWatcherJmxProxy   = null;
  private MBeanServerAdminMXBean mbeanServerAdminProxy = null;
  private DynoWatcherMXBean      dynoWatcherProxy      = null;
  private DynoMXBean             dynoProxy             = null;
  private String                 dynoName              = null;

  public DynoContextListener() { }

  @Override
  public void contextInitialized(final ServletContextEvent contextEvent) {
    // Run the Haptava setup in a thread to avoid blocking dyno setup
    ExecutorService execService = Executors.newSingleThreadExecutor();
    execService.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              initLauncher();
              initUserGroupProxy();
              initDynoProxies();

              // Make values available to servlets
              contextEvent.getServletContext().setAttribute(USERGROUP_JMX_PROXY, userGroupJmxProxy);
              contextEvent.getServletContext().setAttribute(MBEANSERVER_ADMIN_PROXY, mbeanServerAdminProxy);
              contextEvent.getServletContext().setAttribute(DYNO_WATCHER_JMX_PROXY, dynoWatcherJmxProxy);
              contextEvent.getServletContext().setAttribute(DYNO_WATCHER_MBEAN_PROXY, dynoWatcherProxy);
              contextEvent.getServletContext().setAttribute(DYNO_PROXY, dynoProxy);
              contextEvent.getServletContext().setAttribute(DYNO_NAME, dynoName);
            }
            catch (final Exception e) {
              System.out.println(format("Unable to initialize [%s: %s]", e.getClass().getSimpleName(), e.getMessage()));
            }
          }
        });
  }

  private void initLauncher()
      throws ConfigurationException, LauncherException {
    // Credentials are with set Heroku config vars HAPTAVA_USERNAME and HAPTAVA_PASSWORD
    this.launcher =
        new JmxProxyLauncher.Builder()
            .setLauncherName(format("heroku-dyno-page-%s", getApplicationId()))
            .setUniqueLauncherName(true)
            .build();

    // Heroku terminates dynos with SIGTERM
    // Disable the launcher shutdownhook and instead run it below after calling cleanUp()
    this.launcher.setShutDownHookEnabled(false);

    // Deal with SIGTERM sent by Heroku during shutdown
    Runtime.getRuntime()
           .addShutdownHook(
               new Thread("DynoContextListenerShutDownHook") {
                 @Override
                 public void run() {
                   System.out.println("DynoContextListener shutdown hook invoked");
                   cleanUp();
                   // Call launcher shutdown hook actions disabled above
                   launcher.onShutDownHookInvoked();
                 }
               });

    // Connect to server
    this.launcher.connect();

    while (!this.launcher.getLauncherConnectedMonitor().waitUntilTrue(RETRY_SECS))
      System.out.println("Waiting for JmxProxyLauncher to connect...");
  }

  private void initUserGroupProxy()
      throws IOException, MalformedObjectNameException {

    // Create JmxProxy for UserGroup MBeanServer
    this.userGroupJmxProxy = this.launcher.startProxy("usergroup@haptava", 8001);

    // Wait for JmxProxy to start
    while (!this.userGroupJmxProxy.getStartedMonitor().waitUntilTrue(RETRY_SECS))
      System.out.println("Waiting for UserGroup JmxProxy to start...");

    // Wait for MBeanServer to connect
    while (!this.userGroupJmxProxy.isMBeanServerConnected()) {
      System.out.println("Waiting for UserGroup MBeanServer to connect...");
      TimeUtils.secondsOfSleep(RETRY_SECS);
    }

    // Connect to JmxProxy at service:jmx:rmi:///jndi/rmi://:8001/jmxrmi
    JMXServiceURL userGroupUrl = new JMXServiceURL(this.userGroupJmxProxy.getServiceUrl());
    final MBeanServerConnection connection = JMXConnectorFactory.connect(userGroupUrl)
                                                                .getMBeanServerConnection();

    // Create proxy for MBeanServerAdmin MBean
    this.mbeanServerAdminProxy = JMX.newMXBeanProxy(connection,
                                                    new ObjectName(ADMIN_OBJECT_NAME),
                                                    MBeanServerAdminMXBean.class);
    // Verfiy that it is working
    this.mbeanServerAdminProxy.getServerCount();
  }

  private void initDynoProxies()
      throws IOException, MalformedObjectNameException {

    // Create JmxProxy for DynoWatcherServer platform MBeanServer
    // The server query uses the ":>>" suffix to match the newest one
    final String serverQuery = format("platform@%s-*:>>", getDynoWatcherLauncherName());
    this.dynoWatcherJmxProxy = this.launcher.startProxy(serverQuery, 8002);

    // Wait for JmxProxy to start
    while (!this.dynoWatcherJmxProxy.getStartedMonitor().waitUntilTrue(RETRY_SECS))
      System.out.println("Waiting for DynoWatcher JmxProxy to start...");

    // Wait for MBeanServer to connect
    while (!this.dynoWatcherJmxProxy.isMBeanServerConnected()) {
      System.out.println("Waiting for DynoWatcher MBeanServer to connect...");
      TimeUtils.secondsOfSleep(RETRY_SECS);
    }

    // Connect to JmxProxy at service:jmx:rmi:///jndi/rmi://:8002/jmxrmi
    JMXServiceURL dynoWatcherUrl = new JMXServiceURL(this.dynoWatcherJmxProxy.getServiceUrl());
    final MBeanServerConnection connection = JMXConnectorFactory.connect(dynoWatcherUrl)
                                                                .getMBeanServerConnection();

    // Create proxy for DynoWatcher MBean
    this.dynoWatcherProxy = JMX.newMXBeanProxy(connection,
                                               new ObjectName(getDynoWatcherMBeanObjectName()),
                                               DynoWatcherMXBean.class);

    // Register new dyno with DynoWatcherServer, which will create a Dyno MBean on the DynoWatcher platform MBeanServer
    this.dynoName = this.dynoWatcherProxy.registerDyno(Utils.getHostName());

    // Create proxy for newly-created Dyno MBean
    this.dynoProxy = JMX.newMXBeanProxy(connection,
                                        new ObjectName(getDynoMBeanObjectName(this.dynoName)),
                                        DynoMXBean.class);

    // Verfiy that it is working
    this.dynoProxy.getHostName();
  }

  private void cleanUp() {
    if (this.dynoWatcherProxy != null && this.dynoName != null) {
      try {
        this.dynoWatcherProxy.unregisterDyno(this.dynoName);
        System.out.println(format("Unregistered %s", this.dynoName));
      }
      catch (final Exception e) {
        e.printStackTrace();
      }
    }

    if (this.userGroupJmxProxy != null) {
      try {
        this.userGroupJmxProxy.close();
      }
      catch (final LauncherException e) {
        e.printStackTrace();
      }
    }

    if (this.dynoWatcherJmxProxy != null) {
      try {
        this.dynoWatcherJmxProxy.close();
      }
      catch (final LauncherException e) {
        e.printStackTrace();
      }
    }

    if (this.launcher != null)
      this.launcher.close();
  }

  @Override
  public void contextDestroyed(final ServletContextEvent servletContextEvent) {
    // Not called in heroku env
  }
}
