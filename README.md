# Median Filter (ImageJ)

CSC360 Group 4 end-semester project: **Image Filtering. Understand a filtering algorithm and
implement it using ImageJ.** We chose the **median filter**, which removes "salt and pepper"
noise (random white and black dots) from an image while keeping edges sharp.

The algorithm part of this README was written from **Chapter 5 (Filters)** of the course textbook,
W. Burger and M. J. Burge, *Digital Image Processing: An Algorithmic Introduction Using Java*,
2nd ed., Springer, 2016 [[1]](#references), and we use the book's terms and notation. The
section [Sources: what came from the book](#sources-what-came-from-the-book-and-what-is-ours)
lists exactly which parts come from the book and which are our own work.

- [Sources: what came from the book](#sources-what-came-from-the-book-and-what-is-ours)
- [Terminology](#terminology-the-books-words-and-ours)
- [What we understood about the median filter](#what-we-understood-about-the-median-filter)
- [Scope](#scope-what-this-project-covers)
- [Dependencies](#dependencies)
- [The algorithm](#the-algorithm)
- [Worked examples](#worked-examples)
- [Design decisions](#design-decisions)
- [Strengths and limits](#strengths-and-limits)
- [Project layout](#project-layout)
- [Build and test](#build-and-test)
- [Run](#run)
- [Results on the sample image](#results-on-the-sample-image)
- [Analysis](#analysis-what-we-learned-about-the-median-filter)
- [Where each step of the algorithm lives in the code](#where-each-step-of-the-algorithm-lives-in-the-code)
- [Testing](#testing)
- [Team and division of work](#team-and-division-of-work)
- [References](#references)

## Sources: what came from the book and what is ours

| Part of this README | Where it comes from |
|---|---|
| What a filter is, the filter region and its support, linear versus nonlinear filters | Book, Sec. 5.1 (pp. 89-91) |
| The median filter: definition (Eq. 5.40 and 5.41), even-sized regions (Eq. 5.42), region size and the centre index (Eq. 5.43), the 3x3 example (Fig. 5.17), effect on structures (Fig. 5.19), Java code of arbitrary size (Prog. 5.5) | Book, Sec. 5.4.2 (pp. 107-110) |
| Why the median is robust against outliers; the weighted median (not implemented here) | Book, Sec. 5.4.3 (p. 109 onwards) |
| Handling the image borders (the four padding methods, our choice of edge clamping) | Book, Sec. 5.5.2 (pp. 113-114) |
| Test on small cases whose result can be predicted | Book, Sec. 5.5.3 (p. 114) |
| ImageJ's own median filter, `RankFilters` | Book, Sec. 5.6.3 (p. 116) |
| Colour images: scalar (per channel) median filter and why new colours can appear; vector median (not implemented) | Book, Sec. 15.2.1 and 15.2.2 (p. 378) |
| Maven layout, JUnit tests, Javadoc, the dependency analysis between our classes, the division of work | Our own work and standard practice, not from the book |
| "What we understood" and the hand trace | Our own summary of the book's explanation, checked against our code |
| The error measures (mean squared error, PSNR), the noise generator, the experiments and the cost measurements | Standard measures that are not defined in the book; the experiments are our own |
| The probability argument for when the median fails (Analysis, part 2) | Our own derivation: the book says that the "majority" of the values decides the median (Sec. 5.4.3); the binomial calculation that turns this into a failure rate is ours |
| The statement that Gaussian noise suits averaging filters better | General background knowledge, not stated in the book |

## Terminology: the book's words and ours

The book calls the neighbourhood the **filter region R** and the pixel list the **pixel vector A**.
Our code and comments say *window* and `window`. The table connects the two so the README, the
book and the code can be read together.

| Book (Burger and Burge) | Meaning | In our README and code |
|---|---|---|
| I(u, v) and I'(u, v) | Source and result pixel at column u, row v | `x` is u and `y` is v; `int[][]` indexed `[y][x]` |
| **Filter region R** (support) | The set of source pixels used for one result pixel | window, `windowSize` x `windowSize` |
| **Hot spot** | The centre pixel of the region, the one being replaced | centre pixel |
| Region size (2r + 1) x (2r + 1) | Side length 2r + 1 for radius r | `FilterOptions.radiusFor` gives r = (windowSize - 1) / 2 |
| **Pixel vector A** = (a0, ..., a2n) | The values of R, sorted | the `window` array in `MedianFilter.apply`, sorted in `Median.median` |
| Median a_n, n = 2(r^2 + r) | The centre element of the sorted vector (Eq. 5.41, 5.43) | `Median.median` returns `sorted[(sorted.length - 1) / 2]` |
| **Salt-and-pepper noise**, **outliers** | Isolated white and black pixels | same words |
| **Nonlinear filter**, rank order statistic | The median is not a weighted sum of the pixels | same words |
| **Box filter** (linear) | Replace the pixel by the mean of a 3x3 region | "mean 3x3" in our tables, our comparison filter |
| **Padding**, "border pixels extend beyond the image boundaries" (Method 3B) | How missing pixels outside the image are supplied | **edge clamping**, `PixelGrid.getClamped` |
| **Scalar (monochromatic) median filter** | The median applied to each colour channel on its own | `ColorFilter`: red, green and blue separately |

## Scope: what this project covers

The assignment topic is **"Image Filtering: understand a filtering algorithm and implement it
using ImageJ."**

| In scope | How we cover it |
|---|---|
| Understand one filtering algorithm | The median filter: what it does, why it works, when it fails (see [The algorithm](#the-algorithm) and [Analysis](#analysis-what-we-learned-about-the-median-filter)) |
| Implement it ourselves | Our own code in `MedianFilter` and `Median`, not a call to ImageJ's built-in filter |
| Use ImageJ | To open, hold and show images (`ImageAdapter`, `MedianFilterApp`) |
| Grayscale and colour images | 8-bit and 16-bit grayscale, and RGB colour (each channel filtered separately) |
| Different window sizes (filter region sizes) | Any odd size from 3 up (3x3, 5x5, 7x7, ...) |
| Image borders | Edge clamping: a missing neighbour is replaced by the nearest edge pixel |
| Evidence that it works | 42 JUnit tests, plus measured experiments on a noisy test picture |
| Professional project form | Maven layout, Javadoc on all code, this README |

| Out of scope | Why |
|---|---|
| Other filters (Gaussian blur, sharpening, edge detection) | The topic asks for one algorithm, understood properly. A simple mean filter appears only as a comparison. |
| ImageJ's own median filter (Process > Filters > Median, class `RankFilters`, book Sec. 5.6.3) | We implement the algorithm ourselves; that is the point of the assignment. |
| Weighted median filter (book Sec. 5.4.3) and vector median filter (book Sec. 15.2.2) | Both are described in the book. We implement the standard median only, as the topic asks for one algorithm understood properly. |
| 32-bit floating-point images | Their pixels are decimal numbers that do not fit our integer arrays. They are rejected with a clear message. |
| Non-square or round filter regions | Only square regions are used. ImageJ's own `RankFilters` uses an approximately circular region (book Sec. 5.6.3); a square region is the simplest case and the one the book's Prog. 5.5 uses. |
| Speed optimisations (histogram-based median) | Sorting is the clearest method to learn and fast enough here (see the cost measurements). |
| Noise other than salt-and-pepper | We analyse salt-and-pepper noise, the case the median filter is made for. Other noise is mentioned as a limit. |
| A full graphical user interface, batch processing, image stacks | The program opens one image and shows the original and the filtered result. |

## Dependencies

### Libraries and tools

| Dependency | Used by | Why we need it |
|---|---|---|
| **ImageJ** (`net.imagej:ij`) | `ImageAdapter` and `MedianFilterApp` only | Opens, holds and shows images |
| **JUnit 5** | `src/test` only (test scope) | Runs the unit tests |
| **Java standard library** | `Median` (`java.util.Arrays`), `Experiments` (`java.awt.image`, `javax.imageio`, `java.util.Random`) | Sorting; reading and writing PNG files; seeded random numbers |
| **Maven plugins**: surefire, javadoc, exec | The build | Run tests, build the Javadoc pages, run the program |

### Between our own classes

We worked this out from the source code (which classes each class refers to). There are no
circular dependencies, so the code can be built and merged in layers:

```
Layer 4   MedianFilterApp ........ also uses ImageJ
              |
              v
Layer 3   ImageAdapter ........... also uses ImageJ
              |            \
              v             \
Layer 2   ColorFilter        \
              |               \
              v                v
Layer 1   MedianFilter  <-------'
              |
     +--------+---------+
     v        v         v
Layer 0   PixelGrid   FilterOptions   Median      (depend on nothing of ours)

Also: ColorFilter uses PixelGrid.  Experiments uses MedianFilter, PixelGrid, FilterOptions.
```

| Class | Uses | Used by |
|---|---|---|
| `PixelGrid` | nothing | `MedianFilter`, `ColorFilter`, `Experiments` |
| `FilterOptions` | nothing | `MedianFilter`, `Experiments` |
| `Median` | nothing | `MedianFilter` |
| `MedianFilter` | `PixelGrid`, `FilterOptions`, `Median` | `ColorFilter`, `ImageAdapter`, `Experiments` |
| `ColorFilter` | `MedianFilter`, `PixelGrid` | `ImageAdapter` |
| `ImageAdapter` | `ColorFilter`, `MedianFilter`, ImageJ | `MedianFilterApp` |
| `MedianFilterApp` | `ImageAdapter`, ImageJ | (entry point) |
| `Experiments` | `MedianFilter`, `PixelGrid`, `FilterOptions` | (run separately) |

### What this told us

- **Three classes depend on nothing of ours** (`PixelGrid`, `FilterOptions`, `Median`). They are
  the foundation: built first and tested on their own.
- **`MedianFilter` is the centre.** Everything else (colour support, the ImageJ bridge, the
  experiments) builds on it.
- **Only two classes touch ImageJ.** The whole algorithm is plain Java on `int[][]` arrays, so it
  is fully unit tested without any image library. If ImageJ causes trouble, only
  `ImageAdapter` and `MedianFilterApp` are affected.
- **The dependency order is also the team's merge order** (see
  [Team and division of work](#team-and-division-of-work)).

## The algorithm

*Source: Burger and Burge [[1]](#references), Sec. 5.1 (what a filter is) and Sec. 5.4.2 (the median
filter). The notation is the book's; the worked 3x3 example with a noisy centre is ours.*

### What a filter is (book Sec. 5.1)

A filter computes each new pixel value I'(u, v) from a **set of source pixels**, not from the
single pixel I(u, v) as a point operation would. The source pixels lie in a **filter region R**
around the current position (u, v), and the size of R is the **support** of the filter. Filters
are either **linear** (the result is a weighted sum of the source pixels, for example the 3x3
box filter that takes their mean) or **nonlinear**. The median filter is a nonlinear filter.

### The problem

A grayscale image is a grid of pixels, each a number from 0 (black) to 255 (white). Noise from a
faulty sensor or a bad transmission turns a few pixels into extreme values, for example a pure
white 255 pixel in the middle of a dark area. This is **salt-and-pepper noise**, and the wrong
pixels are **outliers**: values far from everything around them.

### The idea (book Eq. 5.40 and 5.41)

> Replace every pixel by the **median** of the pixels in the filter region R around it:
> I'(u, v) = median of I(u + i, v + j) over all (i, j) in R.

The **median** of a set of 2n + 1 values is defined in the book as the **centre value a_n after
sorting** them into the ordered vector A = (a0, ..., a2n), where a_i <= a_(i+1). An outlier ends
up at one end of the sorted vector, so it can never be the centre value. The book calls the median
a **robust** measure because one exceptionally high or low value cannot influence it much (Sec.
5.4.3), unlike the arithmetic mean.

**Size of the region (Eq. 5.43).** R is a square of side 2r + 1 for a radius r, so it holds
(2r + 1)^2 = 4(r^2 + r) + 1 values, and the index of the centre element is n = 2(r^2 + r).
Radius r = 1 gives a 3x3 filter with n = 4; r = 2 gives 5x5 with n = 12. Our
`FilterOptions.radiusFor` computes this r.

**Even-sized regions (Eq. 5.42).** If R had an even number of elements, the book defines the
median as the mean of the two centre values. That creates pixel values that were not in the
image. With an odd number of elements the median is always one of the existing pixel values. We
therefore **only allow odd window sizes**.

### Steps

For every pixel position (u, v), with u the column and v the row:

1. Take the filter region R: a square (2r + 1) x (2r + 1) centred on the hot spot (u, v). A 3x3
   region covers the pixel and its 8 neighbours; 5x5 covers 25 pixels, and so on.
2. Copy the values of R into a vector A (9 values for 3x3).
3. **Sort** A.
4. Take the **centre** element a_n (the 5th of 9).
5. Write it into the same position (u, v) of a **new output image**.

Repeat for every pixel. The output is the filtered image. The book's Java code (Prog. 5.5) works
the same way: it first makes a copy of the image, `ip.duplicate()`, reads the region from the copy
and writes the result into the original, so that already-filtered pixels never feed into later
ones. We write into a new array instead, which has the same effect.

### One filter region in detail

```
 50   52   51
 49  255   53        values: 50 52 51 49 255 53 50 51 52
 50   51   52        sorted: 49 50 50 51 51 52 52 53 255
                                          ^ centre a_4 (5th of 9) = 51
```

The noisy 255 becomes **51**, which fits its neighbours. The mean of the same nine numbers is
about 74, which would leave a bright smudge: this is the difference between median and mean.

The book works one example of its own (Fig. 5.17): the region with rows `3 7 2`, `1 0 0`, `9 5 8`
sorts to `0 0 1 2 3 5 7 8 9`, so the median is 3.

### What the filter does to image structures (book Fig. 5.19 and Fig. 5.18)

With a 3x3 median filter, isolated dots and thin lines are eliminated, a step edge is left
unchanged, and a corner is rounded off. Compared with the linear box filter, the median filter
removes salt-and-pepper noise much more completely and keeps the remaining structures largely
intact; the box filter only weakens the dots and blurs the whole image. The book also notes a
drawback: the median filter creates small flat patches that noticeably affect sharpness. Our
worked examples below reproduce these effects with real numbers.

## What we understood about the median filter

The aim of this project is to understand one filtering algorithm properly, not just to run it.
This is what we took from the book chapter, from our own code and from our experiments.

1. **A filter builds each new pixel from a region of old pixels.** The median filter takes the
   region R around the hot spot, sorts its values and keeps the centre one (book Eq. 5.40, 5.41).
2. **The median picks a value that already exists; the mean computes a new one.** With an odd
   number of values the result is one of the original pixel values, so no invented gray levels. The
   mean (box filter) mixes in the outlier and smears it over the whole neighbourhood.
3. **Sorting is how we find the middle.** The median is a rank order statistic: after sorting, the
   smallest and largest values sit at the ends of A and the centre is the middle rank. Extreme
   noise values always land at the ends, so they never become the centre. This is why the book
   calls the median robust.
4. **Noise disappears because it is the minority.** To become the median, noise must make up at
   least half of the region (5 of 9 for 3x3). With 10% noise that almost never happens. With
   heavy noise it does, which is why 3x3 fails at 40% noise (Analysis, part 2).
5. **Straight edges survive because of the same majority rule.** On an edge, most of the region
   belongs to one side, so the centre value is a real value from that side, not a blend. The
   mean produces in-between values and blurs the edge.
6. **Thin lines, dots and corners are lost for the same reason.** A structure that fills less
   than half the region is in the minority, so the filter treats it like noise (book Fig. 5.19).
   This is the price of the filter and the reason the window size is a trade-off.
7. **Each step of our design follows from the algorithm.** A new output image, so that
   already-filtered pixels never feed later windows. Edge clamping, so border windows are full
   size and still have a centre. Odd window sizes, so there is a centre and no averaging. Red,
   green and blue separately, because a colour has no natural order to sort by.

### Tracing the algorithm by hand

We filtered a small 4x4 image with a 3x3 window. It has one white outlier (255) and one black
outlier (0). The values and the result were produced by this project's code; we also worked the
two windows below by hand.

```
input                   output (median 3x3)
 10  12  11  10          11  11  11  11
 11 255  12  11          11  11  11  11
 10  11   0  12          11  11  11  11
 12  10  11  11          11  11  11  11
```

| Pixel (u, v) | Region R read row by row | Sorted vector A | Centre a_4 |
|---|---|---|---|
| (1, 1), the white outlier | 10 12 11 / 11 **255** 12 / 10 11 **0** | 0 10 10 11 11 **11** 12 12 255 | **11** |
| (0, 0), a corner | 10 10 12 / 10 10 12 / 11 11 **255** (clamped) | 10 10 10 10 11 **11** 12 12 255 | **11** |

In the first row both outliers sit inside the same region, and sorting pushes them to the two ends
(0 at the start, 255 at the end), so the centre is 11. In the second row the corner pixel has no
neighbours above or to the left, so edge clamping repeats the nearest edge pixels (the top row
and left column of the region are copies of the edge); the window is still full size and the centre is again
11. The result is a flat image: both outliers are gone and nothing new was invented.

## Worked examples

All numbers below were produced by this project's code (window 3x3). The mean filter used for
comparison is what the book calls the **box filter**.

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
| What about pixels at the border, whose window sticks out of the image? | **Edge clamping**: a missing neighbour is replaced by the nearest edge pixel | In the book's classification (Sec. 5.5.2) this is **Method 3B**, padding where "the border pixels extend beyond the image boundaries". The book says it causes only minor artifacts, is simple to compute and is often the method of choice. It keeps the region full size, so there is always an odd number of values and a true centre. The other options are: Method 1, set the border to a constant (the image effectively shrinks); Method 2, leave the border unfiltered (the book's own Prog. 5.5 does this, which leaves noise there); Method 3A, constant padding (artifacts at the border); 3C, mirror; 3D, periodic repetition. |
| Which window size? | Odd, 3 or larger (default 3) | Odd gives a centre pixel and no new values (book Eq. 5.41 and 5.42). The book's rule of thumb is that structures smaller than about half the filter size are eliminated (Sec. 5.4.2). Our own sharper version: a structure is removed when it fills less than half the window, so a 3x3 window cannot remove a 3x3 blob but a 5x5 window can. Bigger windows blur more detail. |
| Colour images | Filter **red, green and blue separately** and recombine | This is the **scalar (monochromatic) median filter** of book Sec. 15.2.1: no natural ordering exists for colour vectors, so each channel is treated as its own grayscale image. ImageJ and Photoshop do the same. The book warns that each channel's median may come from a different pixel of the region, so the result can be a colour that was not in the original image. The vector median filter (Sec. 15.2.2) avoids this but is not implemented here. |
| Why plain `int[][]` arrays in the filter? | The filter does not depend on ImageJ | The book writes the filter as an ImageJ `PlugInFilter` working on an `ImageProcessor` (Prog. 5.5). We keep the algorithm in plain Java so it can be unit tested without any image library; `ImageAdapter` is the only bridge to ImageJ. |

### Cost

For an image of W x H pixels and a window of k x k values, each pixel sorts k*k numbers, so the
total work grows roughly with W * H * k*k * log(k*k). We measured it: see
[Analysis](#analysis-what-we-learned-about-the-median-filter). Faster methods exist (histogram
based), but sorting is the clearest to learn and fast enough for this project. The book's own
Prog. 5.5 also sorts, and it says its examples are deliberately simple rather than efficient (Sec. 5.5.1).

## Strengths and limits

**Good at:** salt-and-pepper noise; keeping straight edges sharp; never inventing new gray levels
when the region has an odd number of elements (book Sec. 5.4.2). On colour images, filtering the
channels separately can produce colours that were not in the original (book Sec. 15.2.1).

**Not good at:**
- **Thin lines.** A one pixel wide line is an "outlier" too, so it is erased (book Fig. 5.19):

  ```
  input (a 1px bright line)       median 3x3
   10  10 200  10  10              10  10  10  10  10
   10  10 200  10  10     ->       10  10  10  10  10
   10  10 200  10  10              10  10  10  10  10
  ```
- **Fine detail and sharp corners** are rounded off (book Fig. 5.19), more so with larger windows,
  and small flat patches appear (book Fig. 5.18).
- **Smooth, grain-like noise (Gaussian noise)** is reduced less well than by averaging filters
  (general background knowledge, not from the book).
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

## Run

```
mvn compile exec:java -Dexec.args="src/main/resources/samples/noisy.png 3"
```

The first argument is the image file (use any image of your own, too); the optional second is the
window size (odd, at least 3; default 3). Two ImageJ windows open: the original and the filtered image.

Bad input is reported instead of crashing: a missing file, a window size that is not a whole
number, or an even or too small window size.

## Results on the sample image

`src/main/resources/samples/noisy.png` is a 256x256 test picture (soft gradient, a bright disc and
a dark square) with **10% salt-and-pepper noise** added: 5% of pixels forced to black, 5% to
white, at random (fixed seed, so it is reproducible). `clean.png` is the same picture without
noise, so we can measure how close each filter gets to the original.

**Input and output of the filter.** Left is the noisy input, right is the output of the 3x3 median
filter (the same call the app makes: `ImageAdapter` -> `MedianFilter.apply` with window size 3).

![input and output of the median filter](docs/images/input_output.png)

Left to right below: noisy input, median 3x3, mean 3x3.

![noisy, median 3x3, mean 3x3](docs/images/before_after.png)

| Image | Mean squared error vs clean (lower is better) | PSNR (higher is better) |
|---|---|---|
| Noisy input | 2067.4 | 15.0 dB |
| **Median 3x3** | **12.8** | **37.0 dB** |
| Median 5x5 | 12.8 | 37.1 dB |
| Mean 3x3 (for comparison) | 304.3 | 23.3 dB |

*Mean squared error* is the average of (filtered pixel - original pixel)^2 over all pixels.
*PSNR* (peak signal-to-noise ratio) turns that into decibels: PSNR = 10 * log10(255^2 / MSE).

What the numbers say:
- The median filter removes almost all of the noise (error drops from 2067 to about 13); the
  mean filter only reduces it to 304, because it spreads each noisy pixel over its neighbours.
- At 10% noise a 3x3 and a 5x5 window do about equally well on this picture. A bigger window is
  only worth its extra blur and cost when noise comes in larger blotches.
- The remaining small error comes from pixels at the corners of the square, where the window mixes
  two regions (the "rounded corners" limit above).

(These pictures and tables were produced with this project's own classes by `Experiments`; run it
yourself with `mvn compile exec:java -Dexec.mainClass=medianfilter.Experiments
-Dexec.args="src/main/resources/samples docs/images"`.)

## Analysis: what we learned about the median filter

Every number in this section is produced by the `Experiments` class on the sample picture with a
fixed random seed, so anyone can reproduce it.

### 1. How noise level and window size interact

PSNR in decibels against the clean picture (higher is better):

| Noise | Noisy input | Median 3x3 | Median 5x5 | Median 7x7 | Mean 3x3 |
|---|---|---|---|---|---|
| 5% | 17.9 | 39.2 | 39.2 | 38.2 | 26.1 |
| 10% | 15.0 | 37.0 | 37.1 | 36.6 | 23.3 |
| 20% | 11.9 | 30.0 | 34.2 | 33.7 | 19.9 |
| 30% | 10.1 | 23.8 | 31.5 | 31.6 | 17.6 |
| 40% | 8.9 | 18.4 | 29.0 | 30.3 | 15.9 |
| 50% | 7.9 | 14.7 | 24.2 | 28.6 | 14.6 |

- **Up to 10% noise, a 3x3 window is enough.** At 5% and 10% the 3x3 and 5x5 windows score the
  same (39.2 dB; 37.0 and 37.1 dB). A 7x7 window is slightly worse (38.2 and 36.6 dB): it also
  removes the noise, but rounds the corners of the square and the disc.
- **From 20% noise, 3x3 starts to fail** and bigger windows pay off. At 40% noise: 3x3 gives
  18.4 dB, 5x5 gives 29.0 dB and 7x7 gives 30.3 dB.
- **At 50% noise, 7x7 holds up best** (28.6 dB against 14.7 dB for 3x3).
- **The mean filter is far behind everywhere**, for example 23.3 dB against 37.0 dB for the
  median at 10% noise.
- **Rule of thumb: use the smallest window that removes the noise.** Small windows keep more
  detail; large ones are only worth it when the noise is heavy.

### 2. Why: when does the median fail?

In a window of n values the median is value number (n + 1) / 2 after sorting. Black noise pixels
sit at the bottom of the sorted list and white ones at the top. So the median is itself a noise
value **only when at least half the window (rounded up) is black, or at least half is white**:
5 of 9 pixels for a 3x3 window, 13 of 25 for 5x5, 25 of 49 for 7x7.

If each pixel is black with probability (noise / 2), the chance of failure is twice the upper
tail of a binomial distribution with n trials. We compared that prediction with the measurement.
The test picture has no pure black or white pixels, so any output pixel that is 0 or 255 is noise
the filter failed to remove. Each cell is *predicted / measured*:

| Noise | 3x3 | 5x5 | 7x7 |
|---|---|---|---|
| 20% | 0.18% / 0.18% | 0.00% / 0.00% | 0.00% / 0.00% |
| 30% | 1.13% / 1.14% | 0.00% / 0.00% | 0.00% / 0.00% |
| 40% | 3.92% / 4.01% | 0.07% / 0.09% | 0.00% / 0.00% |
| 50% | 9.79% / 10.12% | 0.67% / 0.79% | 0.02% / 0.06% |

- **Prediction and measurement agree closely** (for example 3.92% against 4.01% for 3x3 at 40%
  noise). Small differences are expected: pixels at the border reuse edge values through edge
  clamping, so the windows there are not made of independent pixels.
- **Bigger windows fail far less often**, because they need many more bad pixels on the same
  side: 13 of 25 instead of 5 of 9.
- **A few failures still cost a lot.** At 40% noise only about 4% of the 3x3 output pixels fail,
  but each is a pure black or white pixel roughly 150 gray levels from the truth, which is
  why the error is large (a mean squared error near 940, or 18.4 dB).

### 3. Cost

Each pixel sorts k*k values, so the work grows with the window area:

| Window | Values sorted per pixel | Relative time |
|---|---|---|
| 3x3 | 9 | 1.0x |
| 5x5 | 25 | 3.3x |
| 7x7 | 49 | 5.7x |
| 9x9 | 81 | 9.9x |

The relative time follows the number of values sorted (9, 25, 49, 81 are 1x, 2.8x, 5.4x and 9x).
The exact ratios change from run to run and from computer to computer; the trend is what
matters. A bigger window removes more noise but costs more and blurs more.

### What we conclude

- The median filter is the right tool for salt-and-pepper noise: it removes the spikes without
  inventing new gray values, and keeps straight edges sharp.
- It works as long as less than half of every window is noise. The window size sets that
  limit, and the probability calculation predicts the failures we measured.
- The price of a bigger window is detail (corners, thin lines) and running time, so the choice
  is a trade-off, not "bigger is better".

## Where each step of the algorithm lives in the code

| Algorithm step | Book | Code |
|---|---|---|
| Read the image and get its pixels | Sec. 5.5.1 (ImageJ `getPixel()` / `putPixel()`) | `ImageAdapter.toArray` |
| Check the image and window size | | `PixelGrid` constructor, `FilterOptions.validateWindowSize` |
| Region radius r (3 -> 1, 5 -> 2) | Eq. 5.43 | `FilterOptions.radiusFor` |
| Collect the filter region R around a pixel into the vector A, with padding at the border | Prog. 5.5, lines 24-30; Sec. 5.5.2 Method 3B | `MedianFilter.apply` (two inner loops), `PixelGrid.getClamped` |
| Sort A and take the centre element a_n | Eq. 5.41; Prog. 5.5, lines 32-33 | `Median.median` |
| Write the result into a new image | Prog. 5.5 (copy of the image) | `MedianFilter.apply` (the `result` array) |
| Colour: filter R, G and B separately | Sec. 15.2.1 | `ColorFilter.applyRgb` |
| Turn the result back into an ImageJ image and show it | | `ImageAdapter.fromArray`, `MedianFilterApp.main` |
| Reproduce the measurements in this README | | `Experiments.main` |

## Testing

The book advises (Sec. 5.5.3) not to start with large images but with small test cases whose
outcome can easily be predicted, because a faulty filter still "does something" to the image. We
follow that advice: 42 JUnit 5 tests check the algorithm against hand-worked results:

- the 3x3 example above (255 becomes 51);
- an isolated spike vanishes, a flat image is unchanged, a straight edge stays sharp;
- corner pixels, a 1x1 image, and a spike next to the border;
- a 5x5 window removes a blob that a 3x3 window cannot;
- the input image is never modified;
- invalid images and window sizes are rejected;
- colour channels are filtered independently;
- the experiment tools: seeded noise, the comparison mean filter, error and PSNR formulas, and the
  failure-rate prediction (`ExperimentsTest`).

## Team and division of work

CSC360 Group 4.

| Member | Role | Files | Commits |
|---|---|---|---|
| Dhyan (Member 1) | Project setup, ImageJ integration and experiments | `.gitignore`, `pom.xml`, `README.md`, `package-info.java`, `ImageAdapter.java`, `MedianFilterApp.java`, `Experiments.java`, sample images | 10 |
| Shashwat (Member 3) | Foundations: pixel access, edge handling, window size rules | `PixelGrid.java`, `FilterOptions.java` | 5 |
| Dev (Member 2) | The algorithm itself, then the final README and the `Experiments` tests | `Median.java`, `MedianFilter.java`, `ColorFilter.java`, `ExperimentsTest.java`, the final `README.md` | 7 |
| Krishna (Member 4) | Testing and verification | `TestSupport`, `MedianTest`, `PixelGridTest`, `FilterOptionsTest`, `MedianFilterTest`, `ColorFilterTest` (31 tests), and a Javadoc line in `package-info.java` | 6 |

### Why we divided the work this way

1. **One class, one job, one owner.** The program already splits into small classes with a single
   responsibility (see the project layout). Giving each person whole classes means we work in
   different files, so our branches rarely conflict when merged.
2. **We followed the dependencies.** The code is built in layers: setup, then pixel access and
   options, then the filter, then the ImageJ app, then tests. `MedianFilter` uses `PixelGrid` and
   `FilterOptions`, the app uses the filter, and the tests use everything. So the work is merged in
   that order, and each person starts from a `main` that already has the parts they depend on.
3. **The risky part is isolated.** The filter works on plain `int[][]` arrays, and only
   `ImageAdapter` touches ImageJ. One person owns that boundary (Member 1), so a problem with the
   library cannot block the people working on the algorithm.
4. **The tests are written by someone who did not write the code.** Krishna wrote the tests for the
   filter classes; the tests for `Experiments` (11 tests) were added by Dev with Krishna's
   agreement. Krishna tests against the
   hand-worked examples in this README (for example 255 becomes 51), not against the
   implementation. If the author of a class also wrote its tests, they could repeat their own
   misunderstanding; an independent tester is more likely to catch it.
5. **Everyone owns a part they can explain.** Edge clamping (Shashwat), sorting and the median
   (Dev), ImageJ integration and setup (Dhyan), testing (Krishna). Each part has a
   concept that matches the project topic, so every member understands the algorithm from
   their own angle.
6. **The load is balanced into small steps.** Each person makes at least five commits, and each
   commit is one small, understandable step (for example "add edge clamping", then "add border
   detection"), so the history shows how the work grew.

### How we worked together

- Each member worked on their own feature branch and opened a pull request into `main`.
- Each pull request lists its author's own commits, so the repository history shows who built what.
- Pull requests were merged with "Create a merge commit" so each member's own commits stay visible
  in the history.

## References

[1] W. Burger and M. J. Burge, *Digital Image Processing: An Algorithmic Introduction Using Java*,
2nd ed., Texts in Computer Science. London: Springer, 2016. ISBN 978-1-4471-6683-2,
DOI 10.1007/978-1-4471-6684-9. Used: Chapter 5 "Filters" (Sec. 5.1, 5.4.2, 5.4.3, 5.5.2, 5.5.3,
5.6.3) and Sec. 15.2 "Nonlinear Color Filters". Page numbers are the printed page numbers.

[2] ImageJ, image processing and analysis in Java, W. Rasband, National Institutes of Health,
https://imagej.nih.gov/ij/.
