/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.impl;

import java.lang.invoke.MethodHandles;
import java.util.Locale;

import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

/**
 * Applies rules imposed by Elasticsearch to index names.
 *
 * @author Gunnar Morling
 */
public class ElasticsearchIndexNameNormalizer {

	private static final Log LOG = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private ElasticsearchIndexNameNormalizer() {
	}

	public static String normalize(String indexName) {
		String esIndexName = indexName.toLowerCase( Locale.ENGLISH );
		if ( !esIndexName.equals( indexName ) ) {
			LOG.debugf( "Normalizing index name from '%1$s' to '%2$s'", indexName, esIndexName );
		}
		return esIndexName;
	}
}
