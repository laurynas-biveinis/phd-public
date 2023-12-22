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

package xxl.applications.pipes.auctions;

/**
 * This class contain all necessary information from a bid generated
 * by the nexmark generator.
 */
public class Bid {

	private long itemID;
	private long bidderID;
	private float bid;
	private long timestamp;
	
	public Bid(){};
	
	public String toString() {
		return "(itemID: "+itemID+"; bidderID: "+bidderID+"; bid: "+bid+"; timestamp: "+ timestamp+")";
	}
	
	/**
	 * @return Returns the bid.
	 */
	public float getBid() {
		return bid;
	}

	/**
	 * @param bid The bid to set.
	 */
	public void setBid(float bid) {
		this.bid = bid;
	}

	/**
	 * @return Returns the bidderID.
	 */
	public long getBidderID() {
		return bidderID;
	}
	/**
	 * @param bidderID The bidderID to set.
	 */
	public void setBidderID(long bidderID) {
		this.bidderID = bidderID;
	}
	/**
	 * @return Returns the itemID.
	 */
	public long getItemID() {
		return itemID;
	}
	/**
	 * @param itemID The itemID to set.
	 */
	public void setItemID(long itemID) {
		this.itemID = itemID;
	}
	/**
	 * @return Returns the timestamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
