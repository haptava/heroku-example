/*
 * Copyright (c) 2015 Paul Ambrose
 */

package io.haptava.examples.heroku.mbeans;

import java.beans.ConstructorProperties;
import java.util.Date;

import static java.lang.String.format;

public class Request {

  private final String url;
  private final long   requestTime;

  @ConstructorProperties({"url", "requestTime"})
  public Request(final String url, final long requestTime) {
    this.url = url;
    this.requestTime = requestTime;
  }

  public String getUrl() { return this.url; }

  public long getRequestTime() { return this.requestTime; }

  @Override
  public String toString() {
    return format("%s -- %s", new Date(this.getRequestTime()), this.getUrl());
  }
}
