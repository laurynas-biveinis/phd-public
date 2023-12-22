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

package xxl.core.pipes.scheduler;

import java.util.Collections;
import java.util.Comparator;

/**
 * Comparator-based strategy.
 * 
 * @since 1.1
 */
public class ComparatorStrategy extends AbstractStrategy {

	protected Comparator<Controllable> comp;
	protected Controllable next;
	
	public ComparatorStrategy(Controllable[] nodes, Comparator<Controllable> comp) {
		super(nodes);
		this.comp = comp;
	}
	
	public ComparatorStrategy(Controllable node, Comparator<Controllable> comp) {
		super(node);
		this.comp = comp;
	}
	
	public ComparatorStrategy(Comparator<Controllable> comp) {
		this.comp = comp;
	}

	@Override
	public boolean computeNext() {
		if (nodes.size() == 0)
			return false;
		Collections.sort(nodes, comp);	
		for (int i = 0; i < nodes.size(); i++) {
			next = nodes.get(i);	
			if (next.isFinished())
				deregister(next);
			else
				return true;
		}
		return false;
	}

	@Override
	public Controllable next() {
		return next;
	}

}
