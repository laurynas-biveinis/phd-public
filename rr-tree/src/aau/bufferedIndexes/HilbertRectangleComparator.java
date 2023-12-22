package aau.bufferedIndexes;

import xxl.core.spatial.rectangles.Rectangle;

import java.util.Comparator;

/**
 *
 */
public class HilbertRectangleComparator implements Comparator<Rectangle> {

    private HilbertRectangleComparator() { }

    public static final Comparator<Rectangle> INSTANCE = new HilbertRectangleComparator();

    public int compare(final Rectangle o1, final Rectangle o2) {
        return HilbertPointComparator.INSTANCE.compare(o1.getCorner(false), o2.getCorner(false));
    }
}
