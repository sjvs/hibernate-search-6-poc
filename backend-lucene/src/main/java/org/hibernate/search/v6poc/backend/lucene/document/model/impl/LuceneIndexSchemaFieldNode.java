/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.document.model.impl;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import org.hibernate.search.v6poc.backend.lucene.logging.impl.Log;
import org.hibernate.search.v6poc.backend.lucene.types.codec.impl.LuceneFieldCodec;
import org.hibernate.search.v6poc.backend.lucene.types.formatter.impl.LuceneFieldFormatter;
import org.hibernate.search.v6poc.backend.lucene.types.predicate.impl.LuceneFieldPredicateBuilderFactory;
import org.hibernate.search.v6poc.backend.lucene.types.sort.impl.LuceneFieldSortContributor;
import org.hibernate.search.v6poc.util.EventContext;
import org.hibernate.search.v6poc.logging.spi.EventContexts;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

/**
 * @author Guillaume Smet
 */
public class LuceneIndexSchemaFieldNode<T> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final String relativeFieldName;

	private final LuceneIndexSchemaObjectNode parent;

	private final String absoluteFieldPath;

	private final LuceneFieldFormatter<?> formatter;

	private final LuceneFieldCodec<T> codec;

	private final LuceneFieldPredicateBuilderFactory predicateBuilderFactory;

	private final LuceneFieldSortContributor sortContributor;

	public LuceneIndexSchemaFieldNode(LuceneIndexSchemaObjectNode parent, String relativeFieldName, LuceneFieldFormatter<?> formatter, LuceneFieldCodec<T> codec,
			LuceneFieldPredicateBuilderFactory predicateBuilderFactory, LuceneFieldSortContributor sortContributor) {
		this.parent = parent;
		this.relativeFieldName = relativeFieldName;
		this.absoluteFieldPath = parent.getAbsolutePath( relativeFieldName );
		this.formatter = formatter;
		this.codec = codec;
		this.predicateBuilderFactory = predicateBuilderFactory;
		this.sortContributor = sortContributor;
	}

	public LuceneIndexSchemaObjectNode getParent() {
		return parent;
	}

	public String getFieldName() {
		return relativeFieldName;
	}

	public String getAbsoluteFieldPath() {
		return absoluteFieldPath;
	}

	public LuceneFieldFormatter<?> getFormatter() {
		return formatter;
	}

	public LuceneFieldCodec<T> getCodec() {
		return codec;
	}

	public LuceneFieldPredicateBuilderFactory getPredicateBuilderFactory() {
		if ( predicateBuilderFactory == null ) {
			throw log.unsupportedDSLPredicates( getEventContext() );
		}
		return predicateBuilderFactory;
	}

	public LuceneFieldSortContributor getSortContributor() {
		if ( sortContributor == null ) {
			throw log.unsupportedDSLSorts( getEventContext() );
		}
		return sortContributor;
	}

	public boolean isCompatibleWith(LuceneIndexSchemaFieldNode<?> other) {
		return Objects.equals( formatter, other.formatter )
				&& Objects.equals( codec, other.codec )
				&& Objects.equals( predicateBuilderFactory, other.predicateBuilderFactory )
				&& Objects.equals( sortContributor, other.sortContributor );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() ).append( "[" )
				.append( "parent=" ).append( parent )
				.append( ", relativeFieldName=" ).append( relativeFieldName )
				.append( ", formatter=" ).append( formatter )
				.append( ", codec=" ).append( codec )
				.append( ", predicateBuilderFactory=" ).append( predicateBuilderFactory )
				.append( ", sortContributor=" ).append( sortContributor )
				.append( "]" );
		return sb.toString();
	}

	private EventContext getEventContext() {
		return EventContexts.fromIndexFieldAbsolutePath( absoluteFieldPath );
	}
}
