/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.mbeans;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.haptava.examples.heroku.Constants.getDynoMBeanObjectName;
import static java.lang.String.format;

public class DynoWatcherMBean
    extends NotificationBroadcasterSupport
    implements DynoWatcherMXBean {

  private final AtomicInteger          idGenerator = new AtomicInteger(0);
  private final Map<String, DynoMBean> dynoMap     = Maps.newConcurrentMap();

  private final String applicationId;

  public DynoWatcherMBean(final String applicationId) {
    this.applicationId = applicationId;
  }

  @Override
  public String getApplicationId() { return this.applicationId; }

  @Override
  public String registerDyno(final String hostName) {
    try {
      String name = format("Dyno-%d", idGenerator.incrementAndGet());
      DynoMBean mbean = new DynoMBean(name, hostName);
      this.dynoMap.put(name, mbean);
      ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, new ObjectName(getDynoMBeanObjectName(name)));
      System.out.println(format("%s registered", name));
      return name;
    }
    catch (final Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public boolean unregisterDyno(final String dynoName) {
    try {
      this.dynoMap.remove(dynoName);
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(getDynoMBeanObjectName(dynoName)));
      System.out.println(format("%s unregistered", dynoName));
      return true;
    }
    catch (final Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public int getDynoCount() { return this.dynoMap.size(); }

  @Override
  public List<Dyno> getDynos() {
    return
        FluentIterable
            .from(this.dynoMap.values())
            .transform(new Function<DynoMBean, Dyno>() {
              @Override
              public Dyno apply(final DynoMBean mbean) {
                return new Dyno(mbean.getName(),
                                mbean.getHostName(),
                                mbean.getCreateTimeMillis(),
                                mbean.getLastActivityMillis(),
                                mbean.getRequestCount(),
                                mbean.getRequests(5));
              }
            })
            .toList();
  }

  @Override
  public void resetDynos() {
    for (final DynoMBean mbean : this.dynoMap.values())
      mbean.resetRequests();
  }
}
