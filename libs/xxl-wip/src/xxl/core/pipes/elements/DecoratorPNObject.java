/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.pipes.elements;

public class DecoratorPNObject<T,D> extends PNObject<T> {
	
	protected D decoration;
	
	public DecoratorPNObject(PNObject<T> pno, D decoration) {
		super(pno);
		this.decoration = decoration;
	}
	
	public D getDecoration() {
		return decoration;
	}
}
