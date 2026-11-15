package medianfilter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests for {@link MedianFilter}. */
class MedianFilterTest {

    /** Checks that the spike in the middle is replaced by 51. */
    @Test
    void theSpikeInTheMiddleIsReplacedBy51() {
        int[][] result = MedianFilter.apply(TestSupport.spikeExample(), 3);
        assertEquals(51, result[1][1]);
    }

    /** Checks that a uniform image stays the same. */
    @Test
    void aUniformImageStaysTheSame() {
        int[][] image = TestSupport.filled(6, 4, 128);
        assertArrayEquals(image, MedianFilter.apply(image, 3));
    }

    /** Checks that the output has the same size as the input. */
    @Test
    void theOutputHasTheSameSizeAsTheInput() {
        int[][] result = MedianFilter.apply(TestSupport.filled(7, 3, 9), 3);
        assertEquals(3, result.length);
        assertEquals(7, result[0].length);
    }

    /** Checks that the input image is not changed. */
    @Test
    void theInputImageIsNotChanged() {
        int[][] image = TestSupport.spikeExample();
        MedianFilter.apply(image, 3);
        assertEquals(255, image[1][1]);
    }

    /** Checks that a single isolated spike vanishes everywhere. */
    @Test
    void aSingleIsolatedSpikeVanishesEverywhere() {
        int[][] image = TestSupport.filled(5, 5, 20);
        image[2][2] = 255;
        assertArrayEquals(TestSupport.filled(5, 5, 20), MedianFilter.apply(image, 3));
    }

    /** Checks that a spike in a corner is removed by edge clamping. */
    @Test
    void aSpikeInACornerIsRemovedByEdgeClamping() {
        int[][] image = TestSupport.filled(4, 4, 30);
        image[0][0] = 0;
        assertEquals(30, MedianFilter.apply(image, 3)[0][0]);
    }

    /** Checks that a straight edge between two regions stays sharp. */
    @Test
    void aStraightEdgeBetweenTwoRegionsStaysSharp() {
        int[][] image = new int[5][6];
        for (int[] row : image) {
            for (int x = 0; x < 6; x++) {
                row[x] = x < 3 ? 0 : 200;
            }
        }
        assertArrayEquals(image, MedianFilter.apply(image, 3));
    }

    /** Checks that a single one pixel image is unchanged. */
    @Test
    void aSingleOnePixelImageIsUnchanged() {
        assertArrayEquals(new int[][] {{42}}, MedianFilter.apply(new int[][] {{42}}, 3));
    }

    /** Checks that a larger window removes a thicker blotch. */
    @Test
    void aLargerWindowRemovesAThickerBlotch() {
        int[][] image = TestSupport.filled(9, 9, 10);
        for (int y = 3; y <= 5; y++) {
            for (int x = 3; x <= 5; x++) {
                image[y][x] = 255;
            }
        }
        int[][] small = MedianFilter.apply(image, 3);
        int[][] large = MedianFilter.apply(image, 5);
        assertEquals(255, small[4][4]);
        assertEquals(10, large[4][4]);
    }

    /** Checks that an invalid window size is rejected. */
    @Test
    void anInvalidWindowSizeIsRejected() {
        int[][] image = TestSupport.filled(3, 3, 1);
        assertThrows(IllegalArgumentException.class, () -> MedianFilter.apply(image, 4));
        assertThrows(IllegalArgumentException.class, () -> MedianFilter.apply(image, 1));
    }

    /** Checks that a bad image is rejected. */
    @Test
    void aBadImageIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MedianFilter.apply(null, 3));
        assertThrows(IllegalArgumentException.class, () -> MedianFilter.apply(new int[0][0], 3));
    }
}
