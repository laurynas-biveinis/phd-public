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
package xxl.core.util;

/**
 * Objects implementing this interface can be classified. <br>
 * That means each object is assigned to exactly one cluster.
 * The ID of this cluster can be enquired and changed.
 * Furthermore each object has to provide an internal status
 * that shows if the object has already been classified. <p>
 *
 * However, clustering algrithms make extremely use of objects
 * implementing this interface.
 *
 * @see xxl.core.cursors.groupers.DBScan
 *
 */
public interface Classifiable {

	/**
	 * Returns <tt>true</tt>, if the object has already been classified.
	 *
	 * @return <tt>true</tt> if the object has already been classified, <tt>false</tt> otherwise.
	 */
	public abstract boolean isClassified ();

	/**
	 * Returns the cluster ID of this object.
	 *
	 * @return the cluster ID of this object.
	 */
	public abstract long getClusterID ();

	/**
	 *	Sets the cluster ID of this object.
	 *
	 * @param CLUSTER_ID the new cluster ID of this object.
	 */
	public abstract void setClusterID (long CLUSTER_ID);

}
