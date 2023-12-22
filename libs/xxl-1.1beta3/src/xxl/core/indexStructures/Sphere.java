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
package xxl.core.indexStructures;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xxl.core.io.Convertable;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.DoubleConverter;
import xxl.core.spatial.LpMetric;
import xxl.core.util.Distance;

/** 
 * This class implements a {@link xxl.core.indexStructures.Descriptor} that is a sphere. 
 * This class is used in the @link xxl.core.indexStructures.MTree MTree}.
 *
 */
public class Sphere implements Descriptor, Convertable {

	/** The center of the sphere.
	 */
	protected Object center;
	
	/** The radius if the sphere.
	 */
	protected double radius;
	
	/** A suitable converter for the center of the sphere.
	 */
	protected Converter centerConverter;
	
	/** The distance from the center of this sphere to the center of the
	 * sphere of the parent node in the MTree.
	 */
	protected double distanceToParent = -1;

	/** The metric distance function for points. 
	 */
	protected Distance pointDistance = LpMetric.EUCLIDEAN;
	
	/** The metric distance function for spheres.
	 */
	protected Distance sphereDistance = new Distance () {
		public double distance (Object o1, Object o2) {
			Sphere s1 = (Sphere)o1, s2 = (Sphere)o2;
			return Math.abs(s1.centerDistance(s2) - s1.radius() - s2.radius());
		}
	};
	
	/** Creates a new sphere.
	 * 
	 * @param center the new {@link Sphere#center}
	 * @param radius the new {@link Sphere#radius}
	 * @param centerConverter the new {@link Sphere#centerConverter}
	 * @param distanceToParent the new {@link Sphere#distanceToParent}
	 * @param pointDistance the new {@link #pointDistance}
	 * @param sphereDistance the new {@link #sphereDistance}
	 */
	public Sphere (Object center, double radius, Converter centerConverter, double distanceToParent, Distance pointDistance, Distance sphereDistance) {
		this.center = center;
		this.radius = radius;
		this.centerConverter = centerConverter;
		this.distanceToParent = distanceToParent;
		this.pointDistance = pointDistance;
		this.sphereDistance = sphereDistance;
	}
	
	/** Creates a new sphere.
	 * 
	 * @param center the new {@link Sphere#center}
	 * @param radius the new {@link Sphere#radius}
	 * @param centerConverter the new {@link Sphere#centerConverter}
	 * @param distanceToParent the new {@link Sphere#distanceToParent}
	 */
	public Sphere (Object center, double radius, Converter centerConverter, double distanceToParent) {
		this.center = center;
		this.radius = radius;
		this.centerConverter = centerConverter;
		this.distanceToParent = distanceToParent;
	}
	
	/** Creates a new sphere.
	 * 
	 * @param center the new {@link Sphere#center}
	 * @param radius the new {@link Sphere#radius}
	 * @param centerConverter the new {@link Sphere#centerConverter}
	 * @param distanceToParent the new {@link Sphere#distanceToParent}
	 * @param pointDistance the new {@link #pointDistance}
	 */
	public Sphere (Object center, double radius, Converter centerConverter, double distanceToParent, Distance pointDistance) {
		this(center, radius, centerConverter, distanceToParent);
		this.pointDistance = pointDistance;
	}

	/** Creates a new sphere by calling
	 * <pre><code>
 			this(center, radius, centerConverter, -1);
 		 </code></pre>
	 * 
	 * @param center the new {@link Sphere#center}
	 * @param radius the new {@link Sphere#radius}
	 * @param centerConverter the new {@link Sphere#centerConverter}
	 * @param pointDistance the new {@link #pointDistance}
	 * @param sphereDistance the new {@link #sphereDistance}
	 */
	public Sphere (Object center, double radius, Converter centerConverter, Distance pointDistance, Distance sphereDistance) {
		this(center, radius, centerConverter, -1, pointDistance, sphereDistance);
	}
	
	/** Creates a new sphere.
	 * 
	 * @param center the new {@link Sphere#center}
	 * @param radius the new {@link Sphere#radius}
	 * @param centerConverter the new {@link Sphere#centerConverter}
	 */
	public Sphere (Object center, double radius, Converter centerConverter) {
		this(center, radius, centerConverter, -1);
	}

	/* (non-Javadoc)
	 * @see xxl.core.io.Convertable#write(java.io.DataOutput)
	 */
	public void write (DataOutput dataOutput) throws IOException {
		centerConverter.write(dataOutput, center);
		DoubleConverter.DEFAULT_INSTANCE.writeDouble(dataOutput,radius);
		DoubleConverter.DEFAULT_INSTANCE.writeDouble(dataOutput, distanceToParent);
	}

