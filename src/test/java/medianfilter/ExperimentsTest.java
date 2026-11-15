package medianfilter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for {@link Experiments}: the noise generator, the comparison filter and the metrics. */
class ExperimentsTest {

    /** Checks that the test picture has the documented size and valid gray levels. */
    @Test
    void theCleanImageIs256By256WithValidGrayLevels() {
        int[][] image = Experiments.cleanImage();
        assertEquals(Experiments.SIZE, image.length);
        assertEquals(Experiments.SIZE, image[0].length);
        for (int[] row : image) {
            for (int value : row) {
                assertTrue(value >= 0 && value <= 255);
            }
        }
    }

    /** Checks that zero noise returns an identical picture. */
    @Test
    void zeroNoiseLeavesTheImageUnchanged() {
        int[][] clean = Experiments.cleanImage();
        assertArrayEquals(clean, Experiments.addSaltAndPepper(clean, 0.0, 1));
    }

    /** Checks that the same seed always gives the same noisy picture. */
    @Test
    void theSameSeedGivesTheSameNoise() {
        int[][] clean = Experiments.cleanImage();
        assertArrayEquals(Experiments.addSaltAndPepper(clean, 0.2, 7),
                Experiments.addSaltAndPepper(clean, 0.2, 7));
    }

    /** Checks that the noise generator does not change the picture it is given. */
    @Test
    void addingNoiseDoesNotChangeTheOriginal() {
        int[][] clean = Experiments.cleanImage();
        int[][] copy = Experiments.cleanImage();
        Experiments.addSaltAndPepper(clean, 0.5, 3);
        assertArrayEquals(copy, clean);
    }

    /** Checks that damaged pixels are only pure black or pure white, about the requested share. */
    @Test
    void noisyPixelsAreBlackOrWhiteAndRoughlyTheRequestedShare() {
        int[][] clean = Experiments.cleanImage();
        int[][] noisy = Experiments.addSaltAndPepper(clean, 0.2, 42);
        int damaged = 0;
        for (int y = 0; y < clean.length; y++) {
            for (int x = 0; x < clean[0].length; x++) {
                if (noisy[y][x] != clean[y][x]) {
                    assertTrue(noisy[y][x] == 0 || noisy[y][x] == 255);
                    damaged++;
                }
            }
        }
        double share = damaged / (double) (Experiments.SIZE * Experiments.SIZE);
        assertTrue(share > 0.17 && share < 0.23, "share was " + share);
    }

    /** Checks that the mean filter is pulled up by a spike, unlike the median (the lesson). */
    @Test
    void theMeanOfTheSpikeExampleIsAbout74WhileTheMedianIs51() {
        int[][] example = TestSupport.spikeExample();
        assertEquals(74, Experiments.meanFilter(example, 3)[1][1]);
        assertEquals(51, MedianFilter.apply(example, 3)[1][1]);
    }

    /** Checks that averaging a flat picture changes nothing. */
    @Test
    void theMeanFilterLeavesAUniformImageUnchanged() {
        int[][] image = TestSupport.filled(5, 5, 90);
        assertArrayEquals(image, Experiments.meanFilter(image, 3));
    }

    /** Checks the error measure on identical and on different pictures. */
    @Test
    void meanSquaredErrorIsZeroForEqualImagesAndSquaredDifferenceOtherwise() {
        assertEquals(0.0, Experiments.meanSquaredError(
                TestSupport.spikeExample(), TestSupport.spikeExample()));
        assertEquals(100.0, Experiments.meanSquaredError(new int[][] {{0}}, new int[][] {{10}}));
    }

    /** Checks the decibel conversion, including the infinite value for a perfect match. */
    @Test
    void psnrFollowsTheFormulaAndIsInfiniteForAPerfectMatch() {
        assertEquals(28.13, Experiments.psnr(100.0), 0.01);
        assertEquals(Double.POSITIVE_INFINITY, Experiments.psnr(0.0));
    }

    /** Checks the failure-rate formula against a value worked out by hand (3x3, 50% noise). */
    @Test
    void predictedFailureRateMatchesTheHandWorkedBinomialValue() {
        assertEquals(0.0979, Experiments.predictedFailureRate(3, 0.5), 0.0005);
        assertEquals(0.0, Experiments.predictedFailureRate(3, 0.0));
    }

    /** Checks that a larger window is predicted to fail less often at the same noise level. */
    @Test
    void biggerWindowsAreLessLikelyToFail() {
        assertTrue(Experiments.predictedFailureRate(5, 0.4) < Experiments.predictedFailureRate(3, 0.4));
        assertTrue(Experiments.predictedFailureRate(7, 0.4) < Experiments.predictedFailureRate(5, 0.4));
    }
}
