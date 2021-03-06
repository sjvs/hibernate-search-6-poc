/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.impl;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaFieldNode;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonAccessor;

import com.google.gson.JsonElement;


/**
 * @author Yoann Rodiere
 * @author Guillaume Smet
 */
public class ElasticsearchIndexFieldAccessor<T> implements IndexFieldAccessor<T> {

	private final JsonAccessor<JsonElement> accessor;

	private final ElasticsearchIndexSchemaFieldNode schemaNode;

	public ElasticsearchIndexFieldAccessor(JsonAccessor<JsonElement> accessor, ElasticsearchIndexSchemaFieldNode schemaNode) {
		this.accessor = accessor;
		this.schemaNode = schemaNode;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[schemaNode=" + schemaNode + ", accessor=" + accessor + "]";
	}

	@Override
	public void write(DocumentElement target, T value) {
		ElasticsearchDocumentObjectBuilder builder = (ElasticsearchDocumentObjectBuilder) target;
		builder.checkTreeConsistency( schemaNode.getParent() );
		builder.add( accessor, schemaNode.getCodec().encode( value ) );
	}

}
