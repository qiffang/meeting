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
package org.apache.openmeetings.test.webservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.apache.openmeetings.db.dto.basic.ServiceResult;
import org.apache.openmeetings.db.dto.basic.ServiceResult.Type;
import org.apache.openmeetings.db.dto.room.RoomOptionsDTO;
import org.apache.openmeetings.db.dto.user.ExternalUserDTO;
import org.junit.Test;

public class TestUserService extends AbstractWebServiceTest {
	@Test
	public void loginTest() {
		ServiceResult r = login();
		assertNotNull("Valid ServiceResult should be returned", r);
	}

	@Test
	public void hashTest() {
		ServiceResult r = login();
		ExternalUserDTO user = new ExternalUserDTO();
		user.setExternalId("1");
		user.setExternalType("OmJunitTests");
		user.setEmail("user1@junit.openmeetings.apache.org");
		user.setFirstname("First Name 1");
		user.setLastname("Last Name 1");
		user.setLogin("login1");
		RoomOptionsDTO options = new RoomOptionsDTO();
		options.setRoomId(5L);
		options.setModerator(true);
		Response resp = getClient(USER_SERVICE_URL)
				.path("/hash")
				.query("sid", r.getMessage())
				.form(new Form().param("user", user.toString()).param("options", options.toString()));
		assertNotNull("Valid ServiceResult should be returned", resp);
		assertEquals("Call should be successful", Response.Status.OK.getStatusCode(), resp.getStatus());
		ServiceResult r1 = resp.readEntity(ServiceResult.class);
		assertEquals("OM Call should be successful", r1.getType(), Type.SUCCESS.name());
	}
}
