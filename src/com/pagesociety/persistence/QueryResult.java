package com.pagesociety.persistence;

import java.util.List;

/**
 * A structure for returning the results of a query.
 */
public abstract class QueryResult
{
	/**
	 * Returns a list of entities. This is the data result of the query.
	 * 
	 * @return A list of restored entities.
	 */
	public abstract List<Entity> getEntities();

	/**
	 * Returns a token that can be passed to the store to return the next page
	 * of query results.
	 * 
	 * @return A token
	 * @see PersistentStore#getNextResults(Object)
	 */
	public abstract Object getNextResultsToken();

	/**
	 * Returns the number of entities in this result (not the count of the
	 * complete result set).
	 * 
	 * @return The number of entities in this page.
	 */
	public abstract int size();

	/**
	 * Returns whether the query result is empty or not.
	 * 
	 * @return true if the query result is empty.
	 */
	public boolean isEmpty()
	{
		return (size() == 0);
	}

	/**
	 * Returns whether there are more results for the query that produced this
	 * result set.
	 * 
	 * @return true if the query has more results and a next token is available.
	 */
	public boolean hasMoreResults()
	{
		return (getNextResultsToken() != null);
	}
}
