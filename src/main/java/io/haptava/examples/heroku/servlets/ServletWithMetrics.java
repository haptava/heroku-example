package io.haptava.examples.heroku.servlets;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;

import javax.servlet.http.HttpServlet;
import java.util.concurrent.TimeUnit;

public abstract class ServletWithMetrics
    extends HttpServlet {

  private static final String PREFIX = "servlets";

  private final Counter   requestCounter;
  private final Meter     requestMeter;
  private final Histogram requestHistogram;

  protected ServletWithMetrics(final MetricRegistry metricRegistry, final String name) {
    this.requestCounter = metricRegistry.counter(MetricRegistry.name(PREFIX, name, "requestCounter"));
    this.requestMeter = metricRegistry.meter(MetricRegistry.name(PREFIX, name, "requestMeter"));
    this.requestHistogram = metricRegistry.histogram(MetricRegistry.name(PREFIX, name, "requestHistogram"));
  }

  protected void markMetrics(final Stopwatch sw) {
    this.requestCounter.inc();
    this.requestMeter.mark();
    this.requestHistogram.update(sw.elapsed(TimeUnit.MILLISECONDS));
  }
}
