/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.search.dsl;


/**
 * @author Yoann Rodiere
 */
public interface MultiFieldQueryClauseFieldSetContext<S> extends QueryClauseContext<S> {

	default S orField(String field) {
		return orFields( field );
	}

	S orFields(String ... field);

}
