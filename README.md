# Median Filter (ImageJ)

CSC360 Group 4 end-semester project: **Image Filtering. Understand a filtering algorithm and
implement it using ImageJ.** We chose the **median filter**, which removes "salt and pepper"
noise (random white and black dots) from an image while keeping edges sharp.

- [The algorithm](#the-algorithm)
- [Worked examples](#worked-examples)
- [Design decisions](#design-decisions)
- [Strengths and limits](#strengths-and-limits)
- [Project layout](#project-layout)
- [Build and test](#build-and-test)

## The algorithm

### The problem

A grayscale image is a grid of pixels, each a number from 0 (black) to 255 (white). Noise from a
faulty sensor or a bad transmission turns a few pixels into extreme values, for example a pure
white 255 pixel in the middle of a dark area. These wrong pixels are **outliers**: values far
from everything around them.

### The idea

> Replace every pixel by the **median** of the pixels in a small square around it.

The **median** is the middle value after sorting. An outlier ends up at one end of the sorted
list, so it can never be the middle value. That is why the median ignores noise.

### Steps

For every pixel at position (x, y):

1. Take a square **window** centred on (x, y). A 3x3 window covers the pixel and its 8 neighbours;
   5x5 covers 25 pixels, and so on. The window size is always odd so there is a centre pixel.
2. Copy the window's values into a list (9 values for 3x3).
3. **Sort** the list.
4. Take the **middle** value (the 5th of 9).
5. Write that value into the same position (x, y) of a **new output image**.

Repeat for every pixel. The output is the filtered image.

### One window in detail

```
 50   52   51
 49  255   53        values: 50 52 51 49 255 53 50 51 52
 50   51   52        sorted: 49 50 50 51 51 52 52 53 255
                                          ^ middle (5th of 9) = 51
```

The noisy 255 becomes **51**, which fits its neighbours. The average of the same nine numbers is
about 67, which would leave a bright smudge: this is the difference between median and mean.

## Worked examples

All numbers below were produced by this project's code (window 3x3).

### 1. Removing noise

A flat gray image (value 10) with one white spike (255) and one black spike (0):

```
input                       median 3x3                  mean 3x3 (for comparison)
 10  10  10  10  10          10  10  10  10  10          10  37  37  37  10
 10  10 255  10  10          10  10  10  10  10          10  37  37  37  10
 10  10  10  10  10          10  10  10  10  10           9  36  36  37  10
 10   0  10  10  10          10  10  10  10  10           9   9   9  10  10
 10  10  10  10  10          10  10  10  10  10           9   9   9  10  10
```

The median filter restores a perfectly clean image. The mean filter does not remove the noise: it
spreads each spike over its whole neighbourhood (one 255 pixel turns nine pixels into 37).

### 2. Keeping edges sharp

An edge between a dark region (0) and a bright region (200):

```
input                     median 3x3                mean 3x3
 0  0  0 200 200 200       0  0  0 200 200 200       0  0  67 133 200 200
 0  0  0 200 200 200       0  0  0 200 200 200       0  0  67 133 200 200
 0  0  0 200 200 200       0  0  0 200 200 200       0  0  67 133 200 200
```

The median filter leaves the edge exactly as it was. The mean filter blurs it into a ramp (67, 133).
Reason: near an edge the window holds mostly one side's values, so the middle value is one of the
real values, never an in-between invented number.

## Design decisions

| Question | Our choice | Why |
|---|---|---|
| Where is the result written? | Into a **new** image | If we wrote into the input, later pixels would be computed from already-filtered values and the result would be wrong. |
| What about pixels at the border, whose window sticks out of the image? | **Edge clamping**: a missing neighbour is replaced by the nearest edge pixel | Keeps the window full size, so there is always an odd number of values and a true middle. Alternatives (leave border unfiltered, shrink the window) leave noise at the border or change the maths. |
| Which window size? | Odd, 3 or larger (default 3) | Odd gives a centre pixel. Bigger windows remove bigger blotches but blur more detail (a 3x3 window cannot remove a 3x3 blob; a 5x5 can). |
| Colour images | Filter **red, green and blue separately** and recombine | Sorting colours as one number is meaningless; channel-by-channel is the standard simple approach. |
| Why plain `int[][]` arrays in the filter? | The filter does not depend on ImageJ | Lets us unit test the algorithm without any image library; `ImageAdapter` is the only bridge to ImageJ. |

### Cost

For an image of W x H pixels and a window of k x k values, each pixel sorts k*k numbers, so the
total work grows roughly with W * H * k*k * log(k*k). Doubling the window side makes the filter
about four to five times slower. Faster methods exist (histogram based), but sorting is the
clearest to learn and fast enough for this project.

## Strengths and limits

**Good at:** salt-and-pepper noise; keeping straight edges sharp; never inventing new gray levels.

**Not good at:**
- **Thin lines.** A one pixel wide line is an "outlier" too, so it is erased:

  ```
  input (a 1px bright line)       median 3x3
   10  10 200  10  10              10  10  10  10  10
   10  10 200  10  10     ->       10  10  10  10  10
   10  10 200  10  10              10  10  10  10  10
  ```
- **Fine detail and sharp corners** are rounded off, more so with larger windows.
- **Smooth, grain-like noise (Gaussian noise)** is reduced less well than by averaging filters.
- It is slower than a simple average for large windows.

## Project layout

The project follows **Maven's standard directory layout**, so Maven finds everything without extra
configuration.

```
median-filter/
├── pom.xml                              Maven build file (dependencies, plugins)
├── README.md
├── .gitignore
├── docs/images/                         before/after pictures used in this README
└── src/
    ├── main/
    │   ├── java/medianfilter/           program code (package "medianfilter")
    │   │   ├── MedianFilterApp.java     entry point: opens an image, shows original and filtered
    │   │   ├── ImageAdapter.java        ImageJ image <-> int[][] arrays
    │   │   ├── MedianFilter.java        the median filter (grayscale)
    │   │   ├── ColorFilter.java         the same filter applied to R, G and B separately
    │   │   ├── Median.java              sort and pick the middle value
    │   │   ├── PixelGrid.java           pixel access with edge clamping
    │   │   ├── FilterOptions.java       window size checks
    │   │   └── package-info.java
    │   └── resources/samples/           sample images (clean.png, noisy.png)
    └── test/
        └── java/medianfilter/           JUnit 5 tests
```

| Maven folder | What goes there |
|---|---|
| `src/main/java` | Program source code |
| `src/main/resources` | Non-code files that belong to the program (our sample images) |
| `src/test/java` | Test code (JUnit), compiled and run only by `mvn test` |
| `target/` | Everything Maven generates (compiled classes, jar, Javadoc). Never committed: it is in `.gitignore`. |

## Build and test

Requirements: JDK 11 or newer, Apache Maven 3.8 or newer.

| Task | Command |
|------|---------|
| Compile | `mvn compile` |
| Run all tests | `mvn test` |
| Generate Javadoc | `mvn javadoc:javadoc` (open `target/site/apidocs/index.html`) |
