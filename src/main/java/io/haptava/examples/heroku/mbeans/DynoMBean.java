/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.mbeans;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import javax.management.NotificationBroadcasterSupport;
import java.util.List;

public class DynoMBean
    extends NotificationBroadcasterSupport
    implements DynoMXBean {

  private final List<Request> requests = Lists.newArrayList();

  private final long createTimeMillis = System.currentTimeMillis();

  private long lastActivityMillis = 0;

  private final String name;
  private final String hostName;

  public DynoMBean(final String name, final String hostName) {
    this.name = name;
    this.hostName = hostName;
  }

  @Override
  public String getName() { return this.name; }

  @Override
  public String getHostName() { return this.hostName; }

  @Override
  public long getCreateTimeMillis() { return this.createTimeMillis; }

  @Override
  public long getLastActivityMillis() { return this.lastActivityMillis; }

  @Override
  public void recordRequest(final String url) {
    this.lastActivityMillis = System.currentTimeMillis();
    this.requests.add(new Request(url, System.currentTimeMillis()));
  }

  @Override
  public int getRequestCount() { return this.requests.size(); }

  @Override
  public List<Request> getRequests() { return Lists.reverse(this.requests); }

  @Override
  public List<Request> getRequests(final int limit) {
    return
        FluentIterable
            .from(this.getRequests())
            .limit(limit < 0 ? 0 : limit)
            .toList();
  }

  @Override
  public void resetRequests() { this.requests.clear(); }
}
