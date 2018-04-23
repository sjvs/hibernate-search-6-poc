/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.index.impl;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexModel;
import org.hibernate.search.v6poc.backend.elasticsearch.impl.ElasticsearchBackendImpl;
import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.backend.elasticsearch.search.impl.ElasticsearchSearchTargetModel;
import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTarget;
import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTargetBuilder;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;


/**
 * @author Yoann Rodiere
 */
class ElasticsearchIndexSearchTargetBuilder implements IndexSearchTargetBuilder {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final ElasticsearchBackendImpl backend;

	// Use LinkedHashSet to ensure stable order when generating requests
	private final Set<ElasticsearchIndexManager> indexManagers = new LinkedHashSet<>();

	ElasticsearchIndexSearchTargetBuilder(ElasticsearchBackendImpl backend, ElasticsearchIndexManager indexManager) {
		this.backend = backend;
		this.indexManagers.add( indexManager );
	}

	void add(ElasticsearchBackendImpl backend, ElasticsearchIndexManager indexManager) {
		if ( ! this.backend.equals( backend ) ) {
			throw log.cannotMixElasticsearchSearchTargetWithOtherBackend( this, indexManager );
		}
		indexManagers.add( indexManager );
	}

	@Override
	public IndexSearchTarget build() {
		// Use LinkedHashSet to ensure stable order when generating requests
		Set<ElasticsearchIndexModel> indexModels = indexManagers.stream().map( ElasticsearchIndexManager::getModel )
				.collect( Collectors.toCollection( LinkedHashSet::new ) );
		ElasticsearchSearchTargetModel searchTargetModel = new ElasticsearchSearchTargetModel( indexModels );
		return new ElasticsearchIndexSearchTarget( backend.getSearchContext(), searchTargetModel );
	}

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "backend=" ).append( backend )
				.append( ", indexManagers=" ).append( indexManagers )
				.append( "]")
				.toString();
	}

}
