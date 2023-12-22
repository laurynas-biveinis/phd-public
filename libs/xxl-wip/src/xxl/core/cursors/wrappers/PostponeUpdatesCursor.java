/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.cursors.wrappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.predicates.Predicate;

public class PostponeUpdatesCursor extends AbstractCursor {

	Cursor cursor;
	Object lastObject;
	Predicate update;
	Predicate remove;
	List updateOperations;
	List removeOperations;

	public PostponeUpdatesCursor(Cursor cursor, Predicate update, Predicate remove) {
		this.cursor = cursor;
		this.update = update;
		this.remove = remove;
		
		lastObject = null;
		
		updateOperations = new ArrayList();
		removeOperations = new ArrayList();
	}
	protected void performUpdatesAndRemoves() {
		Iterator it = updateOperations.iterator();
		Object id, object;
		while (it.hasNext()) {
			id = it.next();
			object = it.next();
			update.invoke(id, object);
		}
		updateOperations = null;
		it = removeOperations.iterator();
		while (it.hasNext())
			remove.invoke(it.next());
		removeOperations = null;
	}
	public void close() {
		super.close();
		performUpdatesAndRemoves();
		cursor.close();
	}
	public void remove() throws IllegalStateException {
		super.remove();
		removeOperations.add(lastObject);
	}
	public void reset() throws UnsupportedOperationException {
		performUpdatesAndRemoves();
		super.reset();
	}
	public boolean supportsRemove() {
		return remove!=null;
	}
	public boolean supportsReset() {
		return cursor.supportsReset();
	}
	public boolean supportsUpdate() {
		return update!=null;
	}
	public void update(Object object) throws IllegalStateException, UnsupportedOperationException {
		super.update(object);
		updateOperations.add(lastObject);
		updateOperations.add(object);
	}
	protected boolean hasNextObject() {
		return cursor.hasNext();
	}
	protected Object nextObject() {
		lastObject = cursor.next();
		return lastObject;
	}
}
