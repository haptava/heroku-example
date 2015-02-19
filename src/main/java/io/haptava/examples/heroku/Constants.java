package io.haptava.examples.heroku;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

public class Constants {

  public static final String ADMIN_OBJECT_NAME        = "io.haptava.server.mbeanserver:type=Admin,name=MBeanServerAdmin";
  public static final String EXAMPLE_DOMAIN           = "io.haptava.heroku-example";
  public static final String USERGROUP_JMX_PROXY      = "userGroupJmxProxy";
  public static final String DYNO_WATCHER_JMX_PROXY   = "dynoWatcherJmxProxy";
  public static final String MBEANSERVER_ADMIN_PROXY  = "mbeanServerAdminProxy";
  public static final String DYNO_WATCHER_MBEAN_PROXY = "dynoWatcherProxy";
  public static final String DYNO_PROXY               = "dynoProxy";
  public static final String DYNO_NAME                = "dynoName";
  public static final int    RETRY_SECS               = 15;

  public static String getDynoWatcherLauncherName() {
    return format("heroku-dyno-watcher-%s", getApplicationId());
  }

  public static String getDynoWatcherMBeanObjectName() {
    return format("%s:name=DynoWatcher", EXAMPLE_DOMAIN);
  }

  public static String getDynoMBeanObjectName(final String dynoName) {
    return format("%s:type=Dynos,name=%s", EXAMPLE_DOMAIN, dynoName);
  }

  // A unique application id prevents problems when multiple users from the same usergroup run the app
  // or when a user run multiple instances of the app
  public static String getApplicationId() {
    String id = System.getenv("APPLICATION_ID");
    if (isNullOrEmpty(id))
      return "MISSING";
    else if (id.length() <= 5)
      return id;
    else
      return id.substring(0, 5).toUpperCase();
  }
}
