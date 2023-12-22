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

/**
 * This class is used to tranform xml to java objects with castor.  
 */
public class Auction {
	private long id;
	private int reserve;
	private String privacy;	
	private Itemref itemref;
	private Seller person;	
	private int category;
	private int quantity;
	private String type;
	private Interval interval;
	
	public Auction(){}
	
	public String toString(){
		return "id "+id+" reserve "+reserve+" privacy "+privacy+" item "+itemref.toString()+" person "+
			person.toString()+" category "+category+" quantity "+quantity+" type "+type+" "+interval.toString();
	}
	
	/**
	 * @return
	 */
	public int getCategory() {
		return category;
	}

	/**
	 * @return
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return
	 */
	public Interval getInterval() {
		return interval;
	}

	/**
	 * @return
	 */
	public String getPrivacy() {
		return privacy;
	}

	/**
	 * @return
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @return
	 */
	public int getReserve() {
		return reserve;
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param i
	 */
	public void setCategory(int i) {
		category = i;
	}

	/**
	 * @param l
	 */
	public void setId(long l) {
		id = l;
	}

	/**
	 * @param interval
	 */
	public void setInterval(Interval interval) {
		this.interval = interval;
	}

	/**
	 * @param string
	 */
	public void setPrivacy(String string) {
		privacy = string;
	}

	/**
	 * @param i
	 */
	public void setQuantity(int i) {
		quantity = i;
	}

	/**
	 * @param i
	 */
	public void setReserve(int i) {
		reserve = i;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}
	
	/**
	 * @return
	 */
	public Seller getPerson() {
		return person;
	}

	/**
	 * @param seller
	 */
	public void setPerson(Seller seller) {
		person = seller;
	}

	/**
	 * @return
	 */
	public Itemref getItemref() {
		return itemref;
	}

	/**
	 * @param itemref
	 */
	public void setItemref(Itemref itemref) {
		this.itemref = itemref;
	}
}
