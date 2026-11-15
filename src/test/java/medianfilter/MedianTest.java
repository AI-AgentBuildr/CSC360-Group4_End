package medianfilter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests for {@link Median}. */
class MedianTest {

    /** Checks that middle of an odd count of sorted values. */
    @Test
    void middleOfAnOddCountOfSortedValues() {
        assertEquals(3, Median.median(new int[] {1, 2, 3, 4, 5}));
    }

    /** Checks that middle of unsorted values. */
    @Test
    void middleOfUnsortedValues() {
        assertEquals(51, Median.median(new int[] {50, 52, 51, 49, 255, 53, 50, 51, 52}));
    }

    /** Checks that single value is its own median. */
    @Test
    void singleValueIsItsOwnMedian() {
        assertEquals(7, Median.median(new int[] {7}));
    }

    /** Checks that an extreme value does not move the median. */
    @Test
    void anExtremeValueDoesNotMoveTheMedian() {
        assertEquals(10, Median.median(new int[] {10, 10, 10, 0, 255}));
    }

    /** Checks that even count gives the lower middle value. */
    @Test
    void evenCountGivesTheLowerMiddleValue() {
        assertEquals(2, Median.median(new int[] {1, 2, 3, 4}));
    }

    /** Checks that the input array is not reordered. */
    @Test
    void theInputArrayIsNotReordered() {
        int[] values = {5, 1, 4};
        Median.median(values);
        assertArrayEquals(new int[] {5, 1, 4}, values);
    }

    /** Checks that null or empty is rejected. */
    @Test
    void nullOrEmptyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Median.median(null));
        assertThrows(IllegalArgumentException.class, () -> Median.median(new int[0]));
    }
}
