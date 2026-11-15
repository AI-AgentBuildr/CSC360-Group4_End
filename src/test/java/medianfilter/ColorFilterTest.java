package medianfilter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests for {@link ColorFilter}. */
class ColorFilterTest {

    /**
     * Packs red, green and blue into one 0xRRGGBB integer.
     *
     * @param r red, 0 to 255
     * @param g green, 0 to 255
     * @param b blue, 0 to 255
     * @return the packed colour
     */
    private static int rgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    /** Checks that a uniform colour image stays the same. */
    @Test
    void aUniformColourImageStaysTheSame() {
        int[][] image = TestSupport.filled(4, 4, rgb(10, 200, 30));
        assertArrayEquals(image, ColorFilter.applyRgb(image, 3));
    }

    /** Checks that a spike in one channel is removed and the others are kept. */
    @Test
    void aSpikeInOneChannelIsRemovedAndTheOthersAreKept() {
        int[][] image = TestSupport.filled(5, 5, rgb(10, 20, 30));
        image[2][2] = rgb(255, 20, 30);
        int[][] result = ColorFilter.applyRgb(image, 3);
        assertEquals(rgb(10, 20, 30), result[2][2]);
    }

    /** Checks that each channel is filtered on its own. */
    @Test
    void eachChannelIsFilteredOnItsOwn() {
        int[][] image = TestSupport.filled(5, 5, rgb(100, 100, 100));
        image[2][2] = rgb(0, 255, 0);
        int[][] result = ColorFilter.applyRgb(image, 3);
        assertEquals(rgb(100, 100, 100), result[2][2]);
    }
}
