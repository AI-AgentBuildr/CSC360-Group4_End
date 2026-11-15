package medianfilter;

/** Small helpers shared by the tests. */
final class TestSupport {

    /** Not instantiable: this class only holds static methods. */
    private TestSupport() {
    }

    /**
     * Builds an image of the given size where every pixel has the same value.
     *
     * @param width number of columns
     * @param height number of rows
     * @param value the value of every pixel
     * @return the new image, indexed [row][column]
     */
    static int[][] filled(int width, int height, int value) {
        int[][] image = new int[height][width];
        for (int[] row : image) {
            java.util.Arrays.fill(row, value);
        }
        return image;
    }

    /**
     * The 3 by 3 example from the lessons: a noisy 255 in the middle of values near 50.
     *
     * @return a new copy of the example image
     */
    static int[][] spikeExample() {
        return new int[][] {
            {50, 52, 51},
            {49, 255, 53},
            {50, 51, 52}
        };
    }
}
