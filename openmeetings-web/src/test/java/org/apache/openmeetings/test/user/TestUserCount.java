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
package org.apache.openmeetings.test.user;

import static org.apache.openmeetings.web.app.WebSession.getUserId;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.test.AbstractWicketTester;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TestUserCount extends AbstractWicketTester {
	@Autowired
	private UserDao userDao;
	Random random = new Random();

	@Test
	public void testCountSearchUsers() throws Exception {
		User u = createUser(random.nextInt());
		assertTrue("Account of search users should be one", userDao.count(u.getFirstname()) == 1);
	}

	@Test
	public void testCountFilteredUsers() throws Exception {
		User u = createUser(random.nextInt());
		User contact = createUserContact(random.nextInt(), u.getId());
		assertTrue("Account of filtered user should be one", userDao.count(contact.getFirstname(), true, u.getId()) == 1);
	}

	@Test
	public void testCountUnfilteredUsers() throws Exception {
		User u = createUser(random.nextInt());
		createUserContact(random.nextInt(), u.getId());
		assertTrue("Account of unfiltered should be more then one", userDao.count("firstname", false, getUserId()) > 1);
	}
		
	@Test
	public void testCountAllUsers() throws Exception {
		assertTrue("Account of users should be positive", userDao.count() > 0);
	}
}
