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
public class Bidder {	
	private PersonRef person_ref;
	private float bid;
	private long time;	
	
	public Bidder(){}
	
	public String toString() {
		return "person-ref:"+person_ref.toString()+" bid: "+bid+" time: "+time ; 
	}
	
	/**
	 * @return float
	 */
	public float getBid() {
		return bid;
	}

	/**
	 * @return long
	 */
	public PersonRef getPerson_ref() {
		return person_ref;
	}

	/**
	 * @return long
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets the bid.
	 * @param bid The bid to set
	 */
	public void setBid(float bid) {
		this.bid = bid;
	}

	/**
	 * Sets the person.
	 * @param person The person to set
	 */
	public void setPerson_ref(PersonRef person) {
		person_ref = person;
	}

	/**
	 * Sets the time.
	 * @param time The time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
}
