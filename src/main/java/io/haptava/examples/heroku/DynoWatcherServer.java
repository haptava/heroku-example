/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku;

import com.sudothought.util.TimeUtils;
import io.haptava.api.jmxconnector.JmxConnectorLauncher;
import io.haptava.examples.heroku.mbeans.DynoWatcherMBean;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static io.haptava.examples.heroku.Constants.RETRY_SECS;
import static io.haptava.examples.heroku.Constants.getApplicationId;
import static io.haptava.examples.heroku.Constants.getDynoWatcherLauncherName;
import static io.haptava.examples.heroku.Constants.getDynoWatcherMBeanObjectName;

public class DynoWatcherServer {

  public static void main(String[] argv) {
    try {
      // Register DynoWatcherMBean
      ManagementFactory.getPlatformMBeanServer()
                       .registerMBean(new DynoWatcherMBean(getApplicationId()),
                                      new ObjectName(getDynoWatcherMBeanObjectName()));

      // Credentials are with set Heroku config vars HAPTAVA_USERNAME and HAPTAVA_PASSWORD
      JmxConnectorLauncher launcher =
          new JmxConnectorLauncher.Builder()
              .setLauncherName(getDynoWatcherLauncherName())
              .setLoginUrl("http://localhost:8090")
              .setUniqueLauncherName(true)
              .build();

      // Connect to server
      launcher.connect();

      // Wait until launcher connects
      while (!launcher.getLauncherConnectedMonitor().waitUntilTrue(RETRY_SECS))
        System.out.println("Waiting for JmxConnectorLauncher to connect...");
      System.out.println("JmxConnectorLauncher connected");

      TimeUtils.secondsOfSleep(Integer.MAX_VALUE);
    }
    catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
