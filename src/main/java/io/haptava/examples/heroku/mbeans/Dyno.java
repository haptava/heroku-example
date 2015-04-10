/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.mbeans;

import java.beans.ConstructorProperties;
import java.util.List;

public class Dyno {

  private final String        name;
  private final String        hostName;
  private final long          createTimeMillis;
  private final long          lastActivityMillis;
  private final long          requestCount;
  private final List<Request> requests;

  // @ConstructorProperties required for MXBeans
  @ConstructorProperties({"name", "hostName", "createTimeMillis", "lastActivityMillis", "requestCount", "requests"})
  public Dyno(final String name,
              final String hostName,
              final long createTimeMillis,
              final long lastActivityMillis,
              final long requestCount,
              final List<Request> requests) {
    this.name = name;
    this.hostName = hostName;
    this.createTimeMillis = createTimeMillis;
    this.lastActivityMillis = lastActivityMillis;
    this.requestCount = requestCount;
    this.requests = requests;
  }

  public String getName() { return this.name; }

  public String getHostName() { return this.hostName; }

  public long getCreateTimeMillis() { return this.createTimeMillis; }

  public long getLastActivityMillis() { return this.lastActivityMillis; }

  public long getRequestCount() { return this.requestCount; }

  public List<Request> getRequests() { return this.requests; }
}
