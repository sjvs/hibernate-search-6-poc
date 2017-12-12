/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.dsl.impl;

import java.util.Set;

import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexModel;
import org.hibernate.search.v6poc.backend.elasticsearch.orchestration.impl.ElasticsearchWorkOrchestrator;
import org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl.ElasticsearchSearchPredicateFactory;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.ElasticsearchWorkFactory;

/**
 * @author Yoann Rodiere
 */
public interface SearchTargetContext {

	Set<String> getIndexNames();

	Set<ElasticsearchIndexModel> getIndexModels();

	ElasticsearchWorkFactory getWorkFactory();

	ElasticsearchWorkOrchestrator getQueryOrchestrator();

	ElasticsearchSearchPredicateFactory getSearchPredicateFactory();

}