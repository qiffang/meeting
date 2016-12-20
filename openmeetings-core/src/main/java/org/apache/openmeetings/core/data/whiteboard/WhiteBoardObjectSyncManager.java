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
package org.apache.openmeetings.core.data.whiteboard;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.openmeetings.db.dto.room.WhiteboardSyncLockObject;
import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

/**
 * Memory based, configured as singleton in spring configuration
 * 
 * @author sebawagner
 *
 */
public class WhiteBoardObjectSyncManager {
	private Map<Long, Map<String, WhiteboardSyncLockObject>> whiteBoardSyncList = new ConcurrentHashMap<Long, Map<String, WhiteboardSyncLockObject>>();
	private Map<Long, Map<String, Map<String, WhiteboardSyncLockObject>>> whiteBoardObjectSyncList = new ConcurrentHashMap<Long, Map<String, Map<String, WhiteboardSyncLockObject>>>();

	private static final Logger log = Red5LoggerFactory.getLogger(WhiteBoardObjectSyncManager.class, webAppRootKey);

	/*
	 * Initial Sync Process
	 */
	public void setWhiteBoardSyncListByRoomid(Long roomId, Map<String, WhiteboardSyncLockObject> mapObject) {
		whiteBoardSyncList.put(roomId, mapObject);
	}

	public Map<String, WhiteboardSyncLockObject> getWhiteBoardSyncListByRoomid(Long roomId) {
		if (whiteBoardSyncList.containsKey(roomId)) {
			return whiteBoardSyncList.get(roomId);
		} else {
			return new HashMap<String, WhiteboardSyncLockObject>();
		}
	}

	/*
	 * Image Sync Process
	 */
	public void setWhiteBoardImagesSyncListByRoomid(Long roomId, Map<String, Map<String, WhiteboardSyncLockObject>> mapObject) {
		whiteBoardObjectSyncList.put(roomId, mapObject);
	}

	public void setWhiteBoardImagesSyncListByRoomAndObjectId(Long roomId, String objectId, Map<String, WhiteboardSyncLockObject> imageSyncList) {
		Map<String, Map<String, WhiteboardSyncLockObject>> roomList = getWhiteBoardObjectSyncListByRoomid(roomId);
		if (imageSyncList.isEmpty()) {
			roomList.remove(objectId);
		} else {
			roomList.put(objectId, imageSyncList);
		}
		setWhiteBoardImagesSyncListByRoomid(roomId, roomList);
	}

	public Map<String, Map<String, WhiteboardSyncLockObject>> getWhiteBoardObjectSyncListByRoomid(Long roomId) {
		if (whiteBoardObjectSyncList.containsKey(roomId)) {
			return whiteBoardObjectSyncList.get(roomId);
		} else {
			return new HashMap<String, Map<String, WhiteboardSyncLockObject>>();
		}
	}

	public Map<String, WhiteboardSyncLockObject> getWhiteBoardObjectSyncListByRoomAndObjectId(Long roomId, String objectId) {
		log.debug("getWhiteBoardImagesSyncListByRoomAndImageid roomId: " + roomId);
		Map<String, Map<String, WhiteboardSyncLockObject>> roomList = getWhiteBoardObjectSyncListByRoomid(roomId);
		if (log.isDebugEnabled()) {
			log.debug("getWhiteBoardImagesSyncListByRoomAndImageid roomList: " + roomList);
			log.debug("getWhiteBoardImagesSyncListByRoomAndImageid objectId: " + objectId);
			if (roomList.size() == 1) {
				log.debug("getWhiteBoardImagesSyncListByRoomAndImageid roomList Key imageId: " + roomList.keySet().iterator().next());
			}
		}
		Map<String, WhiteboardSyncLockObject> imageSyncList = roomList.get(objectId);
		if (imageSyncList == null) {
			imageSyncList = new HashMap<String, WhiteboardSyncLockObject>();
		}
		return imageSyncList;
	}
}
