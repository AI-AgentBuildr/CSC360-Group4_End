package medianfilter;

/** Median filtering for colour images stored as packed RGB integers (0xRRGGBB). */
public final class ColorFilter {

    /** Not instantiable: this class only holds static methods. */
    private ColorFilter() {
    }

    /**
     * Filters the red, green and blue channels separately with
     * {@link MedianFilter#apply(int[][], int)} and packs them back together.
     *
     * @param rgb image as {@code rgb[y][x]}, each value packed as 0xRRGGBB
     * @param windowSize odd side length of the window, at least 3
     * @return the filtered image, same size as the input
     * @throws IllegalArgumentException if the image or the window size is invalid
     */
    public static int[][] applyRgb(int[][] rgb, int windowSize) {
        PixelGrid grid = new PixelGrid(rgb);
        int h = grid.getHeight();
        int w = grid.getWidth();
        int[][] red = new int[h][w];
        int[][] green = new int[h][w];
        int[][] blue = new int[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = rgb[y][x];
                red[y][x] = (p >> 16) & 0xFF;
                green[y][x] = (p >> 8) & 0xFF;
                blue[y][x] = p & 0xFF;
            }
        }
        int[][] r = MedianFilter.apply(red, windowSize);
        int[][] g = MedianFilter.apply(green, windowSize);
        int[][] b = MedianFilter.apply(blue, windowSize);
        int[][] result = new int[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                result[y][x] = (r[y][x] << 16) | (g[y][x] << 8) | b[y][x];
            }
        }
        return result;
    }
}
