package medianfilter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests for {@link FilterOptions}. */
class FilterOptionsTest {

    /** Checks that odd sizes from three up are accepted. */
    @Test
    void oddSizesFromThreeUpAreAccepted() {
        for (int size : new int[] {3, 5, 7, 9, 21}) {
            assertDoesNotThrow(() -> FilterOptions.validateWindowSize(size));
        }
    }

    /** Checks that even sizes are rejected. */
    @Test
    void evenSizesAreRejected() {
        for (int size : new int[] {4, 6, 10}) {
            assertThrows(IllegalArgumentException.class,
                    () -> FilterOptions.validateWindowSize(size), "size " + size);
        }
    }

    /** Checks that too small sizes are rejected. */
    @Test
    void tooSmallSizesAreRejected() {
        for (int size : new int[] {1, 0, -3}) {
            assertThrows(IllegalArgumentException.class,
                    () -> FilterOptions.validateWindowSize(size), "size " + size);
        }
    }

    /** Checks that radius is half the window rounded down. */
    @Test
    void radiusIsHalfTheWindowRoundedDown() {
        assertEquals(1, FilterOptions.radiusFor(3));
        assertEquals(2, FilterOptions.radiusFor(5));
        assertEquals(3, FilterOptions.radiusFor(7));
    }

    /** Checks that radius rejects an invalid size. */
    @Test
    void radiusRejectsAnInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> FilterOptions.radiusFor(4));
    }
}
