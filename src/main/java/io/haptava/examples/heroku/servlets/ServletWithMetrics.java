package io.haptava.examples.heroku.servlets;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import javax.servlet.http.HttpServlet;

public abstract class ServletWithMetrics
    extends HttpServlet {

  private final MetricRegistry metricRegistry;
  private final Counter        requestCounter;
  private final Meter          requestMeter;
  private final Histogram      requestHistogram;

  public ServletWithMetrics(final MetricRegistry metricRegistry, final String name) {
    this.metricRegistry = metricRegistry;
    this.requestCounter = metricRegistry.counter(MetricRegistry.name("servlets", name, "requestCounter"));
    this.requestMeter = metricRegistry.meter(MetricRegistry.name("servlets", name, "requestMeter"));
    this.requestHistogram = metricRegistry.histogram(MetricRegistry.name("servlets", name, "requestHistogram"));
  }

  protected MetricRegistry getMetricRegistry() { return this.metricRegistry; }

  protected Counter getRequestCounter() { return this.requestCounter; }

  public Meter getRequestMeter() { return this.requestMeter; }

  protected Histogram getRequestHistogram() { return this.requestHistogram; }
}
