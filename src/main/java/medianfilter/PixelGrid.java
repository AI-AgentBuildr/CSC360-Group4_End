package medianfilter;

/**
 * A read-only view of a rectangular grid of pixel values, indexed as {@code pixels[y][x]}
 * (row first). It validates the grid once and handles the image edges for the filter.
 */
public final class PixelGrid {

    /** The pixel values, indexed [row][column]. */
    private final int[][] pixels;
    /** Number of columns. */
    private final int width;
    /** Number of rows. */
    private final int height;

    /**
     * Wraps a pixel array.
     *
     * @param pixels rows of pixel values; must be non-null, non-empty and rectangular
     * @throws IllegalArgumentException if the array is null, empty, or its rows differ in length
     */
    public PixelGrid(int[][] pixels) {
        if (pixels == null || pixels.length == 0 || pixels[0] == null || pixels[0].length == 0) {
            throw new IllegalArgumentException("Image must have at least one pixel");
        }
        int w = pixels[0].length;
        for (int[] row : pixels) {
            if (row == null || row.length != w) {
                throw new IllegalArgumentException("Image rows must all have the same length");
            }
        }
        this.pixels = pixels;
        this.width = w;
        this.height = pixels.length;
    }

    /**
     * Returns the image width.
     *
     * @return number of pixel columns
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the image height.
     *
     * @return number of pixel rows
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the pixel at (x, y). A position outside the image is moved to the nearest edge
     * pixel, so border pixels see their own edge value repeated instead of missing neighbours.
     *
     * @param x column, may be negative or past the right edge
     * @param y row, may be negative or past the bottom edge
     * @return the pixel value at the clamped position
     */
    public int getClamped(int x, int y) {
        int cx = Math.max(0, Math.min(width - 1, x));
        int cy = Math.max(0, Math.min(height - 1, y));
        return pixels[cy][cx];
    }

    /**
     * Tells whether a window of the given radius around (x, y) would stick out of the image.
     *
     * @param x column of the centre pixel
     * @param y row of the centre pixel
     * @param radius how far the window reaches from the centre
     * @return true if any part of the window is outside the image
     */
    public boolean isBorder(int x, int y, int radius) {
        return x - radius < 0 || y - radius < 0 || x + radius >= width || y + radius >= height;
    }
}