package medianfilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for {@link PixelGrid}. */
class PixelGridTest {

    /** A small 2 row by 3 column image used by several tests. */
    private static final int[][] IMAGE = {
        {1, 2, 3},
        {4, 5, 6}
    };

    /** Checks that reports width and height. */
    @Test
    void reportsWidthAndHeight() {
        PixelGrid grid = new PixelGrid(IMAGE);
        assertEquals(3, grid.getWidth());
        assertEquals(2, grid.getHeight());
    }

    /** Checks that inside positions return their own value. */
    @Test
    void insidePositionsReturnTheirOwnValue() {
        PixelGrid grid = new PixelGrid(IMAGE);
        assertEquals(1, grid.getClamped(0, 0));
        assertEquals(6, grid.getClamped(2, 1));
    }

    /** Checks that positions outside use the nearest edge pixel. */
    @Test
    void positionsOutsideUseTheNearestEdgePixel() {
        PixelGrid grid = new PixelGrid(IMAGE);
        assertEquals(1, grid.getClamped(-5, -5));
        assertEquals(3, grid.getClamped(10, -1));
        assertEquals(4, grid.getClamped(-1, 9));
        assertEquals(6, grid.getClamped(10, 10));
    }

    /** Checks that border detection depends on the radius. */
    @Test
    void borderDetectionDependsOnTheRadius() {
        PixelGrid grid = new PixelGrid(TestSupport.filled(5, 5, 0));
        assertTrue(grid.isBorder(0, 2, 1));
        assertTrue(grid.isBorder(4, 4, 1));
        assertFalse(grid.isBorder(2, 2, 1));
        assertTrue(grid.isBorder(2, 2, 3));
    }

    /** Checks that invalid grids are rejected. */
    @Test
    void invalidGridsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PixelGrid(null));
        assertThrows(IllegalArgumentException.class, () -> new PixelGrid(new int[0][0]));
        assertThrows(IllegalArgumentException.class, () -> new PixelGrid(new int[][] {{}}));
        assertThrows(IllegalArgumentException.class,
                () -> new PixelGrid(new int[][] {{1, 2}, {3}}));
    }
}
