/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.core.math.statistics.parametric.aggregates;

import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.operators.mappers.TemporalAggregator;
import xxl.core.pipes.operators.mappers.TemporalAggregator.TemporalAggregatorSA;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.CursorSource;

/**
 * Provides the same functionality as {@link Average} but
 * keeps the count information. Hence, the incrementally
 * computed aggregate consists of an Object array whose
 * first component is the current average and the second
 * component is the counter.
 * 
 * @link Average
 */
public class CountAwareAverage extends AggregationFunction<Number,Number[]> {

    /** 
     * Function call for incremental aggregation.
     * The first argument corresponds to the old aggregate,
     * whereas the second argument corresponds to the new
     * incoming value. <br>
     * Depending on these two arguments the new aggregate, i.e. 
     * average, has to be computed and returned.
	 * 
	 * @param average result of the aggregation function in the previous computation step
	 * @param next next object used for computation
	 * @return an Object array that contains the new aggregation value of type Double,
	 * and a counter of type Integer that reveals how often this function has
	 * been invoked.
	 */
    public Number[] invoke(Number[] agg, Number next) {
        double nextValue = next.doubleValue();
        if (agg == null) {
            return new Number[]{
                new Double(nextValue), 
                new Long(1)
            };
        }      
        long count = agg[1].longValue() + 1;
		return new Number[]{
	        new Double(((double) (count - 1) / (double) count) * agg[0].doubleValue() + (1.0 / count) * nextValue), 
	        new Long(count)
	    };
    }
    
    /**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
    public static void main(String[] args) {
    	
    		// Example 1 
//    		Enumerator e = new Enumerator(1, 11, 100);
//    		
//    		Mapper m = new Mapper(e, new Function() {
//    			public Object invoke(Object o) {
//    				return new TemporalObject(o, new TimeInterval(((Integer)o).intValue(),((Integer)o).intValue()+10));
//    			}}
//    		);
//    		
//    		TemporalAggregator agg = new TemporalAggregator(m, 
//                    new TemporalAggregatorSA(new CountAwareAverage())
//    		);
//    		
//    		Mapper m2 = new Mapper(agg, new Function() {
//    		    public Object invoke(Object o) {
//    		        TemporalObject to = (TemporalObject)o;
//    		        return new TemporalObject(
//    		          ((Object[])((TemporalObject)o).getObject())[0],
//    		          to.getTimeInterval()
//    		        );
//    		    }}
//    		);
//    		
//    		Printer printer = new Printer(m2);
//    		QueryExecutor exec = new QueryExecutor();
//    		exec.registerQuery(printer);
//    		exec.startQuery(printer);
    		
    		
    		// Example 2
    		
    		TemporalObject[] objects = new TemporalObject[] {
				new TemporalObject(new Double(24.3), new TimeInterval(1, 6)),
				new TemporalObject(new Double(40.27), new TimeInterval(2, 7)),
				new TemporalObject(new Double(47.18), new TimeInterval(3, 8)),
				new TemporalObject(new Double(51.76), new TimeInterval(4, 9)),
				new TemporalObject(new Double(46.95), new TimeInterval(5, 10))
			};
    		
    		TemporalAggregator agg = new TemporalAggregator(new CursorSource(new ArrayCursor<TemporalObject>(objects), 0), 
                    new TemporalAggregatorSA(new CountAwareAverage())
    		);
			
    		Mapper m = new Mapper(agg, new Function() {
    		    public Object invoke(Object o) {
    		        TemporalObject to = (TemporalObject)o;
    		        return new TemporalObject(
    		          ((Object[])((TemporalObject)o).getObject())[0],
    		          to.getTimeInterval()
    		        );
    		    }}
    		);
    		Printer printer = new Printer(m);
    		QueryExecutor exec = new QueryExecutor();
    		exec.registerQuery(printer);
    		exec.startQuery(printer);    	
    	}
    
}
