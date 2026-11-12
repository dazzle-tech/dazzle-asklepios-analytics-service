package com.dazzle.asklepios.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Draws the intra-procedure vital monitoring readings as an inline SVG, in the style of a paper
 * anesthesia record: blood pressure as a bar from diastolic (∧) to systolic (∨), and heart rate,
 * SpO2 and respiratory rate as lines. All series share one y axis. The legend is left to the
 * template so it can be translated.
 */
@Component
public class ProcedureVitalsChartRenderer {

    static final String HEART_RATE_COLOR = "#dc2626";
    static final String OXYGEN_COLOR = "#2563eb";
    static final String BLOOD_PRESSURE_COLOR = "#334155";
    static final String RESPIRATORY_COLOR = "#16a34a";

    private static final int WIDTH = 760;
    private static final int HEIGHT = 230;
    private static final int PAD_LEFT = 38;
    private static final int PAD_RIGHT = 18;
    private static final int PAD_TOP = 14;
    private static final int PAD_BOTTOM = 30;
    private static final int MAX_TIME_LABELS = 8;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.ENGLISH);

    public record Point(
            Instant recordedAt,
            BigDecimal heartRate,
            BigDecimal oxygenSaturation,
            BigDecimal systolic,
            BigDecimal diastolic,
            BigDecimal respiratoryRate
    ) {
    }

    /**
     * @return the SVG markup, or null when no reading has a value that can be plotted
     */
    public String render(List<Point> points, ZoneId zone) {
        List<Point> sorted = points.stream()
                .filter(p -> p.recordedAt() != null)
                .sorted((a, b) -> a.recordedAt().compareTo(b.recordedAt()))
                .toList();

        List<Double> values = sorted.stream()
                .flatMap(p -> Stream.of(p.heartRate(), p.oxygenSaturation(), p.systolic(), p.diastolic(), p.respiratoryRate()))
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .toList();

        if (values.isEmpty()) {
            return null;
        }

        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double step = (max - min) > 160 ? 40 : 20;
        double yMin = Math.max(0, Math.floor((min - 5) / step) * step);
        double yMax = Math.ceil((max + 5) / step) * step;
        if (yMax <= yMin) {
            yMax = yMin + step;
        }

        double plotWidth = WIDTH - PAD_LEFT - PAD_RIGHT;
        double plotHeight = HEIGHT - PAD_TOP - PAD_BOTTOM;
        long first = sorted.get(0).recordedAt().getEpochSecond();
        long last = sorted.get(sorted.size() - 1).recordedAt().getEpochSecond();
        boolean byTime = last > first;

        double[] xs = new double[sorted.size()];
        for (int i = 0; i < sorted.size(); i++) {
            double ratio;
            if (byTime) {
                ratio = (double) (sorted.get(i).recordedAt().getEpochSecond() - first) / (last - first);
            } else {
                ratio = sorted.size() == 1 ? 0.5 : (double) i / (sorted.size() - 1);
            }
            xs[i] = PAD_LEFT + 10 + ratio * (plotWidth - 20);
        }

        final double low = yMin;
        final double high = yMax;
        Function<Double, Double> y = v -> PAD_TOP + plotHeight - (v - low) / (high - low) * plotHeight;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
                .append(WIDTH).append(' ').append(HEIGHT)
                .append("\" width=\"100%\" preserveAspectRatio=\"xMidYMid meet\" class=\"vitals-chart\" direction=\"ltr\">");

        // Plot background and horizontal grid with y labels
        svg.append(rect(PAD_LEFT, PAD_TOP, plotWidth, plotHeight, "#f8fafc"));
        for (double v = yMin; v <= yMax + 0.001; v += step) {
            double gy = y.apply(v);
            svg.append(line(PAD_LEFT, gy, WIDTH - PAD_RIGHT, gy, "#e2e8f0", 0.8, null));
            svg.append(text(PAD_LEFT - 6, gy + 3, "end", fmt(v)));
        }

        // Time labels on the x axis; readings over more than one day also show the date
        boolean multiDay = !sorted.get(0).recordedAt().atZone(zone).toLocalDate()
                .equals(sorted.get(sorted.size() - 1).recordedAt().atZone(zone).toLocalDate());
        DateTimeFormatter labelFormat = multiDay ? DATE_TIME : TIME;
        double minGap = multiDay ? 64 : 40;
        for (int i : labelIndexes(xs, minGap)) {
            svg.append(line(xs[i], PAD_TOP, xs[i], PAD_TOP + plotHeight, "#eef2f7", 0.6, "2 3"));
            svg.append(text(xs[i], HEIGHT - PAD_BOTTOM + 14, "middle",
                    labelFormat.format(sorted.get(i).recordedAt().atZone(zone))));
        }

        // Blood pressure bars with systolic (v) and diastolic (^) chevrons
        for (int i = 0; i < sorted.size(); i++) {
            Point p = sorted.get(i);
            Double sys = toDouble(p.systolic());
            Double dia = toDouble(p.diastolic());
            if (sys != null && dia != null) {
                svg.append(line(xs[i], y.apply(sys), xs[i], y.apply(dia), BLOOD_PRESSURE_COLOR, 1.4, null));
            }
            if (sys != null) {
                double sy = y.apply(sys);
                svg.append(polyline(List.of(new double[]{xs[i] - 5, sy - 6}, new double[]{xs[i], sy}, new double[]{xs[i] + 5, sy - 6}),
                        BLOOD_PRESSURE_COLOR, 1.8, null));
            }
            if (dia != null) {
                double dy = y.apply(dia);
                svg.append(polyline(List.of(new double[]{xs[i] - 5, dy + 6}, new double[]{xs[i], dy}, new double[]{xs[i] + 5, dy + 6}),
                        BLOOD_PRESSURE_COLOR, 1.8, null));
            }
        }

        appendSeries(svg, sorted, xs, y, Point::respiratoryRate, RESPIRATORY_COLOR, "4 3", "triangle");
        appendSeries(svg, sorted, xs, y, Point::oxygenSaturation, OXYGEN_COLOR, "6 3", "square");
        appendSeries(svg, sorted, xs, y, Point::heartRate, HEART_RATE_COLOR, null, "circle");

        // Plot frame
        svg.append(line(PAD_LEFT, PAD_TOP + plotHeight, WIDTH - PAD_RIGHT, PAD_TOP + plotHeight, "#94a3b8", 1, null));
        svg.append(line(PAD_LEFT, PAD_TOP, PAD_LEFT, PAD_TOP + plotHeight, "#94a3b8", 1, null));
        svg.append("</svg>");
        return svg.toString();
    }

    /**
     * Picks the readings that get an x axis label: the last one always, then earlier ones from
     * right to left whenever they are at least {@code minGap} away from the label to their right.
     */
    static List<Integer> labelIndexes(double[] xs, double minGap) {
        List<Integer> picked = new ArrayList<>();
        double lastX = Double.MAX_VALUE;
        for (int i = xs.length - 1; i >= 0; i--) {
            if (lastX - xs[i] >= minGap) {
                picked.add(0, i);
                lastX = xs[i];
            }
        }
        while (picked.size() > MAX_TIME_LABELS) {
            picked.remove(1);
        }
        return picked;
    }

    private void appendSeries(
            StringBuilder svg,
            List<Point> points,
            double[] xs,
            Function<Double, Double> y,
            Function<Point, BigDecimal> value,
            String color,
            String dash,
            String marker
    ) {
        List<double[]> coords = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            Double v = toDouble(value.apply(points.get(i)));
            if (v != null) {
                coords.add(new double[]{xs[i], y.apply(v)});
            }
        }
        if (coords.size() > 1) {
            svg.append(polyline(coords, color, 1.8, dash));
        }
        for (double[] c : coords) {
            svg.append(marker(marker, c[0], c[1], color));
        }
    }

    private static String marker(String shape, double x, double y, String color) {
        return switch (shape) {
            case "square" -> "<rect x=\"" + fmt(x - 3) + "\" y=\"" + fmt(y - 3)
                    + "\" width=\"6\" height=\"6\" fill=\"#ffffff\" stroke=\"" + color + "\" stroke-width=\"1.6\"/>";
            case "triangle" -> "<polygon points=\"" + fmt(x) + "," + fmt(y - 4) + " " + fmt(x - 4) + "," + fmt(y + 3)
                    + " " + fmt(x + 4) + "," + fmt(y + 3) + "\" fill=\"" + color + "\"/>";
            default -> "<circle cx=\"" + fmt(x) + "\" cy=\"" + fmt(y) + "\" r=\"3.2\" fill=\"" + color
                    + "\" stroke=\"#ffffff\" stroke-width=\"1\"/>";
        };
    }

    private static String rect(double x, double y, double w, double h, String fill) {
        return "<rect x=\"" + fmt(x) + "\" y=\"" + fmt(y) + "\" width=\"" + fmt(w) + "\" height=\"" + fmt(h)
                + "\" fill=\"" + fill + "\"/>";
    }

    private static String line(double x1, double y1, double x2, double y2, String color, double width, String dash) {
        return "<line x1=\"" + fmt(x1) + "\" y1=\"" + fmt(y1) + "\" x2=\"" + fmt(x2) + "\" y2=\"" + fmt(y2)
                + "\" stroke=\"" + color + "\" stroke-width=\"" + fmt(width) + "\""
                + (dash != null ? " stroke-dasharray=\"" + dash + "\"" : "") + "/>";
    }

    private static String polyline(List<double[]> coords, String color, double width, String dash) {
        StringBuilder points = new StringBuilder();
        for (double[] c : coords) {
            if (!points.isEmpty()) {
                points.append(' ');
            }
            points.append(fmt(c[0])).append(',').append(fmt(c[1]));
        }
        return "<polyline points=\"" + points + "\" fill=\"none\" stroke=\"" + color + "\" stroke-width=\"" + fmt(width)
                + "\" stroke-linejoin=\"round\" stroke-linecap=\"round\""
                + (dash != null ? " stroke-dasharray=\"" + dash + "\"" : "") + "/>";
    }

    private static String text(double x, double y, String anchor, String value) {
        return "<text x=\"" + fmt(x) + "\" y=\"" + fmt(y) + "\" text-anchor=\"" + anchor
                + "\" font-size=\"9\" font-family=\"Arial, sans-serif\" fill=\"#64748b\">" + value + "</text>";
    }

    private static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    static String fmt(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
