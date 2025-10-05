package metrics;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Collects runtime and memory metrics for Kadane's algorithm.
 */
public class PerformanceTracker {

    private final List<BenchmarkRecord> records = new ArrayList<>();

    public record BenchmarkRecord(String distribution,
                                  int inputSize,
                                  Duration averageDuration,
                                  Duration minDuration,
                                  Duration maxDuration,
                                  long averageMemoryBytes) {
    }

    public void measure(String distribution,
                        int inputSize,
                        IntFunction<int[]> generator,
                        int warmupIterations,
                        int measurementIterations) {
        Objects.requireNonNull(distribution, "distribution");
        Objects.requireNonNull(generator, "generator");
        if (warmupIterations < 0 || measurementIterations <= 0) {
            throw new IllegalArgumentException("Iterations must be non-negative and measurements positive");
        }

        for (int i = 0; i < warmupIterations; i++) {
            int[] input = generator.apply(inputSize);
            KadaneAlgorithm.maxSubarray(input);
        }

        List<Long> durations = new ArrayList<>(measurementIterations);
        List<Long> memoryUsage = new ArrayList<>(measurementIterations);
        for (int i = 0; i < measurementIterations; i++) {
            int[] input = generator.apply(inputSize);
            long beforeMemory = usedMemory();
            long start = System.nanoTime();
            Result result = KadaneAlgorithm.maxSubarray(input);
            long duration = System.nanoTime() - start;
            long afterMemory = usedMemory();
            Objects.requireNonNull(result, "result");
            durations.add(duration);
            memoryUsage.add(Math.max(0, afterMemory - beforeMemory));
        }

        DoubleSummaryStatistics durationStats = durations.stream()
                .mapToDouble(Long::doubleValue)
                .summaryStatistics();
        DoubleSummaryStatistics memoryStats = memoryUsage.stream()
                .mapToDouble(Long::doubleValue)
                .summaryStatistics();

        records.add(new BenchmarkRecord(
                distribution,
                inputSize,
                Duration.ofNanos(Math.round(durationStats.getAverage())),
                Duration.ofNanos((long) durationStats.getMin()),
                Duration.ofNanos((long) durationStats.getMax()),
                Math.round(memoryStats.getAverage())));
    }

    public List<BenchmarkRecord> records() {
        return Collections.unmodifiableList(records);
    }

    public void exportCsv(Path path) {
        Objects.requireNonNull(path, "path");
        List<String> lines = new ArrayList<>(records.size() + 1);
        lines.add("distribution,input_size,average_ns,min_ns,max_ns,avg_memory_bytes");
        for (BenchmarkRecord record : records) {
            lines.add(String.format(Locale.ROOT,
                    "%s,%d,%d,%d,%d,%d",
                    record.distribution(),
                    record.inputSize(),
                    record.averageDuration().toNanos(),
                    record.minDuration().toNanos(),
                    record.maxDuration().toNanos(),
                    record.averageMemoryBytes()));
        }
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write metrics CSV", exception);
        }
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}