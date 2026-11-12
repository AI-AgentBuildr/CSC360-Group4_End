package medianfilter;

/** The median filter for a single-channel (grayscale) image. */
public final class MedianFilter {

    private MedianFilter() {
    }

    /**
     * Collects the window of neighbours for every pixel. For now the output is still the
     * centre pixel of the window (so the image is unchanged); the median comes next.
     */
    public static int[][] apply(int[][] pixels, int windowSize) {
        PixelGrid grid = new PixelGrid(pixels);
        int radius = FilterOptions.radiusFor(windowSize);
        int[][] result = new int[grid.getHeight()][grid.getWidth()];
        int[] window = new int[windowSize * windowSize];

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                int count = 0;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        window[count++] = grid.getClamped(x + dx, y + dy);
                    }
                }
                result[y][x] = window[window.length / 2]; // TODO: replace with the median
            }
        }
        return result;
    }
}
