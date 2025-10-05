package algorithms;

import algorithms.KadaneAlgorithm.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KadaneAlgorithmTest {

    @Test
    @DisplayName("Null arrays are not permitted")
    void nullArrayThrows() {
        assertThrows(NullPointerException.class, () -> KadaneAlgorithm.maxSubarray(null));
    }

    @Test
    @DisplayName("Empty arrays return neutral result")
    void emptyArray() {
        Result result = KadaneAlgorithm.maxSubarray(new int[0]);
        assertEquals(new Result(0, -1, -1), result);
    }

    @Test
    @DisplayName("Single element array returns that element")
    void singleElementArray() {
        Result result = KadaneAlgorithm.maxSubarray(new int[]{42});
        assertEquals(new Result(42, 0, 0), result);
    }

    @Test
    @DisplayName("All negative numbers return the least negative element")
    void allNegativeArray() {
        int[] array = {-8, -3, -5, -7};
        Result result = KadaneAlgorithm.maxSubarray(array);
        assertEquals(new Result(-3, 1, 1), result);
    }

    @Test
    @DisplayName("Mixed array finds correct segment")
    void mixedArray() {
        int[] array = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        Result result = KadaneAlgorithm.maxSubarray(array);
        assertEquals(new Result(6, 3, 6), result);
    }

    @Test
    @DisplayName("Duplicate values handled correctly")
    void duplicateValues() {
        int[] array = {1, 2, 2, -1, 2, 2, 2, -5, 2};
        Result result = KadaneAlgorithm.maxSubarray(array);
        assertEquals(new Result(10, 0, 6), result);
    }

    @Test
    @DisplayName("Property-based testing against quadratic baseline")
    void propertyBased() {
        Random random = new Random(1337);
        for (int size = 1; size <= 30; size++) {
            for (int trial = 0; trial < 100; trial++) {
                int[] array = random.ints(size, -20, 21).toArray();
                Result expected = KadaneAlgorithm.bruteForceMaxSubarray(array);
                Result actual = KadaneAlgorithm.maxSubarray(array);
                assertEquals(expected, actual, () -> "Mismatch for " + IntStream.of(array).boxed().toList());
            }
        }
    }
}