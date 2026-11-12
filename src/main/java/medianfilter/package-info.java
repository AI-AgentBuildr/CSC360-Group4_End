/**
 * Median filter for images, built on ImageJ.
 *
 * <p>The filtering itself ({@link medianfilter.MedianFilter}, {@link medianfilter.Median},
 * {@link medianfilter.PixelGrid}, {@link medianfilter.FilterOptions},
 * {@link medianfilter.ColorFilter}) works on plain {@code int[][]} arrays so it can be tested
 * without any image library. {@link medianfilter.ImageAdapter} converts between ImageJ images
 * and those arrays, and {@link medianfilter.MedianFilterApp} is the program entry point.
 */
package medianfilter;
