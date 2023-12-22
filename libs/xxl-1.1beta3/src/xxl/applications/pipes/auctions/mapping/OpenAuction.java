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

package xxl.applications.pipes.auctions.mapping;

import java.util.ArrayList;

/**
 * This class is used to tranform xml to java objects with castor.  
 */
public class OpenAuction {

	private long id;
	private int reserve;
	private boolean reserveSet = false;
	private String privacy;
	private Itemref itemref = new Itemref();
	private Seller seller = new Seller();
	private int category;
	private int quantity;
	private String type;
	private Interval interval = new Interval();
	private ArrayList bidder = new ArrayList();

	public OpenAuction() {
	}
	
	public String toString() {
		return "auction-id: "+id+" reserve: "+(reserveSet ? reserve : -1)+" privacy: "+(privacy==null ? "no" : privacy)+" itemref: "+itemref
		+" seller: "+seller.toString()+" category: "+category+" quantity: "+quantity+" type: "
			+type+" interval: "+interval.toString()+" bidder: "+bidder.toString(); 
	}


	/**
	 * @return Bidder
	 */
	public ArrayList getBidder() {
		return bidder;
	}

	/**
	 * @return int
	 */
	public int getCategory() {
		return category;
	}

	/**
	 * @return long
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return Interval
	 */
	public Interval getInterval() {
		return interval;
	}

	/**
	 * @return Itemref
	 */
	public Itemref getItemref() {
		return itemref;
	}

	/**
	 * @return String
	 */
	public String getPrivacy() {
		return privacy;
	}

	/**
	 * @return int
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @return Seller
	 */
	public Seller getSeller() {
		return seller;
	}

	/**
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the bidder.
	 * @param bidder The bidder to set
	 */
	public void setBidder(ArrayList list) {
		bidder = list;
	}

	/**
	 * Sets the category.
	 * @param category The category to set
	 */
	public void setCategory(int category) {
		this.category = category;
	}

	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Sets the interval.
	 * @param interval The interval to set
	 */
	public void setInterval(Interval interval) {
		this.interval = interval;
	}

	/**
	 * Sets the itemref.
	 * @param itemref The itemref to set
	 */
	public void setItemref(Itemref itemref) {
		this.itemref = itemref;
	}

	/**
	 * Sets the privacy.
	 * @param privacy The privacy to set
	 */
	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}

	/**
	 * Sets the quantity.
	 * @param quantity The quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * Sets the seller.
	 * @param seller The seller to set
	 */
	public void setSeller(Seller seller) {
		this.seller = seller;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return int
	 */
	public int getReserve() {
		return reserve;
	}

	/**
	 * Sets the reserve.
	 * @param reserve The reserve to set
	 */
	public void setReserve(int reserve) {
		reserveSet = true;
		this.reserve = reserve;
	}

}
