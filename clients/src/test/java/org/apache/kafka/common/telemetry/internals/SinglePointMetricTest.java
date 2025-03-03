/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.common.telemetry.internals;

import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SinglePointMetricTest {

    private MetricKey metricKey;
    private Instant now;

    /*
     Test compares the metric representation from returned builder to ensure that the metric is
     constructed correctly.

     For example: Gauge metric with name "name" and double value 1.0 at certain time is represented as:

       name: "name"
          gauge {
            data_points {
              time_unix_nano: 1698063981021420000
              as_double: 1.0
            }
          }
     */

    @BeforeEach
    public void setUp() {
        metricKey = new MetricKey("name", Collections.emptyMap());
        now = Instant.now();
    }

    @Test
    public void testGaugeWithNumberValue() {
        SinglePointMetric gaugeNumber = SinglePointMetric.gauge(metricKey, Long.valueOf(1), now);
        MetricKey metricKey = gaugeNumber.key();
        assertEquals("name", metricKey.name());

        Metric metric = gaugeNumber.builder().build();
        assertEquals(1, metric.getGauge().getDataPointsCount());

        NumberDataPoint point = metric.getGauge().getDataPoints(0);
        assertEquals(now.getEpochSecond() * Math.pow(10, 9) + now.getNano(), point.getTimeUnixNano());
        assertEquals(0, point.getStartTimeUnixNano());
        assertEquals(1, point.getAsInt());
        assertEquals(0, point.getAttributesCount());
    }

    @Test
    public void testGaugeWithDoubleValue() {
        SinglePointMetric gaugeNumber = SinglePointMetric.gauge(metricKey, 1.0, now);
        MetricKey metricKey = gaugeNumber.key();
        assertEquals("name", metricKey.name());

        Metric metric = gaugeNumber.builder().build();
        assertEquals(1, metric.getGauge().getDataPointsCount());

        NumberDataPoint point = metric.getGauge().getDataPoints(0);
        assertEquals(now.getEpochSecond() * Math.pow(10, 9) + now.getNano(), point.getTimeUnixNano());
        assertEquals(0, point.getStartTimeUnixNano());
        assertEquals(1.0, point.getAsDouble());
        assertEquals(0, point.getAttributesCount());
    }

    @Test
    public void testGaugeWithMetricTags() {
        MetricKey metricKey = new MetricKey("name", Collections.singletonMap("tag", "value"));
        SinglePointMetric gaugeNumber = SinglePointMetric.gauge(metricKey, 1.0, now);

        MetricKey key = gaugeNumber.key();
        assertEquals("name", key.name());

        Metric metric = gaugeNumber.builder().build();
        assertEquals(1, metric.getGauge().getDataPointsCount());

        NumberDataPoint point = metric.getGauge().getDataPoints(0);
        assertEquals(now.getEpochSecond() * Math.pow(10, 9) + now.getNano(), point.getTimeUnixNano());
        assertEquals(0, point.getStartTimeUnixNano());
        assertEquals(1.0, point.getAsDouble());
        assertEquals(1, point.getAttributesCount());
        assertEquals("tag", point.getAttributes(0).getKey());
        assertEquals("value", point.getAttributes(0).getValue().getStringValue());
    }

    @Test
    public void testSum() {
        SinglePointMetric sum = SinglePointMetric.sum(metricKey, 1.0, false, now);

        MetricKey key = sum.key();
        assertEquals("name", key.name());

        Metric metric = sum.builder().build();
        assertFalse(metric.getSum().getIsMonotonic());
        assertEquals(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE, metric.getSum().getAggregationTemporality());
        assertEquals(1, metric.getSum().getDataPointsCount());

        NumberDataPoint point = metric.getSum().getDataPoints(0);
        assertEquals(now.getEpochSecond() * Math.pow(10, 9) + now.getNano(), point.getTimeUnixNano());
        assertEquals(0, point.getStartTimeUnixNano());
        assertEquals(1.0, point.getAsDouble());
        assertEquals(0, point.getAttributesCount());
    }

    @Test
    public void testSumWithStartTimeAndTags() {
        MetricKey metricKey = new MetricKey("name", Collections.singletonMap("tag", "value"));
        SinglePointMetric sum = SinglePointMetric.sum(metricKey, 1.0, true, now, now);

        MetricKey key = sum.key();
        assertEquals("name", key.name());

        Metric metric = sum.builder().build();
        assertTrue(metric.getSum().getIsMonotonic());
        assertEquals(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE, metric.getSum().getAggregationTemporality());
        assertEquals(1, metric.getSum().getDataPointsCount());

        NumberDataPoint point = metric.getSum().getDataPoints(0);
        assertEquals(now.getEpochSecond() * Math.pow(10, 9) + now.getNano(), point.getTimeUnixNano());
        assertEquals(now.getEpochSecond() * Math.pow(10, 9) + now.getNano(), point.getStartTimeUnixNano());
        assertEquals(1.0, point.getAsDouble());
        assertEquals(1, point.getAttributesCount());
        assertEquals("tag", point.getAttributes(0).getKey());
        assertEquals("value", point.getAttributes(0).getValue().getStringValue());
    }

    @Test
    public void testDeltaSum() {
        SinglePointMetric sum = SinglePointMetric.deltaSum(metricKey, 1.0, true, now, now);

        MetricKey key = sum.key();
        assertEquals("name", key.name());

        Metric metric = sum.builder().build();
        assertTrue(metric.getSum().getIsMonotonic());
        assertEquals(AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA, metric.getSum().getAggregationTemporality());
        assertEquals(1, metric.getSum().getDataPointsCount());

        NumberDataPoint point = metric.getSum().getDataPoints(0);
        assertEquals(now.getEpochSecond() * Math.pow(10, 9) + now.getNano(), point.getTimeUnixNano());
        assertEquals(now.getEpochSecond() * Math.pow(10, 9) + now.getNano(), point.getStartTimeUnixNano());
        assertEquals(1.0, point.getAsDouble());
        assertEquals(0, point.getAttributesCount());
    }
}
