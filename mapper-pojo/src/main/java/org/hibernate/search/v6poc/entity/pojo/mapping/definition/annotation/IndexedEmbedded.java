/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.search.v6poc.backend.document.model.ObjectFieldStorage;

/**
 * @author Yoann Rodiere
 */
@Documented
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexedEmbedded {

	String prefix() default "";

	int maxDepth() default -1;

	String[] includePaths() default {};

	ObjectFieldStorage storage() default ObjectFieldStorage.DEFAULT;

	// TODO includeEmbeddedObjectId?
	// TODO targetElement?
	// TODO indexNullAs? => Maybe we should rather use "missing" queries?

}
