package medianfilter;

/** Validation and conversion for the filter's window size setting. */
public final class FilterOptions {

    /** Smallest allowed window size (a 3 by 3 square). */
    public static final int MIN_WINDOW_SIZE = 3;

    /** Not instantiable: this class only holds static methods. */
    private FilterOptions() {
    }

    /**
     * Checks that a window size is usable: odd (so there is a centre pixel) and at least 3.
     *
     * @param windowSize side length of the square window, for example 3, 5 or 7
     * @throws IllegalArgumentException if the size is even or smaller than 3
     */
    public static void validateWindowSize(int windowSize) {
        if (windowSize < MIN_WINDOW_SIZE) {
            throw new IllegalArgumentException(
                    "Window size must be at least " + MIN_WINDOW_SIZE + " but was " + windowSize);
        }
        if (windowSize % 2 == 0) {
            throw new IllegalArgumentException(
                    "Window size must be odd (3, 5, 7, ...) but was " + windowSize);
        }
    }

    /**
     * Converts a window size to its radius: how many pixels the window reaches out from the
     * centre in each direction (3 gives 1, 5 gives 2, 7 gives 3).
     *
     * @param windowSize side length of the square window
     * @return the radius
     * @throws IllegalArgumentException if the size is not valid
     */
    public static int radiusFor(int windowSize) {
        validateWindowSize(windowSize);
        return windowSize / 2;
    }
}