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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;


public class SignatureFactory {
	protected Map signatureMap;
	protected Map objectMap;
	protected int length;
	protected int lengthInBytes;
	protected int weight;
	protected Random random;
	protected int creationMisses;
	protected Signature nullSignature;

	public SignatureFactory(int length, int weight, Random random) {
		this.length = length;
		this.weight = weight;
		this.random = random;
		
		if (length<=0)
			throw new RuntimeException("length too small");
		if (weight>length)
			throw new RuntimeException("weight too large");
		
		lengthInBytes = (length+7)/8;
		signatureMap = new HashMap();
		objectMap = new HashMap();
		creationMisses = 0;
		nullSignature = new Signature(length);
	}

	public int getWeight() {
		return weight;
	}

	public int getLength() {
		return length;
	}

	public Signature getNullSignature() {
		return nullSignature;
	}

	public Signature getSignature(Object o) {
		return (Signature) objectMap.get(o);
	}

	public Signature getOrCreateSignature(Object o) {
		Signature newSignature = (Signature) objectMap.get(o);
		if (newSignature==null)
			return createSignature(o);
		else
			return newSignature;
	}

	public Signature createSignature(Object o) {
		Signature newSignature=null;
		while (true) {
			newSignature = Signature.createSignature(length, weight, random);
			
			if (signatureMap.get(newSignature)==null) {
				signatureMap.put(newSignature, o);
				objectMap.put(o, newSignature);
				break;
			}
			creationMisses++;
		}
		return newSignature;
	}

	public Iterator iterator() {
		return objectMap.entrySet().iterator();
	}

	public String toString() {
		return "SignatureFactory, creationMisses: "+creationMisses;
	}

	public static void main (String args[]) {
		SignatureFactory sf = new SignatureFactory(32,3, new Random(0));
		Signature s1 = sf.createSignature("1");
		Signature s2 = sf.createSignature("2");
		Signature s3 = sf.createSignature("3");
		Signature s4 = sf.createSignature("4");
		
		System.out.println(sf);
		
		Iterator it = sf.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			Signature sig = (Signature) me.getValue(); 
			System.out.println(
				"object: "+me.getKey()+
				", signature: "+sig+
				", signature hashcode: "+sig.hashCode());
		}
		
		Signature s1Back = sf.getSignature("1");
		System.out.println("Signature for '1': "+s1Back);
		if (s1Back != s1)
			throw new RuntimeException("Not the same Signature returned");
		
		Signature s12 = s1.overlayWith(s2);
		
		System.out.println("Signature for 1 and 2: "+s12);
		System.out.println("is s1 in s12? "+s1.isInSignature(s12));
		System.out.println("is s2 in s12? "+s2.isInSignature(s12));
		System.out.println("is s3 in s12? "+s3.isInSignature(s12));
		System.out.println("is s4 in s12? "+s4.isInSignature(s12));
	}
}
