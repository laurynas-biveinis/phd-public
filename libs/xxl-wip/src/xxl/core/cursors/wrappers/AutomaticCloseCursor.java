/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.cursors.wrappers;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.DecoratorCursor;

public class AutomaticCloseCursor extends DecoratorCursor {
	boolean closeWasCalled = false;
	public AutomaticCloseCursor(Cursor cursor) {
		super(cursor);
	}
	public void close() {
		// do not call close again if it is called below.
		if (closeWasCalled) {
			cursor.close();
			closeWasCalled = true;
		}
	}
	public boolean hasNext() {
		boolean hn = cursor.hasNext();
		if (!hn) {
			cursor.close();
			closeWasCalled = true;
		}
		return hn;
	}
}
