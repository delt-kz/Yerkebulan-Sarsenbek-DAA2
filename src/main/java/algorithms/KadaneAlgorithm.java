package algorithms;

import java.util.Objects;
import java.util.StringJoiner;

public final class KadaneAlgorithm {

    private KadaneAlgorithm() {

    }

    public static final class Result {
        private final long maxSum;
        private final int startIndex;
        private final int endIndex;

        public Result(long maxSum, int startIndex, int endIndex) {
            this.maxSum = maxSum;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public long maxSum() {
            return maxSum;
        }

        public int startIndex() {
            return startIndex;
        }

        public int endIndex() {
            return endIndex;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Result result)) {
                return false;
            }
            return maxSum == result.maxSum
                    && startIndex == result.startIndex
                    && endIndex == result.endIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(maxSum, startIndex, endIndex);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                    .add("maxSum=" + maxSum)
                    .add("startIndex=" + startIndex)
                    .add("endIndex=" + endIndex)
                    .toString();
        }
    }


    public static Result maxSubarray(int[] array) {
        Objects.requireNonNull(array, "array");
        if (array.length == 0) {
            return new Result(0, -1, -1);
        }

        long bestSum = Long.MIN_VALUE;
        long currentSum = 0;
        int bestStart = 0;
        int bestEnd = 0;
        int candidateStart = 0;

        for (int index = 0; index < array.length; index++) {
            int value = array[index];
            if (currentSum < 0) {
                currentSum = value;
                candidateStart = index;
            } else {
                currentSum += value;
            }

            if (currentSum > bestSum || (currentSum == bestSum && (index - candidateStart) > (bestEnd - bestStart))) {
                bestSum = currentSum;
                bestStart = candidateStart;
                bestEnd = index;
            }
        }

        return new Result(bestSum, bestStart, bestEnd);
    }


    public static Result bruteForceMaxSubarray(int[] array) {
        Objects.requireNonNull(array, "array");
        if (array.length == 0) {
            return new Result(0, -1, -1);
        }

        long bestSum = Long.MIN_VALUE;
        int bestStart = 0;
        int bestEnd = 0;
        for (int start = 0; start < array.length; start++) {
            long sum = 0;
            for (int end = start; end < array.length; end++) {
                sum += array[end];
                if (sum > bestSum || (sum == bestSum && (end - start) > (bestEnd - bestStart))) {
                    bestSum = sum;
                    bestStart = start;
                    bestEnd = end;
                }
            }
        }
        return new Result(bestSum, bestStart, bestEnd);
    }
}