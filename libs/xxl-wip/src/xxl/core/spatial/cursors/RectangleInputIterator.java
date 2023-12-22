/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.spatial.cursors;

import java.io.File;

import xxl.core.cursors.sources.io.FileInputCursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;

/**
 *	The RectangleInputIterator constructs an Iterator of rectangles
 *	for a given flat File of Rectangle-Objects.
 *  <br><br>
 *  The flat file is assumed to contain rectangles based on DoublePoints.
 *  <br><br>
 *  Implementation:
 *
 *	<code><pre>
 *	public RectangleInputIterator(File file, int bufferSize, final int dim){
 *		super(new AbstractFunction(){
 *			public Object invoke(){
 *				return new DoublePointRectangle(dim);
 *			}
 *		}, file, bufferSize);
 *	}
 *	</code></pre>
 *
 * <br><br>
 *	Use-case: (create iterator of rectangles, i.e. read rectangless from a flat file)
 *	<code><pre>
 *	public static void main(String[] args){
 *		
 *		Iterator it = new RectangleInputIterator(new File(args[0]), 1024*1024, Integer.parseInt(args[1]));
 *
 *		Cursors.println(it);	//print elements of Cursor to standard output
 *	}
 *	</code></pre>
 *
 *
 *  @see xxl.core.spatial.rectangles.Rectangles
 *  @see xxl.core.spatial.points.DoublePoint
 *
 */
public class RectangleInputIterator extends FileInputCursor<Rectangle> {

	/** Creates a new RectangleInputIterator.
     *
	 *	@param file the file containing the input-data
	 *	@param bufferSize the buffer-size to be allocated for reading the data
	 *	@param dim the dimensionality of the input-Rectangles
	 */
	public RectangleInputIterator(File file, int bufferSize, final int dim){
		super(
			new ConvertableConverter<Rectangle>(
				new AbstractFunction<Object, DoublePointRectangle>() {
					public DoublePointRectangle invoke(){
						return new DoublePointRectangle(dim);
					}
				}
			),
			file,
			bufferSize
		);
	}
}
