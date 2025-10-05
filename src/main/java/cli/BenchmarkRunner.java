package cli;


import metrics.PerformanceTracker;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.function.IntFunction;

public final class BenchmarkRunner {

    private BenchmarkRunner() {
    }

    public static void main(String[] args) {
        BenchmarkArguments arguments = BenchmarkArguments.parse(args);
        PerformanceTracker tracker = new PerformanceTracker();
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.collectBenchmarks(arguments, tracker);
        if (arguments.outputPath() != null) {
            tracker.exportCsv(arguments.outputPath());
        }
        tracker.records().forEach(record -> System.out.printf(Locale.ROOT,
                "%s,%d,%dns%n",
                record.distribution(),
                record.inputSize(),
                record.averageDuration().toNanos()));
    }

    private void collectBenchmarks(BenchmarkArguments arguments, PerformanceTracker tracker) {
        Objects.requireNonNull(tracker, "tracker");
        Random random = new Random(arguments.randomSeed());
        for (int size : arguments.sizes()) {
            tracker.measure("random", size, randomGenerator(new Random(random.nextLong())),
                    arguments.warmupIterations(), arguments.measurementIterations());
            tracker.measure("sorted", size, sortedGenerator(),
                    arguments.warmupIterations(), arguments.measurementIterations());
            tracker.measure("reverse_sorted", size, reverseSortedGenerator(),
                    arguments.warmupIterations(), arguments.measurementIterations());
            tracker.measure("nearly_sorted", size, nearlySortedGenerator(new Random(random.nextLong())),
                    arguments.warmupIterations(), arguments.measurementIterations());
            tracker.measure("worst_case", size, worstCaseGenerator(),
                    arguments.warmupIterations(), arguments.measurementIterations());
        }
    }

    private IntFunction<int[]> randomGenerator(Random random) {
        return size -> random.ints(size, -1_000, 1_001).toArray();
    }

    private IntFunction<int[]> sortedGenerator() {
        return size -> {
            int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                array[i] = i;
            }
            return array;
        };
    }

    private IntFunction<int[]> reverseSortedGenerator() {
        return size -> {
            int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                array[i] = size - i;
            }
            return array;
        };
    }

    private IntFunction<int[]> nearlySortedGenerator(Random random) {
        return size -> {
            int[] array = sortedGenerator().apply(size);
            int swaps = Math.max(1, size / 20);
            for (int i = 0; i < swaps; i++) {
                int a = random.nextInt(size);
                int b = random.nextInt(size);
                int tmp = array[a];
                array[a] = array[b];
                array[b] = tmp;
            }
            return array;
        };
    }

    private IntFunction<int[]> worstCaseGenerator() {
        return size -> {
            int[] array = new int[size];
            Arrays.fill(array, -1);
            if (size > 0) {
                array[size - 1] = 1;
            }
            return array;
        };
    }

    private record BenchmarkArguments(int[] sizes,
                                      int warmupIterations,
                                      int measurementIterations,
                                      long randomSeed,
                                      Path outputPath) {

        static BenchmarkArguments parse(String[] args) {
            int[] sizes = new int[]{100, 1_000, 10_000};
            int warmup = 3;
            int measurements = 5;
            long seed = 42L;
            Path output = Path.of("data", "kadane-benchmarks.csv");
            for (String arg : args) {
                if (arg.startsWith("--sizes=")) {
                    sizes = Arrays.stream(arg.substring("--sizes=".length()).split(","))
                            .filter(token -> !token.isBlank())
                            .mapToInt(Integer::parseInt)
                            .toArray();
                } else if (arg.startsWith("--warmup=")) {
                    warmup = Integer.parseInt(arg.substring("--warmup=".length()));
                } else if (arg.startsWith("--measurements=")) {
                    measurements = Integer.parseInt(arg.substring("--measurements=".length()));
                } else if (arg.startsWith("--seed=")) {
                    seed = Long.parseLong(arg.substring("--seed=".length()));
                } else if (arg.startsWith("--output=")) {
                    output = Path.of(arg.substring("--output=".length()));
                } else if (arg.equals("--no-output")) {
                    output = null;
                } else if (arg.equals("--help")) {
                    printUsageAndExit();
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            return new BenchmarkArguments(sizes, warmup, measurements, seed, output);
        }

        private static void printUsageAndExit() {
            System.out.println("Usage: java -jar benchmark.jar [options]\n" +
                    "Options:\n" +
                    "  --sizes=100,1000,...     Comma-separated input sizes\n" +
                    "  --warmup=N              Warmup iterations per input\n" +
                    "  --measurements=N        Measurement iterations per input\n" +
                    "  --seed=VALUE            Random seed\n" +
                    "  --output=PATH           CSV output location (default data/kadane-benchmarks.csv)\n" +
                    "  --no-output             Skip writing CSV\n" +
                    "  --help                  Show this help message");
            System.exit(0);
        }
    }
}