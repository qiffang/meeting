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
package org.apache.openmeetings.core.remote;

import static org.apache.openmeetings.util.OmFileHelper.MP4_EXTENSION;
import static org.apache.openmeetings.util.OmFileHelper.WB_VIDEO_FILE_PREFIX;
import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.transaction.util.FileHelper;
import org.apache.openmeetings.core.data.whiteboard.WhiteboardManager;
import org.apache.openmeetings.core.documents.LibraryChartLoader;
import org.apache.openmeetings.core.documents.LibraryDocumentConverter;
import org.apache.openmeetings.core.documents.LibraryWmlLoader;
import org.apache.openmeetings.core.documents.LoadLibraryPresentation;
import org.apache.openmeetings.db.dao.file.FileExplorerItemDao;
import org.apache.openmeetings.db.dao.server.ISessionManager;
import org.apache.openmeetings.db.dao.server.SessiondataDao;
import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.dto.file.LibraryPresentation;
import org.apache.openmeetings.db.entity.file.FileExplorerItem;
import org.apache.openmeetings.db.entity.file.FileItem.Type;
import org.apache.openmeetings.db.entity.room.Client;
import org.apache.openmeetings.db.util.AuthLevelUtil;
import org.apache.openmeetings.util.OmFileHelper;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.api.service.IServiceCapableConnection;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author swagner
 * 
 */
public class ConferenceLibrary implements IPendingServiceCallback {
	private static final Logger log = Red5LoggerFactory.getLogger(ConferenceLibrary.class, webAppRootKey);

	@Autowired
	private ISessionManager sessionManager;
	@Autowired
	private SessiondataDao sessionDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private FileExplorerItemDao fileDao;
	@Autowired
	private WhiteboardManager whiteboardManagement;

