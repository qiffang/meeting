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
package org.apache.openmeetings.db.dto.calendar;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.dto.user.UserDTO;
import org.apache.openmeetings.db.entity.calendar.MeetingMember;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MeetingMemberDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private UserDTO user;
	
	public MeetingMemberDTO() {}

	public MeetingMemberDTO(MeetingMember mm) {
		this.id = mm.getId();
		this.user = new UserDTO(mm.getUser());
	}

	public MeetingMember get(UserDao userDao) {
		MeetingMember mm = new MeetingMember();
		mm.setId(id);
		mm.setUser(user.get(userDao));
		return mm;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}
}
