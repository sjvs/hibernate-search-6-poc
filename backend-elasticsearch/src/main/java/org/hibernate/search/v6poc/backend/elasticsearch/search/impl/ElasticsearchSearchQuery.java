/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.impl;

import java.util.Set;

import org.hibernate.search.v6poc.backend.elasticsearch.orchestration.impl.ElasticsearchWorkOrchestrator;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.ElasticsearchWork;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.ElasticsearchWorkFactory;
import org.hibernate.search.v6poc.search.SearchQuery;
import org.hibernate.search.v6poc.search.SearchResult;

import com.google.gson.JsonObject;


/**
 * @author Yoann Rodiere
 */
public class ElasticsearchSearchQuery<T> implements SearchQuery<T> {

	private final ElasticsearchWorkOrchestrator queryOrchestrator;
	private final ElasticsearchWorkFactory workFactory;
	private final Set<String> indexNames;
	private final JsonObject payload;
	private final HitExtractor<T> hitExtractor;

	private Long firstResultIndex;
	private Long maxResultsCount;

	public ElasticsearchSearchQuery(ElasticsearchWorkOrchestrator queryOrchestrator,
			ElasticsearchWorkFactory workFactory,
			Set<String> indexNames, JsonObject payload, HitExtractor<T> hitExtractor) {
		this.queryOrchestrator = queryOrchestrator;
		this.workFactory = workFactory;
		this.indexNames = indexNames;
		this.payload = payload;
		this.hitExtractor = hitExtractor;
	}

	@Override
	public void setFirstResult(Long firstResultIndex) {
		this.firstResultIndex = firstResultIndex;
	}

	@Override
	public void setMaxResults(Long maxResultsCount) {
		this.maxResultsCount = maxResultsCount;
	}

	@Override
	public SearchResult<T> execute() {
		ElasticsearchWork<SearchResult<T>> work = workFactory.search(
				indexNames, payload, hitExtractor,
				firstResultIndex, maxResultsCount );
		return queryOrchestrator.submit( work ).join();
	}

}
