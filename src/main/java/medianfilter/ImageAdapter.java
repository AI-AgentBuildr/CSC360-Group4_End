package medianfilter;

import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * Converts between ImageJ's {@link ImageProcessor} and the plain {@code int[][]} arrays the
 * filter works on, and runs the right filter for the image type.
 */
public final class ImageAdapter {

    /** Not instantiable: this class only holds static methods. */
    private ImageAdapter() {
    }

    /**
     * Copies an ImageJ image into an array indexed {@code [y][x]}. For colour images each
     * value is a packed 0xRRGGBB integer; otherwise it is the gray level.
     *
     * @param ip the ImageJ image
     * @return a new array holding a copy of the pixels
     */
    public static int[][] toArray(ImageProcessor ip) {
        int[][] pixels = new int[ip.getHeight()][ip.getWidth()];
        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                pixels[y][x] = ip.getPixel(x, y);
            }
        }
        return pixels;
    }

    /**
     * Builds a new ImageJ image of the same type and size as {@code template} from an array.
     *
     * @param pixels the pixel values, {@code [y][x]}
     * @param template an image whose type (grayscale or colour) the result should match
     * @return a new image containing the pixels
     */
    public static ImageProcessor fromArray(int[][] pixels, ImageProcessor template) {
        int height = pixels.length;
        int width = pixels[0].length;
        ImageProcessor result = template.createProcessor(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result.putPixel(x, y, pixels[y][x]);
            }
        }
        return result;
    }

    /**
     * Median-filters an ImageJ image and returns the result as a new image; the original is
     * left untouched.
     *
     * <p>Supported types: 8-bit and 16-bit grayscale, and RGB colour. 32-bit floating point
     * images are rejected, because their pixel values are decimal numbers that do not fit the
     * integer arrays the filter works on.
     *
     * @param ip the image to filter (8-bit or 16-bit grayscale, or RGB colour)
     * @param windowSize odd side length of the window, at least 3
     * @return the filtered copy
     * @throws IllegalArgumentException if the window size is invalid or the image is 32-bit float
     */
    public static ImageProcessor filter(ImageProcessor ip, int windowSize) {
        if (ip instanceof FloatProcessor) {
            throw new IllegalArgumentException(
                    "32-bit float images are not supported; convert to 8-bit or 16-bit first");
        }
        int[][] pixels = toArray(ip);
        int[][] filtered = (ip instanceof ColorProcessor)
                ? ColorFilter.applyRgb(pixels, windowSize)
                : MedianFilter.apply(pixels, windowSize);
        return fromArray(filtered, ip);
    }
}
