package com.splitemapp.android.domain;

// Generated Sep 15, 2014 8:09:15 PM by Hibernate Tools 4.0.0

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * UserStatus generated by hbm2java
 */
@Entity
@Table(name = "user_status", catalog = "splitemapp", uniqueConstraints = @UniqueConstraint(columnNames = "cod"))
public class UserStatus implements java.io.Serializable {

	private static final long serialVersionUID = 4184361611600678462L;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	private short id;

	@Column(name = "cod", unique = true, nullable = false, length = 64)
	private String cod;

	@Column(name = "title", nullable = false, length = 64)
	private String title;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "userStatus")
	private Set<User> users = new HashSet<User>(0);

	public UserStatus() {
	}

	public UserStatus(short id, String cod, String title) {
		this.id = id;
		this.cod = cod;
		this.title = title;
	}

	public UserStatus(short id, String cod, String title, Set<User> users) {
		this.id = id;
		this.cod = cod;
		this.title = title;
		this.users = users;
	}

	public short getId() {
		return this.id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public String getCod() {
		return this.cod;
	}

	public void setCod(String cod) {
		this.cod = cod;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<User> getUsers() {
		return this.users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

}
