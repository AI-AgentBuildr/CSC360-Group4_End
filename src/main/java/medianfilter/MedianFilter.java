package medianfilter;

/** The median filter for a single-channel (grayscale) image. */
public final class MedianFilter {

    /** Not instantiable: this class only holds static methods. */
    private MedianFilter() {
    }

    /**
     * Applies a median filter. Every output pixel is the median of the square window of
     * neighbours around the same position in the input. Results go into a new array, so
     * already-filtered pixels never influence later ones, and the input is not changed.
     * Pixels near the edge use the nearest edge value for missing neighbours
     * (see {@link PixelGrid#getClamped(int, int)}).
     *
     * @param pixels grayscale image as {@code pixels[y][x]}
     * @param windowSize odd side length of the window, at least 3
     * @return the filtered image, same size as the input
     * @throws IllegalArgumentException if the image or the window size is invalid
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
                result[y][x] = Median.median(window);
            }
        }
        return result;
    }
}
