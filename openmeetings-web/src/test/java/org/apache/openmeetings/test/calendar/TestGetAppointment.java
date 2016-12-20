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
package org.apache.openmeetings.test.calendar;

import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.apache.openmeetings.db.dao.calendar.AppointmentDao;
import org.apache.openmeetings.db.entity.calendar.Appointment;
import org.apache.openmeetings.test.AbstractJUnitDefaults;
import org.junit.Test;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class TestGetAppointment extends AbstractJUnitDefaults {
	private static final Logger log = Red5LoggerFactory.getLogger(TestGetAppointment.class, webAppRootKey);

	@Autowired
	private AppointmentDao appointmentDao;
	
	@Test
	public void getAppoinment() {
		log.debug("getAppoinment enter");
		Long userId = 1L;
		
		Calendar now = Calendar.getInstance();
		Calendar a1End = Calendar.getInstance();
		a1End.setTime(now.getTime());
		a1End.add(Calendar.HOUR_OF_DAY, 1);
		Appointment a1 = getAppointment(now.getTime(), a1End.getTime());
		a1.setTitle("GetAppointment");
		
		a1 = appointmentDao.update(a1, userId);
		
		Appointment a = appointmentDao.get(a1.getId());
		assertNotNull("Failed to get Appointment By id", a);
		assertEquals("Inapropriate MeetingMembers count", 0, a.getMeetingMembers() == null ? 0 : a.getMeetingMembers().size());
	}
	
}
