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
public class Address {
	
	private String street;
	private String city ;
	private String country;
	private String province;
	private int zipcode;
	
	public Address() {}
	
	public String toString() {
		if (street==null && city == null && country ==null 
			&& province == null && zipcode ==0)
				return " unknown "; 
		return " [street: "+(street==null ? "unknown" : street)
			+" city: "+(street == null ? "unknown" : street) 
			+" country: "+(country == null ? "unknown" : country)
			+" province: "+(province == null ? "unknown" : province)
			+" zipcode: "+(zipcode == 0 ? "unknown" : zipcode+"")+"]";
	}

	/**
	 * @return
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return
	 */
	public String getProvince() {
		return province;
	}

	/**
	 * @return
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * @return
	 */
	public int getZipcode() {
		return zipcode;
	}

	/**
	 * @param string
	 */
	public void setCity(String string) {
		city = string;
	}

	/**
	 * @param string
	 */
	public void setCountry(String string) {
		country = string;
	}

	/**
	 * @param string
	 */
	public void setProvince(String string) {
		province = string;
	}

	/**
	 * @param string
	 */
	public void setStreet(String string) {
		street = string;
	}

	/**
	 * @param i
	 */
	public void setZipcode(int i) {
		zipcode = i;
	}
}
