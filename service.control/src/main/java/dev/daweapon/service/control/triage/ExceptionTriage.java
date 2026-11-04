package dev.daweapon.service.control.triage;

import dev.daweapon.service.control.logs.LogNoiseFilter;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Groups exceptions and ERROR lines from container logs by signature.
 *
 * A signature is the exception class plus the first stack frame inside the project's own
 * packages, so the same bug thrown from every game server collapses into one row however many
 * times it fired. Line numbers are left out of the frame: they shift with every build, and the
 * point is to see one bug across deploys. An ERROR line with no stack trace is keyed by its
 * message with numbers, ids and hex normalised away.
 *
 * Fed one container's log text at a time; holds no I/O.
 */
public final class ExceptionTriage {
    public static final List<String> DEFAULT_PROJECT_PACKAGES = List.of("dev.daweapon.", "net.swofty.");
    private static final int SAMPLE_LINES = 30;

    private static final Pattern DOCKER_TIMESTAMP = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\S+Z) (.*)$");
    private static final Pattern EXCEPTION_CLASS = Pattern.compile(
            "(?:^|[\\s:(\\[\"])((?:[a-zA-Z_$][\\w$]*\\.)+[A-Z][\\w$]*(?:Exception|Error|Throwable))(?=$|[:\\s;,)])");
    private static final Pattern FRAME = Pattern.compile("^\\s*at\\s+(?:[\\w.$-]+/)?([\\w$.<>]+)\\.([\\w$<>]+)\\(([^)]*)\\)");
    private static final Pattern CONTINUATION = Pattern.compile("^\\s*(Caused by:|Suppressed:|\\.\\.\\. \\d+ more)");
    private static final Pattern ERROR_TOKEN = Pattern.compile("\\bERROR\\b|\\bSEVERE\\b");
    private static final Pattern LEADING_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d+)?\\s*");
    private static final Pattern THREAD = Pattern.compile("^\\[[^]]*]\\s*");
    private static final Pattern UUID = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern HEX = Pattern.compile("\\b(?:0x)?[0-9a-fA-F]{8,}\\b");
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    public enum Kind {
        EXCEPTION,
        ERROR
    }

    public record Group(String signature, Kind kind, String exceptionClass, String frame, String message,
                        int count, Map<String, Integer> perContainer, Instant firstSeen, Instant lastSeen,
                        String lastContainer, List<String> sample) {
    }

    public record Report(Instant scannedAt, int linesScanned, int totalEvents,
                         Map<String, Integer> perContainer, List<Group> groups) {
    }

    private final List<String> projectPackages;
    private final boolean includeNoise;
    private final Map<String, Accumulator> groups = new HashMap<>();
    private final Map<String, Integer> perContainer = new TreeMap<>();
    private int linesScanned;
    private int totalEvents;

    public ExceptionTriage(List<String> projectPackages, boolean includeNoise) {
        this.projectPackages = projectPackages.isEmpty() ? DEFAULT_PROJECT_PACKAGES : projectPackages;
        this.includeNoise = includeNoise;
    }

    private static final class Event {
        final String container;
        final Instant at;
        final boolean error;
        final String headline;
        final List<String> lines = new ArrayList<>();
        String exceptionClass;
        String topFrame;
        String projectFrame;
        int frames;

        Event(String container, Instant at, boolean error, String exceptionClass, String line) {
            this.container = container;
            this.at = at;
            this.error = error;
            this.exceptionClass = exceptionClass;
            this.headline = line;
            lines.add(line);
        }
    }

    private static final class Accumulator {
        final String signature;
        final Kind kind;
        final String exceptionClass;
        final String frame;
        final String message;
        final Map<String, Integer> perContainer = new TreeMap<>();
        int count;
        Instant firstSeen;
        Instant lastSeen;
        String lastContainer;
        List<String> sample;

        Accumulator(String signature, Kind kind, String exceptionClass, String frame, String message) {
            this.signature = signature;
            this.kind = kind;
            this.exceptionClass = exceptionClass;
            this.frame = frame;
            this.message = message;
        }
    }

    public void scan(String container, String logText) {
        Event current = null;
        for (String raw : logText.split("\\R")) {
            if (raw.isBlank()) {
                continue;
            }
            linesScanned++;
            Instant at = null;
            String line = raw;
            Matcher stamp = DOCKER_TIMESTAMP.matcher(raw);
            if (stamp.matches()) {
                at = parseInstant(stamp.group(1));
                line = stamp.group(2);
            }

            Matcher frame = FRAME.matcher(line);
            if (frame.find()) {
                if (current != null) {
                    addFrame(current, frame.group(1) + "." + frame.group(2), line);
                }
                continue;
            }
            if (CONTINUATION.matcher(line).find()) {
                if (current != null && current.lines.size() < SAMPLE_LINES) {
                    current.lines.add(line);
                }
                continue;
            }

            boolean error = ERROR_TOKEN.matcher(line).find();
            Matcher exception = EXCEPTION_CLASS.matcher(line);
            String exceptionClass = exception.find() ? exception.group(1) : null;
            if (!error && exceptionClass == null) {
                close(current);
                current = null;
                continue;
            }

            // tinylog and similar loggers print "ERROR: message" and then the exception on the
            // next line; that is one event, not two.
            if (current != null && !error && exceptionClass != null && current.exceptionClass == null
                    && current.frames == 0 && current.lines.size() <= 2) {
                current.exceptionClass = exceptionClass;
                current.lines.add(line);
                continue;
            }

            close(current);
            current = new Event(container, at, error, exceptionClass, line);
        }
        close(current);
    }

    private void addFrame(Event event, String frame, String line) {
        event.frames++;
        if (event.topFrame == null) {
            event.topFrame = frame;
        }
        if (event.projectFrame == null && projectPackages.stream().anyMatch(frame::startsWith)) {
            event.projectFrame = frame;
        }
        if (event.lines.size() < SAMPLE_LINES) {
            event.lines.add(line);
        }
    }

    /** Only stack traces and ERROR lines count; a WARN that merely mentions an exception does not. */
    private void close(Event event) {
        if (event == null || (!event.error && event.frames == 0)) {
            return;
        }
        if (!includeNoise && LogNoiseFilter.isNoise(String.join("\n", event.lines))) {
            return;
        }

        String signature;
        Accumulator accumulator;
        if (event.exceptionClass != null) {
            String frame = event.projectFrame != null ? event.projectFrame : event.topFrame;
            signature = event.exceptionClass + " @ " + (frame == null ? "no stack frame" : frame);
            accumulator = groups.computeIfAbsent(signature,
                    key -> new Accumulator(key, Kind.EXCEPTION, event.exceptionClass, frame, null));
        } else {
            String message = normalise(event.headline);
            signature = "ERROR " + message;
            accumulator = groups.computeIfAbsent(signature,
                    key -> new Accumulator(key, Kind.ERROR, null, null, message));
        }

        accumulator.count++;
        accumulator.perContainer.merge(event.container, 1, Integer::sum);
        if (event.at != null && (accumulator.firstSeen == null || event.at.isBefore(accumulator.firstSeen))) {
            accumulator.firstSeen = event.at;
        }
        if (accumulator.lastSeen == null || (event.at != null && !event.at.isBefore(accumulator.lastSeen))) {
            accumulator.lastSeen = event.at;
            accumulator.lastContainer = event.container;
            accumulator.sample = List.copyOf(event.lines);
        }
        if (accumulator.sample == null) {
            accumulator.sample = List.copyOf(event.lines);
            accumulator.lastContainer = event.container;
        }
        perContainer.merge(event.container, 1, Integer::sum);
        totalEvents++;
    }

    public static String normalise(String line) {
        String text = LEADING_DATE.matcher(line.strip()).replaceFirst("");
        text = THREAD.matcher(text).replaceFirst("");
        text = UUID.matcher(text).replaceAll("<uuid>");
        text = HEX.matcher(text).replaceAll("<hex>");
        text = DIGITS.matcher(text).replaceAll("#");
        text = text.replaceAll("\\s+", " ").trim();
        return text.length() > 160 ? text.substring(0, 160) + "..." : text;
    }

    public Report report() {
        List<Group> result = new ArrayList<>();
        for (Accumulator a : groups.values()) {
            result.add(new Group(a.signature, a.kind, a.exceptionClass, a.frame, a.message, a.count,
                    new LinkedHashMap<>(a.perContainer), a.firstSeen, a.lastSeen, a.lastContainer, a.sample));
        }
        result.sort(Comparator.comparingInt(Group::count).reversed()
                .thenComparing(Group::lastSeen, Comparator.nullsLast(Comparator.reverseOrder())));
        return new Report(Instant.now(), linesScanned, totalEvents, new LinkedHashMap<>(perContainer), result);
    }

    private static Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
