package medianfilter;

import ij.IJ;
import ij.ImagePlus;

/**
 * Program entry point. Opens an image, applies the median filter, and shows the original and
 * the filtered image in two ImageJ windows.
 *
 * <p>Usage: {@code MedianFilterApp <image-path> [window-size]} (window size defaults to 3).
 */
public final class MedianFilterApp {

    /** Window size used when none is given on the command line. */
    private static final int DEFAULT_WINDOW_SIZE = 3;

    /** Not instantiable: this class only holds static methods. */
    private MedianFilterApp() {
    }

    /**
     * Runs the program.
     *
     * @param args {@code args[0]} is the image file path; optional {@code args[1]} is the
     *             window size
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: MedianFilterApp <image-path> [window-size]");
            return;
        }
        int windowSize = DEFAULT_WINDOW_SIZE;
        if (args.length > 1) {
            try {
                windowSize = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Window size must be a whole number, got: " + args[1]);
                return;
            }
        }

        ImagePlus original = IJ.openImage(args[0]);
        if (original == null) {
            System.out.println("Could not open image: " + args[0]);
            return;
        }

        try {
            ImagePlus filtered = new ImagePlus("Median filtered (" + windowSize + "x" + windowSize + ")",
                    ImageAdapter.filter(original.getProcessor(), windowSize));
            original.show();
            filtered.show();
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
