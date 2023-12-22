package aau.bufferedIndexes;

import org.junit.Test;
import xxl.core.spatial.points.DoublePoint;
import xxl.core.spatial.points.Point;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for HilbertPointComparator
 */
public class HilbertPointComparatorTest {

    @Test
    public void compare1() {
        double p1coords[] = {555505.0D, 6322903.0D};
        double p2coords[] = {555498.0D, 6322706.0D};
        final Point p1 = new DoublePoint(p1coords);
        final Point p2 = new DoublePoint(p2coords);

        assertEquals(-1, HilbertPointComparator.INSTANCE.compare(p1, p2));
    }
}
