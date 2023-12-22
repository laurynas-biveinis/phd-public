/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.jts;

import xxl.core.spatial.geometries.LineString2D;
import xxl.core.spatial.geometries.Point2D;

import com.vividsolutions.jts.geom.LineString;

/** Implementation of the {@link LineString2D} interface based on JTS {@link LineString}s.
 */
public class LineString2DAdapter extends Geometry2DAdapter implements LineString2D {

	/** Sole constructor: wraps the given {@link LineString} inside this object
	 * @param lineString the JTS-LineString to wrap
	 */	
	public LineString2DAdapter(LineString lineString){
		super(lineString);
 	}	

	/** @inheritDoc
	 * @see LineString#getPointN(int)
	 */
	public Point2D getPoint(int n) {
		return new Point2DAdapter (((LineString) geometry).getPointN(n));
	}

	/** @inheritDoc
	 * @see LineString#getStartPoint()
	 */
	public Point2D getStartPoint() {
		return new Point2DAdapter (((LineString) geometry).getStartPoint());
	}

	/** @inheritDoc
	 * @see LineString#getEndPoint()
	 */
	public Point2D getEndPoint() {
		return new Point2DAdapter (((LineString) geometry).getEndPoint());
	}

	/** @inheritDoc
	 * @see LineString#isClosed()
	 */
	public boolean isClosed() {
		return ((LineString) geometry).isClosed();
	}

	/** @inheritDoc
	 * @see LineString#isRing()
	 */
	public boolean isRing() {
		return ((LineString) geometry).isRing();
	}

	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public LineString getJTSGeometry(){
		return (LineString) super.getJTSGeometry();
	}

}
