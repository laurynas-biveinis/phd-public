package xxl.tests.cursors.wrappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.DecoratorCursor;
import xxl.core.cursors.wrappers.AutomaticCloseCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AutomaticCloseCursor.
 */
public class TestAutomaticCloseCursor {
	public static void main(String[] args) {
		List l = new ArrayList();
		l.add("1");
		l.add("2");
		l.add("3");
		l.add("4");
		l.add("5");
		
		Iterator it = l.iterator();
		Cursor c = new DecoratorCursor(it) { //new IteratorCursor(it));
			public void close() {
				System.out.println("Close called");
			}
		};
		
		Cursors.println(new AutomaticCloseCursor(c));
	}

}
