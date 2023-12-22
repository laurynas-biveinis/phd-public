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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.core.collections.containers.recordManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.SortedMap;

import xxl.core.collections.containers.recordManager.RecordManager.PageInformation;

public class HybridBFOEStrategy extends BestFitOnNEmptiestPagesStrategy {
	protected Strategy strategy;	
	
	public HybridBFOEStrategy(int n, Strategy secondStrategy) {
		super(n);
		this.strategy = secondStrategy;
	}
	public Strategy getSecondStrategy(){
	    return strategy;
	}
	public Object getPageForRecord(int bytesRequired) {
		Object pageId = super.getPageForRecord(bytesRequired);
		if (pageId==null)
			return strategy.getPageForRecord(bytesRequired);
		else
			return pageId;
	}
	public void init(SortedMap pages, int pageSize, int maxObjectSize) {
		super.init(pages, pageSize, maxObjectSize);
		strategy.init(pages, pageSize, maxObjectSize);
	}
	public void pageRemoved(Object pageId, PageInformation pi) {
		super.pageRemoved(pageId, pi);
		strategy.pageRemoved(pageId, pi);
	}
	public void recordUpdated(Object pageId, PageInformation pi, short recordNumber, int recordsAdded, int bytesAdded, int linkRecordsAdded) {
		super.recordUpdated(pageId, pi, recordNumber, recordsAdded, bytesAdded, linkRecordsAdded);
		strategy.recordUpdated(pageId, pi, recordNumber, recordsAdded, bytesAdded, linkRecordsAdded);
	}
	public void close() {
		super.close();
		strategy.close();
	}
	public void pageInserted(Object pageId, PageInformation pi) {
		super.pageInserted(pageId, pi);
		strategy.pageInserted(pageId, pi);
	}
	public void read(DataInput dataInput) throws IOException {
		super.read(dataInput);
		strategy.read(dataInput);
	}
	public void write(DataOutput dataOutput) throws IOException {
		super.write(dataOutput);
		strategy.write(dataOutput);
	}
}
