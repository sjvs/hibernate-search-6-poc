/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.showcase.library.model;

import javax.persistence.Entity;

import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Indexed;

/**
 * A video document.
 *
 * @see Document
 */
@Entity
@Indexed(index = Video.INDEX)
public class Video extends Document<VideoCopy> {

	static final String INDEX = "Video";

}
