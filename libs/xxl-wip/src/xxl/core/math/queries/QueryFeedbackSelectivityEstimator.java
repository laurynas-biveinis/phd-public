/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.queries;

import java.util.ArrayList;
import java.util.List;

/** 
 * This class provides a skeleton implementation for selectivity
 * estimators using query feedback as an improvement. The general idea
 * relies on [CR93]: Chen, Roussopolous, Adaptive Selectivity Estimation using
 * Query Feedback, 1993.
 */

public abstract class QueryFeedbackSelectivityEstimator {

	/** 
	 * Stores the number of estimates already done. 
	 */
	protected int numberOfEstimates;

	/** Stores the already processed queries with the corresponding true selectivity.
	 * The information about these queries is necessary to do a proper adjustment 
	 * of the estimator. 
	 */
	protected List<Object[]> alreadyProcessedQueries;

	/** Constructs a new Object of this type.
	 */
	public QueryFeedbackSelectivityEstimator() {
		alreadyProcessedQueries = new ArrayList<Object[]>();
		numberOfEstimates = 0;
	}

	/**
	 * Returns an estimation of the selectivity of a given query. Before the estimation
	 * is returned, the query and the true selectivity are stored in the list of already
	 * processed queries.
	 * 
	 * @param query query to estimate
	 * @param trueSelectivity true selectivity of the given query
	 * @return an estimation of the selectivity of the given query
	 */
	public double estimate(Object query, double trueSelectivity) {
		// the ordering of first estimate and second store the query is important
		// because otherwise the known selectivity of the given query will be
		// used for adjusting the estimator *before* the given query will be estimated.
		double r = getSelectivity(query);
		// first, estimate the selectivity of the given query
		addQuery(query, trueSelectivity);
		// second, store the last estimated query with its true selectivity
		numberOfEstimates++; // 
		return r; // return the estimated selectivity
	}

	/** Stores an already processed query and its true selectivity 
	 * to do an adjustment in order to improve following estimations.
	 * 
	 * @param query query to store
	 * @param sel true selectivity to store
	 */
	protected void addQuery(Object query, double sel) {
		alreadyProcessedQueries.add(new Object[] { query, new Double(sel)});
	}

	/** Computes an estimation of the selectivity of the given query.
	 * This method must be implemented by classes inherited from this class.
	 * 
	 * @param query query to process
	 * @return an estimation of the selectivity of the given query
	 */
	protected abstract double getSelectivity(Object query);
}
