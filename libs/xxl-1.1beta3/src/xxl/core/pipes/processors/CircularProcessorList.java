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
package xxl.core.pipes.processors;

import java.util.Hashtable;
import java.util.NoSuchElementException;


//no null-elements ! - not checked
/**
 * This class implements a circular list that can store processors.
 *
 * @see Processor
 * @since 1.1
 */
public class CircularProcessorList {
  
  //---------------------------------------------------------------------------
  private static class ProcessorListElement {
  	protected ProcessorListElement next;
  	protected ProcessorListElement prev;
  	protected Processor proc;
  	protected boolean blocked = false;
  	protected long blockedUntil = 0;
  	
  	public ProcessorListElement(Processor proc) {
  		this.proc = proc;
  		next = prev = this;
  	}
  	
  	public ProcessorListElement (Processor proc, ProcessorListElement prev, ProcessorListElement next) {
  		this.proc = proc;
  		this.next = next;
  		next.prev = this;
  		this.prev = prev;
  		prev.next = this;
  	}
  	
  }
  
  /**
   * Pointer to the current element
   */
  private ProcessorListElement cur;
  
  /**
   * Contains the list elements to ensure faster access.
   */
  private Hashtable<Processor,ProcessorListElement> procTable = new Hashtable<Processor,ProcessorListElement>();
  
  /** 
   * Adds a Processor to this list.<br>
   * It is inserted at the (current) end of the list.
   *
   * @param proc the Processor to be inserted
   */
  public synchronized void add(Processor proc) {
  	if (cur == null) {
  		cur = new ProcessorListElement(proc);
  		procTable.put(proc, cur);
  	}
  	else {
  		ProcessorListElement e = new ProcessorListElement(proc, cur.prev, cur);
  		procTable.put(proc, e);
  	}  	
  }
  
  /**
   * Inserts a Processor right after another one.
   *
   * @param proc the Processor to be inserted
   * @param predecessor the Processor after which the Processor should be inserted
   *
   * @throws NoSuchElementException if this list does not contain predecessor
   */
  public synchronized void addAfter(Processor proc, Processor predecessor) {
  	ProcessorListElement e = procTable.get(predecessor);
  	if (e == null) throw new NoSuchElementException();
  	ProcessorListElement newEl = new ProcessorListElement(proc, e, e.next);
  	procTable.put(proc, newEl);
  }
  
  /**
   * Tests whether this list contains a certain Processor or not.
   *
   * @param proc the Processor
   *
   * @return <code>true</code> if this list contains the Processor, <code>false</code> otherwise
   */
  public synchronized boolean contains(Processor proc) {
  	return procTable.get(proc) != null;
  }
  
  /**
   * Shifts the head of this list to the second element and returns it.
   * The former head element is thereby transfered to the end of the list.
   *
   * @return the new head element
   */
  public synchronized Processor getNext() {
  	cur = cur.next;
  	
  	return cur.prev.proc;
  }
  
  /**
   * Shifts the head of this list to the first element after the head element
   * that is not blocked and returns it.
   *
   * @return the new head element or <code>null</code> if all Processors are blocked
   */
  public synchronized Processor getNextUnblocked() {
  	if (cur == null) return null;
  	ProcessorListElement allBlockedIndicator = cur; //wenn wir den wieder erreichen und der blockiert ist, sind alle blockiert
  	do{ 
  		//System.out.println("getNextUnblocked : " + cur.proc.getName() + ", " + cur.blocked );
  		if (cur.blockedUntil != 0 && cur.blockedUntil < System.currentTimeMillis()) {
  			cur.blockedUntil = 0;
  			cur.blocked = false;
  		}
  		cur = cur.next;
  		if ( !cur.prev.blocked ){
  			return cur.prev.proc;
  		}  		
  	} while ( cur != allBlockedIndicator );
  	return null;
  }
  
  /**
   * Tests whether this list is empty or not.
   *
   * @return <code>true</code> if and only if this list is empty.
   */
  public boolean isEmpty() {
  	return cur == null;
  }
  
  /**
   * Removes a Processor from this list.
   *
   * @param proc the Processor to be removed
   *
   * @throws NoSuchElementException if this list does not contain the Processor
   */
  public synchronized void remove(Processor proc) {
    System.out.println("REMOVE: "+proc);
  	ProcessorListElement e = procTable.get(proc);
  	if (e ==  null) throw new NoSuchElementException();
  	if (e.prev == e) cur = null; //letztes Element
  	else {
  		e.prev.next = e.next;
  		e.next.prev = e.prev;
  	}
  }
  
  /**
   * Informs this list that a processor is blocked.
   *
   * @param proc the blocked Processor
   *
   * @throws NoSuchElementException if this list does not contain the Processor
   */
  public synchronized void setBlocked(Processor proc) {
  	ProcessorListElement e = procTable.get(proc);
  	if (e == null) throw new NoSuchElementException();
  	e.blocked = true;
  	e.blockedUntil = 0;
  }
  
  /**
   * Informs this list that a processor is blocked and will stay blocked 
   * until a certain point in time is reached.
   *
   * @param proc the blocked Processor
   * @param timestamp the time when the Processor will be unblocked again.
   *
   * @throws NoSuchElementException if this list does not contain the Processor
   */
  public synchronized void setBlockedUntil(Processor proc, long timestamp) {
  	ProcessorListElement e = procTable.get(proc);
  	if (e == null) throw new NoSuchElementException();
  	e.blocked = true;
  	e.blockedUntil = timestamp;
  }
  
  /**
   * Informs this list that a processor is not blocked anymore.
   *
   * @param proc the unblocked Processor
   *
   * @throws NoSuchElementException if this list does not contain the Processor
   */
  public synchronized void setUnblocked(Processor proc) {
  	ProcessorListElement e = procTable.get(proc);
  	if (e == null) throw new NoSuchElementException();
  	e.blocked = false;
  	e.blockedUntil = 0;
  }
  
  /**
   * @return the current number of processors
   */
  public synchronized int size() {
      return procTable.size();
  }
  	
}