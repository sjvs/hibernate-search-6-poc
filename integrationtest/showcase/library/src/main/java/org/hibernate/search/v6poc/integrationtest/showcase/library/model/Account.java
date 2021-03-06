/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.showcase.library.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import org.hibernate.search.v6poc.integrationtest.showcase.library.bridge.annotation.AccountBorrowalSummaryBridge;

/**
 * A user account.
 */
@Entity
@AccountBorrowalSummaryBridge
public class Account extends AbstractEntity<Integer> {

	@Id
	@GeneratedValue
	private Integer id;

	@OneToOne(optional = false)
	private Person user;

	@OneToMany(mappedBy = "account")
	@OrderBy("id")
	private List<Borrowal> borrowals = new ArrayList<>();

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Person getUser() {
		return user;
	}

	public void setUser(Person user) {
		this.user = user;
	}

	public List<Borrowal> getBorrowals() {
		return borrowals;
	}

	public void setBorrowals(List<Borrowal> borrowals) {
		this.borrowals = borrowals;
	}

	@Override
	protected String getDescriptionForToString() {
		return "user=" + getUser();
	}
}
