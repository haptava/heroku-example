/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.mbeans;

import javax.management.MXBean;
import java.util.List;

@MXBean
public interface DynoWatcherMXBean {

  String getApplicationId();

  String registerDyno(String hostName);

  boolean unregisterDyno(String dynoName);

  int getDynoCount();

  List<Dyno> getDynos();

  void resetDynos();

}
