/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.db.entity.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.openmeetings.db.entity.IDataProviderEntity;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Entity
@NamedQueries({
	@NamedQuery(name="getGroupById", query="SELECT c FROM Group AS c WHERE c.id = :id AND c.deleted = false")
	, @NamedQuery(name="getGroupByName", query="SELECT o FROM Group AS o WHERE o.name = :name AND o.deleted = false")
	, @NamedQuery(name="getAnyGroupById", query="SELECT c FROM Group AS c WHERE c.id = :groupId")
	, @NamedQuery(name="getGroupsByIds", query="SELECT c FROM Group AS c WHERE c.id IN :ids")
	, @NamedQuery(name="getGroupsByUserId", query="SELECT ou.group FROM User u, IN(u.groupUsers) ou WHERE u.deleted = false AND u.id = :userId")
	, @NamedQuery(name="getNondeletedGroups", query="SELECT o FROM Group o WHERE o.deleted = false ORDER BY o.id")
	, @NamedQuery(name="countGroups", query="SELECT COUNT(c) FROM Group AS c WHERE c.deleted = false")
})
@Table(name = "om_group")
@Root(name="organisation")
public class Group implements IDataProviderEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Element(data = true, name = "organisation_id")
	private Long id;
	
	@Column(name="name")
	@Element(data = true, required = false)
	private String name;
	
	@Column(name="insertedby")
	private Long insertedby;
	
	@Column(name="updatedby")
	private Long updatedby;
	
	@Column(name="inserted")
	private Date inserted;
	
	@Column(name="updated")
	private Date updated;
	
	@Column(name="deleted")
	@Element(data = true, required = false)
	private boolean deleted;
	

	public Long getInsertedby() {
		return insertedby;
	}
	public void setInsertedby(Long insertedby) {
		this.insertedby = insertedby;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getUpdatedby() {
		return updatedby;
	}
	public void setUpdatedby(Long updatedby) {
		this.updatedby = updatedby;
	}	
	

	public Date getInserted() {
		return inserted;
	}
	public void setInserted(Date inserted) {
		this.inserted = inserted;
	}

	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	@Override
	public String toString() {
		return "Group [id=" + id + ", name=" + name + ", deleted=" + deleted + "]";
	}	
}
