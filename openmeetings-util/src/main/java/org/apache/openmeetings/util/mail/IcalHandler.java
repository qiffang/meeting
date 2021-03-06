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
package org.apache.openmeetings.util.mail;

import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;

import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

/**
 * 
 * @author o.becherer, seba.wagner
 * 
 */
public class IcalHandler {
	private static final Logger log = Red5LoggerFactory.getLogger(IcalHandler.class, webAppRootKey);
	static {
		System.setProperty("net.fortuna.ical4j.timezone.update.enabled", "false");
	}

	/** ICal instance */
	private final Calendar icsCalendar;

	/** Creation of a new Event */
	public final static Method ICAL_METHOD_REQUEST = Method.REQUEST;
	public final static Method ICAL_METHOD_CANCEL = Method.CANCEL;
	public final static Method ICAL_METHOD_REFRESH = Method.REFRESH;

	/**
	 * Constructor
	 * 
	 * @param method
	 *            (@see IcalHandler Constants)
	 * @throws Exception
	 */
	public IcalHandler(Method method) {
		log.debug("Icalhandler method type : " + method);
		
		icsCalendar = new Calendar();
		icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
		icsCalendar.getProperties().add(Version.VERSION_2_0);
		icsCalendar.getProperties().add(CalScale.GREGORIAN);
		icsCalendar.getProperties().add(method);
	}

	/**
	 * 
	 * @param startDate
	 *            use standard TimeZone!!
	 * @param endDate
	 *            use standard time zone!!
	 * @param name
	 *            meeting name
	 * @param attendees
	 *            List of attendees (use getAttendeeData to retrieve valid records)
	 * @param description
	 *            containing the meeting description
	 * @param organizer
	 *            organizer
	 * @param uid
	 *            (maybe null)
	 * @param javaTzId ID of owner's java time zone
	 * @return UID of Meeting
	 */
	// ---------------------------------------------------------------------------------------
	public String addNewMeeting(Date startDate, Date endDate, String name,
			Vector<Map<String, String>> attendees, String description,
			Map<String, String> organizer, String uid, String javaTzId) throws Exception
	{

		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		
		TimeZone timeZone = registry.getTimeZone(javaTzId);
		if (timeZone == null) {
			throw new Exception("Unable to get time zone by id provided: " + javaTzId);
		}

		DateTime start = new DateTime(startDate);
		start.setTimeZone(timeZone);
		DateTime end = new DateTime(endDate);
		end.setTimeZone(timeZone);
		
		VEvent meeting = new VEvent(start, end, name);

		meeting.getProperties().add(new Description(description));
		meeting.getProperties().add(new Sequence(0));
		meeting.getProperties().add(new Location(""));
		meeting.getProperties().add(Transp.OPAQUE);

		// generate unique identifier (if not submitted)
		Uid ui = null;
		if (uid == null || uid.length() < 1) {
			ui = new UidGenerator("uidGen").generateUid();
			log.debug("Generating Meeting UID : " + ui.getValue());
		} else {
			ui = new Uid(uid);
			log.debug("Using Meeting UID : " + ui.getValue());
		}

		meeting.getProperties().add(ui);

		for (Map<String, String> att : attendees) {
			Attendee uno = new Attendee(URI.create(att.get("uri")));
			String chair = att.get("chair");
			uno.getParameters().add("0".equals(chair) ? Role.REQ_PARTICIPANT : Role.CHAIR);
			uno.getParameters().add(new Cn(att.get("cn")));
			meeting.getProperties().add(uno);
		}

		Organizer orger = new Organizer(URI.create(organizer.get("uri")));
		orger.getParameters().add(new Cn(organizer.get("cn")));

		meeting.getProperties().add(orger);

		icsCalendar.getComponents().add(timeZone.getVTimeZone());
		icsCalendar.getComponents().add(meeting);

		return ui.getValue();
	}

	/**
	 * Use this function to build a valid record for the AttendeeList for
	 * addMeetings Generate a Attendee
	 */
	public Map<String, String> getAttendeeData(String emailAdress, String displayName, boolean chair) {
		Map<String, String> oneRecord = new HashMap<String, String>();
		oneRecord.put("uri", "mailto:" + emailAdress);
		oneRecord.put("cn", displayName);
		oneRecord.put("chair", chair ? "1" : "0");

		return oneRecord;
	}

	/**
	 * Write iCal to File
	 */
	public void writeDataToFile(String filerPath) throws Exception {
		if (!filerPath.endsWith(".ics")) {
			filerPath = filerPath + ".ics";
		}

		try (FileOutputStream fout = new FileOutputStream(filerPath)) {
			CalendarOutputter outputter = new CalendarOutputter();
			outputter.output(icsCalendar, fout);
		}
	}

	/**
	 * Get IcalBody as ByteArray
	 */
	public byte[] getIcalAsByteArray() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		CalendarOutputter outputter = new CalendarOutputter();
		outputter.output(icsCalendar, bout);
		return bout.toByteArray();
	}

	/**
	 * Retrieving Data as String
	 */
	public String getICalDataAsString() {
		return icsCalendar.toString();
	}
}
