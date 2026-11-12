package medianfilter;

import java.util.Arrays;

/** Finds the median (middle value) of a set of numbers. */
public final class Median {

    /** Not instantiable: this class only holds static methods. */
    private Median() {
    }

    /**
     * Returns the middle value after sorting. For an odd count this is the exact median;
     * for an even count it is the lower of the two middle values.
     *
     * <p>The input array is not changed; a sorted copy is used.
     *
     * @param values the numbers, at least one
     * @return the median
     * @throws IllegalArgumentException if {@code values} is null or empty
     */
    public static int median(int[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Need at least one value");
        }
        int[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        return sorted[(sorted.length - 1) / 2];
    }
}
