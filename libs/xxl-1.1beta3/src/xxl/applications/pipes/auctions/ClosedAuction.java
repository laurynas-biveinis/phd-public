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
 * This class contain all necessary information from a ClosedAuction
 * generated by the nexmark generator.
 */
public class ClosedAuction {	
	
	public final static int NO_BUYER = -1;
	public final static int NOT_SOLD = -1;
	
	private long itemID;
	private long buyerId;
	private long timestamp;

	private long auctionID;
	private int reserve;
	private String privacy;	
	private long seller ;
	private int category;
	private int quantity;
	private float sellPrice;
	private String type;

	public ClosedAuction(){}
	
	/**
	 * @param itemID
	 * @param buyerId
	 * @param timestamp
	 * @param reserve
	 * @param privacy
	 * @param seller
	 * @param category
	 * @param quantity
	 * @param type
	 */
	public ClosedAuction(long itemID, long buyerId, long timestamp,
			int reserve, String privacy, long seller,
			int category, int quantity, String type) {
		super();
		this.itemID = itemID;
		this.buyerId = buyerId;
		this.timestamp = timestamp;
		this.reserve = reserve;
		this.privacy = privacy;		
		this.seller = seller;
		this.category = category;
		this.quantity = quantity;
		this.type = type;
	}
	
	public String toString() {
		return "(auctionID: "+auctionID+"; buyerId: "+(buyerId >0 ? ""+buyerId : "no one" )+"; itemID: "+itemID+"; reserve: "
		+reserve+"; privacy: "+privacy+"; seller: "+seller+"; category: "+category+"; quantity: "+quantity+"; sellPrice: "
		+(sellPrice >0 ? ""+sellPrice : "not sold")+"; timestamp: "+timestamp+"; type: "+type+")";		
	}
	
	/**
	 * @return Returns the itemId.
	 */
	public long getItemID() {
		return itemID;
	}
	
	/**
	 * @return Returns the sellerId.
	 */
	public long getBuyerId() {
		return buyerId;
	}
	
	/**
	 * @param buyerId The buyerId to set.
	 */
	public void setBuyerId(long buyerId) {
		this.buyerId = buyerId;
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
	/**
	 * @return Returns the category.
	 */
	public int getCategory() {
		return category;
	}
	/**
	 * @param category The category to set.
	 */
	public void setCategory(int category) {
		this.category = category;
	}

	/**
	 * @return Returns the privacy.
	 */
	public String getPrivacy() {
		return privacy;
	}
	/**
	 * @param privacy The privacy to set.
	 */
	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}
	/**
	 * @return Returns the quantity.
	 */
	public int getQuantity() {
		return quantity;
	}
	/**
	 * @param quantity The quantity to set.
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	/**
	 * @return Returns the reserve.
	 */
	public int getReserve() {
		return reserve;
	}
	/**
	 * @param reserve The reserve to set.
	 */
	public void setReserve(int reserve) {
		this.reserve = reserve;
	}
	/**
	 * @return Returns the seller.
	 */
	public long getSeller() {
		return seller;
	}
	/**
	 * @param seller The seller to set.
	 */
	public void setSeller(long seller) {
		this.seller = seller;
	}
	/**
	 * @return Returns the startPrice.
	 */

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return Returns the auctionID.
	 */
	public long getAuctionID() {
		return auctionID;
	}
	/**
	 * @param auctionID The auctionID to set.
	 */
	public void setAuctionID(long auctionID) {
		this.auctionID = auctionID;
	}
	/**
	 * @param itemID The itemID to set.
	 */
	public void setItemID(long itemID) {
		this.itemID = itemID;
	}
	/**
	 * @return Returns the sellPrice.
	 */
	public float getSellPrice() {
		return sellPrice;
	}
	/**
	 * @param sellPrice The sellPrice to set.
	 */
	public void setSellPrice(float sellPrice) {
		this.sellPrice = sellPrice;
	}
}
