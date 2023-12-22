/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
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
}
