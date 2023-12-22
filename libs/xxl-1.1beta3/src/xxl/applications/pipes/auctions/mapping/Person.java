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
public class Person {
	
	private long id;
	private String name;
	private String emailaddress;
	private String phone;
	private Address address = new Address();
	private Profile profile = new Profile();
	private String creditcard;
	private String homepage;
		
	public Person(){}
	
	public String toString() {
		return "(Person with id: "+id
			+", name: "+(name == null ? "unknown": name)
			+", email: "+(emailaddress == null ? "unknown": emailaddress)
			+", phone: "+(phone == null ? "unknown": phone)
			+", address: "+address.toString()
			+", profile: "+profile.toString()
			+", creditcard: "+(creditcard == null ? "unknown": creditcard)
			+", homepage: "+(homepage == null ? "unknown": homepage+")");
	}

	/**
	 * @return
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * @return
	 */
	public String getCreditcard() {
		return creditcard;
	}

	/**
	 * @return
	 */
	public String getEmailaddress() {
		return emailaddress;
	}

	/**
	 * @return
	 */
	public String getHomepage() {
		return homepage;
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
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @return
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * @param address
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	/**
	 * @param string
	 */
	public void setCreditcard(String string) {
		creditcard = string;
	}

	/**
	 * @param string
	 */
	public void setEmailaddress(String string) {
		emailaddress = string;
	}

	/**
	 * @param string
	 */
	public void setHomepage(String string) {
		homepage = string;
	}

	/**
	 * @param l
	 */
	public void setId(long l) {
		id = l;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setPhone(String string) {
		phone = string;
	}

	/**
	 * @param profile
	 */
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

}