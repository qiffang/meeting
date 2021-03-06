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
package org.apache.openmeetings.db.dto.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.openmeetings.db.entity.server.Server;

/**
 * 
 * Bean send to the client about the server he is going to use for the conference 
 * session
 * 
 * @author sebawagner
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String name;
	private String address;
	private int port;
	private String user;
	private String password;
	private String webapp;
	private String protocol;
	private boolean active;
	private String comment;

	public ServerDTO() {}
	
	public ServerDTO(Server s) {
		if (s == null) {
			return;
		}
		id = s.getId();
		name = s.getName();
		address = s.getAddress();
		port = s.getPort();
		user = s.getUser();
		password = s.getPass();
		webapp = s.getWebapp();
		protocol = s.getProtocol();
		active = s.isActive();
		comment = s.getComment();
	}

	public Server get() {
		Server s = new Server();
		s.setId(id);
		s.setName(name);
		s.setAddress(address);
		s.setPort(port);
		s.setUser(user);
		s.setPass(password);
		s.setWebapp(webapp);
		s.setProtocol(protocol);
		s.setActive(active);
		s.setComment(comment);
		return s;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getWebapp() {
		return webapp;
	}

	public void setWebapp(String webapp) {
		this.webapp = webapp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Override
	public String toString() {
		return "id "+id+" address "+address+" port "+port+" protocol "+protocol;
	}

	public static List<ServerDTO> list(List<Server> l) {
		List<ServerDTO> list = new ArrayList<>();
		if (l != null) {
			for (Server s : l) {
				list.add(new ServerDTO(s));
			}
		}
		return list;
	}
}
