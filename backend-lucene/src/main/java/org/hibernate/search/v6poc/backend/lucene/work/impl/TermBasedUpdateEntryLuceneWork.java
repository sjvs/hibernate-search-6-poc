/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.work.impl;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.hibernate.search.v6poc.backend.lucene.document.impl.LuceneIndexEntry;
import org.hibernate.search.v6poc.backend.lucene.util.impl.LuceneFields;

/**
 * @author Guillaume Smet
 */
public class TermBasedUpdateEntryLuceneWork extends AbstractUpdateEntryLuceneWork {

	public TermBasedUpdateEntryLuceneWork(String indexName, String tenantId, String id, LuceneIndexEntry indexEntry) {
		super( indexName, tenantId, id, indexEntry );
	}

	@Override
	protected long doUpdateEntry(IndexWriter indexWriter, String tenantId, String id, LuceneIndexEntry indexEntry) throws IOException {
		return indexWriter.updateDocuments( new Term( LuceneFields.idFieldName(), id ), indexEntry );
	}
}
