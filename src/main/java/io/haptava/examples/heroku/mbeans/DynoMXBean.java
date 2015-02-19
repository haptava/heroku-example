/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.mbeans;

import javax.management.MXBean;
import java.util.List;

@MXBean
public interface DynoMXBean {

  String getName();

  String getHostName();

  long getCreateTimeMillis();

  long getLastActivityMillis();

  void recordRequest(String url);

  int getRequestCount();

  List<Request> getRequests();

  List<Request> getRequests(int limit);

  void resetRequests();
}
