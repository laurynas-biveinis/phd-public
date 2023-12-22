/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.tests.spatial.points;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import xxl.core.io.converters.DoubleConverter;
import xxl.core.spatial.points.NumberPoint;

public class TestNumberPoint {

	public static void main(String[] args) throws IOException {
		NumberPoint<Double> p1 = new NumberPoint<Double>(new Double[] { 1.0d, 1.0d } );
		System.out.println(p1);
		
		// test write
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		p1.write(out);
		p1.write(out);
		
		// test read a)
		NumberPoint<Double> p2 = new NumberPoint<Double>(new Double[] { 0.0, 0.0 } );
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		p2.read(in);
		System.out.println(p2);
		System.out.println(p1.equals(p2));

		// test read b)
		NumberPoint<Double> p3 = new NumberPoint<Double>(2, DoubleConverter.DEFAULT_INSTANCE );
		p3.read(in);
		System.out.println(p3);
		System.out.println(p1.equals(p3));
		
		// test clone()
		NumberPoint<Double> p4 = (NumberPoint<Double>)p1.clone();
		System.out.println(p4);
		System.out.println(p1.equals(p4));
	}

}
