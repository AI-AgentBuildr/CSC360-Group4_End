package medianfilter;

/** The median filter for a single-channel (grayscale) image. */
public final class MedianFilter {

    private MedianFilter() {
    }

    /**
     * Placeholder for now: checks the image is valid and returns an unchanged copy.
     * The real filtering is added in the next commits.
     */
    public static int[][] apply(int[][] pixels, int windowSize) {
        PixelGrid grid = new PixelGrid(pixels);
        int[][] result = new int[grid.getHeight()][grid.getWidth()];
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                result[y][x] = pixels[y][x];
            }
        }
        return result;
    }
}
