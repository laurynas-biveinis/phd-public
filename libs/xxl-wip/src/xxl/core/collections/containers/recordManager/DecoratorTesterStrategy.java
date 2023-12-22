/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.collections.containers.recordManager;

import xxl.core.collections.containers.recordManager.RecordManager.PageInformation;

/**
 * This class makes some consistency checks before forwarding the
 * calls to another strategy. It tests strategy and record manager.
 */
public class DecoratorTesterStrategy extends AbstractStrategy {

	/**
	 * The strategy which is decorated and tested.
	 */
	protected AbstractStrategy as;

	/**
	 * Creates a new tester for an AbstractStrategy.
	 * @param as Strategy to be decorated.
	 */
	public DecoratorTesterStrategy(AbstractStrategy as) {
		this.as = as;
	}

	/**
	 * Makes some checks.
	 * @see xxl.core.collections.containers.recordManager.Strategy#getPageForRecord(int)
	 */
	public Object getPageForRecord(int bytesRequired) {
		Object pageId = as.getPageForRecord(bytesRequired);
		if (pageId!=null) {
			PageInformation pi = (PageInformation) pages.get(pageId);
			if (pi.bytesFreeAfterPossibleReservation(bytesRequired)<0)
				throw new RuntimeException("Strategy proposed page which cannot hold the data.");
		}
		return pageId;
	}

	/**
	 * Makes some checks.
	 * @see xxl.core.collections.containers.recordManager.Strategy#pageInserted(java.lang.Object, xxl.core.collections.containers.recordManager.RecordManager.PageInformation)
	 */
	public void pageInserted(Object pageId, PageInformation pi) {
		if (pages.get(pageId)!=pi)
			throw new RuntimeException("PageInformation object is not correct!");
		as.pageInserted(pageId, pi);
	}

	/**
	 * Makes some checks.
	 * @see xxl.core.collections.containers.recordManager.Strategy#pageRemoved(java.lang.Object, xxl.core.collections.containers.recordManager.RecordManager.PageInformation)
	 */
	public void pageRemoved(Object pageId, PageInformation pi) {
		if (pages.get(pageId)!=pi)
			throw new RuntimeException("PageInformation object is not correct!");
		as.pageRemoved(pageId, pi);
	}

	/**
	 * Makes some checks.
	 * @see xxl.core.collections.containers.recordManager.Strategy#recordUpdated(java.lang.Object, xxl.core.collections.containers.recordManager.RecordManager.PageInformation, short, int, int, int)
	 */
	public void recordUpdated(Object pageId, PageInformation pi, short recordNumber, 
			int recordsAdded, int bytesAdded, int linkRecordsAdded) {
		if (pages.get(pageId)!=pi)
			throw new RuntimeException("PageInformation object is not correct!");
		if (pi.maxRecordNumber>recordNumber || pi.minRecordNumber<recordNumber)
			throw new RuntimeException("recordNumber is wrong from the RecordManager");
		if (pi.numberOfBytesUsedByRecords<bytesAdded)
			throw new RuntimeException("bytesAdded is wrong from the RecordManager");
		if (pi.numberOfRecords<1)
			throw new RuntimeException("numberOfRecords is wrong from the RecordManager");
		as.recordUpdated(pageId, pi, recordNumber, recordsAdded, bytesAdded, linkRecordsAdded);
	}
}
