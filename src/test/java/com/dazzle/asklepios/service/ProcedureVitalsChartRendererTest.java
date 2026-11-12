package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.ProcedureVitalsChartRenderer.Point;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcedureVitalsChartRendererTest {

    private final ProcedureVitalsChartRenderer renderer = new ProcedureVitalsChartRenderer();
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void noPlottableValues_returnsNull() {
        assertNull(renderer.render(List.of(), UTC));
        assertNull(renderer.render(List.of(new Point(Instant.now(), null, null, null, null, null)), UTC));
    }

    @Test
    void singleReading_drawsMarkersWithoutLines() {
        String svg = renderer.render(List.of(point("2026-11-05T10:00:00Z", 80, 98, 120, 80, 16)), UTC);

        assertTrue(svg.startsWith("<svg"));
        assertTrue(svg.contains(ProcedureVitalsChartRenderer.HEART_RATE_COLOR));
        assertTrue(svg.contains(">10:00<"));
        assertFalse(svg.contains("stroke-dasharray=\"6 3\""), "One SpO2 reading has no line to draw");
    }

    @Test
    void readingsAreSortedByTime_andSeriesBecomeLines() {
        String svg = renderer.render(List.of(
                point("2026-11-05T10:30:00Z", 90, 96, 130, 85, 18),
                point("2026-11-05T10:00:00Z", 80, 98, 120, 80, 16)
        ), UTC);

        assertTrue(svg.indexOf(">10:00<") < svg.indexOf(">10:30<"));
        assertTrue(svg.contains("<polyline"));
    }

    @Test
    void readingsOverSeveralDays_showTheDateInLabels() {
        String svg = renderer.render(List.of(
                point("2026-11-05T10:00:00Z", 80, 98, 120, 80, 16),
                point("2026-11-07T08:00:00Z", 82, 97, 118, 78, 15)
        ), UTC);

        assertTrue(svg.contains(">05/11 10:00<"));
        assertTrue(svg.contains(">07/11 08:00<"));
    }

    @Test
    void labelIndexes_keepTheLastLabelAndSkipCrowdedOnes() {
        double[] xs = {50, 60, 70, 300, 700};

        List<Integer> picked = ProcedureVitalsChartRenderer.labelIndexes(xs, 40);

        assertEquals(List.of(2, 3, 4), picked);
    }

    private static Point point(String at, int hr, int spo2, int sbp, int dbp, int rr) {
        return new Point(Instant.parse(at), BigDecimal.valueOf(hr), BigDecimal.valueOf(spo2),
                BigDecimal.valueOf(sbp), BigDecimal.valueOf(dbp), BigDecimal.valueOf(rr));
    }
}
