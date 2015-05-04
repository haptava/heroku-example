package io.haptava.examples.heroku.servlets;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.sudothought.http.HttpConstants;
import com.sudothought.util.IoUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class HtmlPage
    extends ServletWithMetrics {

  private static Map<String, String> FILE_MAP = Maps.newConcurrentMap();

  private final String filename;

  public HtmlPage(final MetricRegistry metricRegistry, final String metricName, final String filename) {
    super(metricRegistry, metricName);
    this.filename = filename;
  }

  private String getFile(final String filename)
      throws IOException {
    if (!FILE_MAP.containsKey(filename))
      FILE_MAP.putIfAbsent(filename, IoUtils.readFile(this.filename));
    return FILE_MAP.get(filename);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

    final Stopwatch sw = Stopwatch.createStarted();

    response.setContentType(HttpConstants.HTML_CONTENT);
    response.setHeader(HttpHeaders.CACHE_CONTROL, HttpConstants.NO_CACHE);
    response.setStatus(HttpServletResponse.SC_OK);

    try {
      final PrintWriter writer = response.getWriter();
      final String file = this.getFile(this.filename);
      writer.print(file);
      writer.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    this.markMetrics(sw);
  }
}
