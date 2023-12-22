package aau.bufferedIndexes;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for AggregateStats
 */
public class AggregateStatsTest {

    @Test
    public void deviation() {
        final AggregateStats s = new AggregateStats();
        s.registerValue(1.0D);
        s.registerValue(2.0D);
        Assert.assertEquals(s.deviation(), 0.001, 0.70711);
    }

}