	public LibraryPresentation getPresentationPreviewFileExplorer(String SID, String parentFolder) {
		try {
			Long users_id = sessionDao.check(SID);

			log.debug("#############users_id : " + users_id);

			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				File working_dir = new File(OmFileHelper.getUploadFilesDir(), parentFolder);
				log.debug("############# working_dir : " + working_dir);

				File file = new File(working_dir, OmFileHelper.libraryFileName);

				if (!file.exists()) {
					throw new Exception(file.getCanonicalPath() + ": does not exist");
				}
				return LoadLibraryPresentation.parseLibraryFileToObject(file);
			} else {
				throw new Exception("not Authenticated");
			}
		} catch (Exception e) {
			log.error("[getListOfFilesByAbsolutePath]", e);
			return null;
		}
	}

	/**
	 * 
	 * Save an Object to the library and returns the file-explorer Id
	 * 
	 * @param SID
	 * @param roomId
	 * @param fileName
	 * @param tObjectRef
	 * @return - file-explorer Id in case of success, -1 otherwise
	 */
	public Long saveAsObject(String SID, Long roomId, String fileName, Object tObjectRef) {
		try {
			Long users_id = sessionDao.check(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				// LinkedHashMap tObject = (LinkedHashMap)t;
				// ArrayList tObject = (ArrayList)t;

				log.debug("saveAsObject :1: " + tObjectRef);
				log.debug("saveAsObject :2: " + tObjectRef.getClass().getName());

				@SuppressWarnings("rawtypes")
				ArrayList tObject = (ArrayList) tObjectRef;

				log.debug("saveAsObject" + tObject.size());

				FileExplorerItem file = fileDao.add(fileName, null, null, roomId, users_id, Type.WmlFile, "", "");
				LibraryDocumentConverter.writeToLocalFolder(file.getHash(), tObject);

				return file.getId();
			}
		} catch (Exception err) {
			log.error("[saveAsObject] ", err);
		}
		return -1L;
	}

	/**
	 * Loads a Object from the library into the whiteboard of all participant of
	 * the current room
	 * 
	 * @param SID
	 * @param room_id
	 * @param fileId
	 * @param whiteboardId
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void loadWmlObject(String SID, Long room_id, Long fileId, Long whiteboardId) {
		try {
			Long users_id = sessionDao.check(SID);

			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				IConnection current = Red5.getConnectionLocal();
				Client currentClient = sessionManager.getClientByStreamId(current.getClient().getId(), null);

				if (currentClient == null) {
					return;
				}

				FileExplorerItem fi = fileDao.get(fileId);
				if (fi == null) {
					log.warn("[loadWmlObject] Unable to load Wml file by Id {}", fileId);
					return;
				}

				List<?> roomItems = LibraryWmlLoader.loadWmlFile(fi.getHash());

				Map whiteboardObjClear = new HashMap();
				whiteboardObjClear.put(2, "clear");
				whiteboardObjClear.put(3, null);

				whiteboardManagement.addWhiteBoardObjectById(room_id, whiteboardObjClear, whiteboardId);

				for (int k = 0; k < roomItems.size(); k++) {
					ArrayList actionObject = (ArrayList)roomItems.get(k);

					Map whiteboardObj = new HashMap();
					whiteboardObj.put(2, "draw");
					whiteboardObj.put(3, actionObject);

					whiteboardManagement.addWhiteBoardObjectById(room_id, whiteboardObj, whiteboardId);
				}

				Map<String, Object> sendObject = new HashMap<String, Object>();
				sendObject.put("id", whiteboardId);
				sendObject.put("roomitems", roomItems);

				// Notify all Clients of that Scope (Room)
				for (IConnection conn : current.getScope().getClientConnections()) {
					if (conn != null) {
						if (conn instanceof IServiceCapableConnection) {
							Client rcl = sessionManager.getClientByStreamId(conn.getClient().getId(), null);
							if ((rcl == null) || rcl.isScreenClient()) {
								continue;
							} else {
								((IServiceCapableConnection) conn).invoke("loadWmlToWhiteboardById", new Object[] { sendObject }, this);
							}
						}
					}
				}
			}
		} catch (Exception err) {
			log.error("[loadWmlObject] ", err);
		}
	}

	/**
	 * 
	 * Loads a chart object
	 * 
	 * @param SID
	 * @param room_id
	 * @param fileName
	 * @return - chart object
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList loadChartObject(String SID, Long room_id, String fileName) {
		try {
			Long users_id = sessionDao.check(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				return LibraryChartLoader.getInstance().loadChart(OmFileHelper.getUploadRoomDir(room_id.toString()), fileName);
			}
		} catch (Exception err) {
			log.error("[loadChartObject] ", err);
		}
		return null;
	}

	/**
	 * @param SID
	 * @param fileId
	 * @return 1 in case of success, -1 otherwise
	 */
	public Long copyFileToCurrentRoom(String SID, Long fileId) {
		try {
			Long users_id = sessionDao.check(SID);

			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {

				IConnection current = Red5.getConnectionLocal();
				String streamid = current.getClient().getId();

				Client currentClient = sessionManager.getClientByStreamId(streamid, null);

				Long roomId = currentClient.getRoomId();

				if (roomId != null) {
					File outputFullFlvFile = new File(OmFileHelper.getStreamsHibernateDir(), WB_VIDEO_FILE_PREFIX + fileId + MP4_EXTENSION);

					File targetFolder = OmFileHelper.getStreamsSubDir(roomId);

					File targetFullFlvFile = new File(targetFolder, WB_VIDEO_FILE_PREFIX + fileId + MP4_EXTENSION);
					if (outputFullFlvFile.exists() && !targetFullFlvFile.exists()) {
						FileHelper.copy(outputFullFlvFile, targetFullFlvFile);
					}
					return 1L;
				}
			}
		} catch (Exception err) {
			log.error("[copyFileToCurrentRoom] ", err);
		}
		return -1L;
	}

	@Override
	public void resultReceived(IPendingServiceCall arg0) {
		// TODO Auto-generated method stub
	}
}