	/* (non-Javadoc)
	 * @see xxl.core.io.Convertable#read(java.io.DataInput)
	 */
	public void read (DataInput dataInput) throws IOException {
		center = centerConverter.read(dataInput);
		radius = DoubleConverter.DEFAULT_INSTANCE.readDouble(dataInput);
		distanceToParent = DoubleConverter.DEFAULT_INSTANCE.readDouble(dataInput);
	}

	/** Computes the distance between the center of this and the center of the 
	 *  specified <tt>sphere</tt> using the {@link MTree#pointDistance}.
	 * 
	 * @param sphere the sphere to whichs center the distance should be determined
	 * @return the distance between the center of this and the center of <tt>sphere</tt>
	 */
	public double centerDistance (Sphere sphere) {
		return pointDistance.distance(center, sphere.center);
	}

	/** Computes the distance between this and the 
	 *  specified <tt>sphere</tt> using the {@link MTree#sphereDistance}.
	 * 
	 * @param sphere the sphere to which the distance should be determined
	 * @return the distance between this and <tt>sphere</tt>
	 */
	public double sphereDistance (Sphere sphere) {
		return overlapsPD(sphere) ? 0 : sphereDistance.distance(this, sphere);
	}

	/** Returns <tt>true</tt> if this sphere overlaps the specified sphere.
	 * 
	 * @param descriptor the sphere to check
	 * @return <tt>true</tt> if this sphere overlaps the specified sphere
	 */
	public boolean overlaps (Descriptor descriptor) {
		Sphere sphere = (Sphere)descriptor;
		return centerDistance(sphere) <= radius + sphere.radius;
	}

	/** Returns <tt>true</tt> if this sphere overlaps the specified sphere.
	 * This implementation uses an optimization: If the difference of
	 * distances to the center of the parent node of the spheres is greater
	 * than the sum of their radiuses, the spheres cannot overlap. This
	 * case can be detected using  
	 * {@link Sphere#distanceToParent}. 
	 * 
	 * @param descriptor the sphere to check
	 * @return <tt>true</tt> if this sphere overlaps the specified sphere
	 */
	public boolean overlapsPD (Descriptor descriptor) {
		Sphere sphere = (Sphere)descriptor;
		if (sphere.distanceToParent != -1 && distanceToParent != -1)
			if (Math.abs(sphere.distanceToParent - distanceToParent) > (sphere.radius + radius))
				return false;
		return overlaps(sphere);
	}

	/** Returns <tt>true</tt> if this sphere contains the specified sphere.
	 * 
	 * @param descriptor the sphere to check
	 * @return <tt>true</tt> if this sphere contains the specified sphere
	 */
	public boolean contains (Descriptor descriptor) {
		Sphere sphere = (Sphere)descriptor;
		return centerDistance(sphere) + sphere.radius <= radius;
	}

	/** Returns <tt>true</tt> if this sphere contains the specified sphere.
	 * This implementation uses an optimization: If the difference of
	 * distances to the center of the parent node of the spheres is greater
	 * than the sum of their radiuses, the spheres cannot overlap and i.e.
	 * not contain each other. This case can be detected using  
	 * {@link Sphere#distanceToParent}. 
	 * 
	 * @param descriptor the sphere to check
	 * @return <tt>true</tt> if this sphere contains the specified sphere
	 */
	public boolean containsPD (Descriptor descriptor) {
		Sphere sphere = (Sphere)descriptor;
		if (sphere.distanceToParent != -1 && distanceToParent != -1)
			if (Math.abs(sphere.distanceToParent - distanceToParent) > (sphere.radius + radius))
				return false;
		return contains(sphere);
	}

	/* (non-Javadoc)
	 * @see xxl.core.indexStructures.Descriptor#union(xxl.core.indexStructures.Descriptor)
	 */
	public void union (Descriptor descriptor) {
		Sphere sphere = (Sphere)descriptor;
		radius = Math.max(radius, centerDistance(sphere) + sphere.radius);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals (Object object) {
		Sphere sphere = (Sphere)object;
		return center.equals(sphere.center) && radius == sphere.radius;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return center.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone () {
		return new Sphere(center, radius, centerConverter, distanceToParent, pointDistance, sphereDistance);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString () {
		String s = "center: "+"("+center.toString()+")";
		s += "\tradius: "+radius;
		s += "\tdistance to parent: "+distanceToParent;
		return s;
	}

	/** Returns the center of this sphere.
	 * 
	 * @return the center of this sphere
	 */
	public Object center () {
		return center;
	}


	/** Returns the radius of this sphere.
	 * 
	 * @return the radius of this sphere
	 */
	public double radius () {
		return radius;
	}


	/** Returns the distance from the center of this node to the center of 
	 * the parent node. 
	 * 
	 * @return the distance to the center of the parent node
	 */
	public double getDistanceToParent() {
		return distanceToParent;
	}
	
	public void setDistanceToParent(double distanceToParent) {
	    this.distanceToParent = distanceToParent;
	}

} // end of class Sphere
