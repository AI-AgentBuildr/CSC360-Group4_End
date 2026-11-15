package medianfilter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Reproducible experiments that measure how well the median filter removes noise. It builds a
 * test picture, adds salt-and-pepper noise, filters it, and compares the result with the clean
 * picture. Everything is seeded, so every run gives the same numbers.
 *
 * <p>Run it with {@code mvn compile exec:java -Dexec.mainClass=medianfilter.Experiments
 * -Dexec.args="src/main/resources/samples docs/images"}. The two arguments are the folders for
 * the sample pictures and for the result pictures; leave them out to print the tables only.
 */
public final class Experiments {

    /** Width and height of the test picture in pixels. */
    public static final int SIZE = 256;

    /** Fixed seed so the noise is the same on every run. */
    private static final long SEED = 42;

    /** Not instantiable: this class only holds static methods. */
    private Experiments() {
    }

    /**
     * Builds the noise-free test picture: a soft gradient with a bright disc and a dark square.
     *
     * @return the picture as {@code [row][column]} gray levels
     */
    public static int[][] cleanImage() {
        int[][] image = new int[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                image[y][x] = 40 + (x * 60) / SIZE;
                int dx = x - 90;
                int dy = y - 100;
                if (dx * dx + dy * dy < 55 * 55) {
                    image[y][x] = 200;
                }
                if (x > 150 && x < 230 && y > 140 && y < 220) {
                    image[y][x] = 20;
                }
            }
        }
        return image;
    }

    /**
     * Adds salt-and-pepper noise: half of the affected pixels become black (0) and half white
     * (255), chosen at random.
     *
     * @param image the clean picture (not changed)
     * @param fraction share of pixels to damage, from 0.0 to 1.0
     * @param seed seed for the random generator, so the result can be reproduced
     * @return a noisy copy
     */
    public static int[][] addSaltAndPepper(int[][] image, double fraction, long seed) {
        Random random = new Random(seed);
        int[][] noisy = new int[image.length][image[0].length];
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                double p = random.nextDouble();
                if (p < fraction / 2) {
                    noisy[y][x] = 0;
                } else if (p < fraction) {
                    noisy[y][x] = 255;
                } else {
                    noisy[y][x] = image[y][x];
                }
            }
        }
        return noisy;
    }

    /**
     * A simple averaging (mean) filter, used only as a comparison for the median filter.
     *
     * @param image the picture to filter
     * @param windowSize odd side length of the window, at least 3
     * @return the filtered picture
     */
    public static int[][] meanFilter(int[][] image, int windowSize) {
        PixelGrid grid = new PixelGrid(image);
        int radius = FilterOptions.radiusFor(windowSize);
        int[][] result = new int[grid.getHeight()][grid.getWidth()];
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                long sum = 0;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        sum += grid.getClamped(x + dx, y + dy);
                    }
                }
                result[y][x] = (int) Math.round(sum / (double) (windowSize * windowSize));
            }
        }
        return result;
    }

    /**
     * Average of (a - b) squared over all pixels: 0 means identical pictures.
     *
     * @param a first picture
     * @param b second picture, same size as the first
     * @return the mean squared error
     */
    public static double meanSquaredError(int[][] a, int[][] b) {
        double sum = 0;
        for (int y = 0; y < a.length; y++) {
            for (int x = 0; x < a[0].length; x++) {
                double d = a[y][x] - b[y][x];
                sum += d * d;
            }
        }
        return sum / ((double) a.length * a[0].length);
    }

    /**
     * Peak signal-to-noise ratio in decibels, computed as 10 * log10(255 squared / mse).
     * Higher means closer to the clean picture.
     *
     * @param mse mean squared error between the clean and the filtered picture
     * @return the PSNR, or infinity if {@code mse} is 0
     */
    public static double psnr(double mse) {
        return mse == 0 ? Double.POSITIVE_INFINITY : 10 * Math.log10(255.0 * 255.0 / mse);
    }

    /**
     * Predicts how often the median filter fails on salt-and-pepper noise, using probability.
     *
     * <p>In a window of n values, sorted, the median is value number (n + 1) / 2. Black noise
     * sits at the bottom of the sorted list and white noise at the top, so the median is itself
     * a noise value only when at least (n + 1) / 2 of the window values are black, or at least
     * that many are white. If each pixel is black with probability {@code noiseFraction / 2},
     * the chance is twice the upper tail of a binomial distribution with n trials.
     *
     * @param windowSize odd side length of the window, at least 3
     * @param noiseFraction share of pixels damaged by noise, from 0.0 to 1.0
     * @return the predicted share of output pixels that are still noise, from 0.0 to 1.0
     */
    public static double predictedFailureRate(int windowSize, double noiseFraction) {
        FilterOptions.validateWindowSize(windowSize);
        int n = windowSize * windowSize;
        int needed = (n + 1) / 2;
        double q = noiseFraction / 2;
        double tail = 0;
        for (int j = needed; j <= n; j++) {
            tail += choose(n, j) * Math.pow(q, j) * Math.pow(1 - q, n - j);
        }
        return 2 * tail;
    }

    /**
     * Binomial coefficient "n choose k".
     *
     * @param n number of items
     * @param k number chosen
     * @return the number of ways to choose k of n items
     */
    private static double choose(int n, int k) {
        double result = 1;
        for (int i = 1; i <= k; i++) {
            result = result * (n - k + i) / i;
        }
        return result;
    }

    /**
     * Saves a picture as a grayscale PNG file.
     *
     * @param image the picture, gray levels 0 to 255
     * @param file where to write it
     * @throws IOException if the file cannot be written
     */
    private static void save(int[][] image, File file) throws IOException {
        BufferedImage out = new BufferedImage(
                image[0].length, image.length, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                out.getRaster().setSample(x, y, 0, image[y][x]);
            }
        }
        ImageIO.write(out, "png", file);
    }

    /**
     * Saves several pictures side by side in one PNG, with a white gap between them.
     *
     * @param images the pictures, all the same size
     * @param file where to write the combined picture
     * @throws IOException if the file cannot be written
     */
    private static void saveSideBySide(int[][][] images, File file) throws IOException {
        int gap = 8;
        int height = images[0].length;
        int width = images[0][0].length;
        BufferedImage out = new BufferedImage(
                images.length * width + (images.length - 1) * gap, height,
                BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < out.getWidth(); x++) {
                out.getRaster().setSample(x, y, 0, 255);
            }
        }
        for (int i = 0; i < images.length; i++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    out.getRaster().setSample(i * (width + gap) + x, y, 0, images[i][y][x]);
                }
            }
        }
        ImageIO.write(out, "png", file);
    }

    /**
     * Formats a PSNR value for a table cell.
     *
     * @param value PSNR in decibels
     * @return the value with one decimal, or "inf"
     */
    private static String db(double value) {
        return Double.isInfinite(value) ? "inf" : String.format("%.1f", value);
    }

    /**
     * Runs the experiments and prints the result tables (Markdown) to the console.
     *
     * @param args optional: {@code args[0]} folder for clean.png and noisy.png,
     *             {@code args[1]} folder for the result pictures
     * @throws IOException if a picture cannot be written
     */
    public static void main(String[] args) throws IOException {
        int[][] clean = cleanImage();

        // Experiment 1: the 10 percent noise picture used in the README.
        int[][] noisy = addSaltAndPepper(clean, 0.10, SEED);
        int[][] median3 = MedianFilter.apply(noisy, 3);
        int[][] median5 = MedianFilter.apply(noisy, 5);
        int[][] mean3 = meanFilter(noisy, 3);
        if (args.length >= 2) {
            save(clean, new File(args[0], "clean.png"));
            save(noisy, new File(args[0], "noisy.png"));
            save(median3, new File(args[1], "median_3x3.png"));
            save(median5, new File(args[1], "median_5x5.png"));
            save(mean3, new File(args[1], "mean_3x3.png"));
            saveSideBySide(new int[][][] {noisy, median3, mean3},
                    new File(args[1], "before_after.png"));
        }
        System.out.println("Experiment 1: 10% noise, error against the clean picture");
        System.out.println("| Image | MSE | PSNR (dB) |");
        System.out.println("|---|---|---|");
        String[] names = {"Noisy input", "Median 3x3", "Median 5x5", "Mean 3x3"};
        int[][][] results = {noisy, median3, median5, mean3};
        for (int i = 0; i < names.length; i++) {
            double mse = meanSquaredError(clean, results[i]);
            System.out.printf("| %s | %.1f | %s |%n", names[i], mse, db(psnr(mse)));
        }

        // Experiment 2: how the quality changes with the amount of noise and the window size.
        System.out.println();
        System.out.println("Experiment 2: PSNR (dB) by noise level and window size");
        System.out.println("| Noise | Noisy input | Median 3x3 | Median 5x5 | Median 7x7 | Mean 3x3 |");
        System.out.println("|---|---|---|---|---|---|");
        for (int percent : new int[] {5, 10, 20, 30, 40, 50}) {
            int[][] n = addSaltAndPepper(clean, percent / 100.0, SEED);
            System.out.printf("| %d%% | %s | %s | %s | %s | %s |%n", percent,
                    db(psnr(meanSquaredError(clean, n))),
                    db(psnr(meanSquaredError(clean, MedianFilter.apply(n, 3)))),
                    db(psnr(meanSquaredError(clean, MedianFilter.apply(n, 5)))),
                    db(psnr(meanSquaredError(clean, MedianFilter.apply(n, 7)))),
                    db(psnr(meanSquaredError(clean, meanFilter(n, 3)))));
        }

        // Experiment 3: how the running time grows with the window size.
        System.out.println();
        System.out.println("Experiment 3: time per image relative to a 3x3 window (machine dependent)");
        System.out.println("| Window | Values sorted per pixel | Relative time |");
        System.out.println("|---|---|---|");
        MedianFilter.apply(noisy, 3); // warm up the JVM
        double base = 0;
        for (int size : new int[] {3, 5, 7, 9}) {
            long best = Long.MAX_VALUE;
            for (int run = 0; run < 5; run++) {
                long start = System.nanoTime();
                MedianFilter.apply(noisy, size);
                best = Math.min(best, System.nanoTime() - start);
            }
            if (size == 3) {
                base = best;
            }
            System.out.printf("| %dx%d | %d | %.1fx |%n", size, size, size * size, best / base);
        }

        // Experiment 4: predicted against measured failure rate of the median filter. The test
        // picture has no pure black or pure white pixels, so any output pixel that is 0 or 255
        // is noise that the filter failed to remove.
        System.out.println();
        System.out.println("Experiment 4: output pixels still pure black or white (predicted / measured)");
        System.out.println("| Noise | 3x3 | 5x5 | 7x7 |");
        System.out.println("|---|---|---|---|");
        for (int percent : new int[] {20, 30, 40, 50}) {
            int[][] n = addSaltAndPepper(clean, percent / 100.0, SEED);
            StringBuilder row = new StringBuilder("| " + percent + "% |");
            for (int size : new int[] {3, 5, 7}) {
                int[][] filtered = MedianFilter.apply(n, size);
                int failed = 0;
                for (int[] line : filtered) {
                    for (int value : line) {
                        if (value == 0 || value == 255) {
                            failed++;
                        }
                    }
                }
                double measured = 100.0 * failed / (SIZE * SIZE);
                row.append(String.format(" %.2f%% / %.2f%% |",
                        100 * predictedFailureRate(size, percent / 100.0), measured));
            }
            System.out.println(row);
        }
    }
}
