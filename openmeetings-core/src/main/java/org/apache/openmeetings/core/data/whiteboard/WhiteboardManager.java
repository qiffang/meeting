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

import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.openmeetings.db.dto.room.WhiteboardObject;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class WhiteboardManager {
	private static final Logger log = Red5LoggerFactory.getLogger(WhiteboardManager.class, webAppRootKey);
	
	@Autowired
	private WhiteBoardObjectListManagerById wbListManagerById;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addWhiteBoardObjectById(Long roomId, Map whiteboardObj, Long whiteBoardId) {
		try {
			log.debug("addWhiteBoardObjectById: ", whiteboardObj);

			String action = whiteboardObj.get(2).toString();

			log.debug("action: " + action);
			log.debug("actionObject: " + whiteboardObj.get(3));

			List actionObject = (List) whiteboardObj.get(3);

			if (action.equals("moveMap")) {
				WhiteboardObject whiteboardObject = wbListManagerById.getWhiteBoardObjectListByRoomIdAndWhiteboard(roomId, whiteBoardId);

				whiteboardObject.setX(Integer.valueOf(actionObject.get(1).toString()));
				whiteboardObject.setY(Integer.valueOf(actionObject.get(2).toString()));

				wbListManagerById.setWhiteBoardObjectListRoomObjAndWhiteboardId(roomId, whiteboardObject, whiteBoardId);
			} else if (action.equals("draw") || action.equals("redo")) {
				WhiteboardObject whiteboardObject = wbListManagerById.getWhiteBoardObjectListByRoomIdAndWhiteboard(roomId, whiteBoardId);

				// log.debug(actionObject);
				// log.debug(actionObject.size()-1);
				// log.debug(actionObject.get(actionObject.size()-1));
				String objectType = actionObject.get(0).toString();
				if (!objectType.equals("pointerWhiteBoard")) {
					String objectOID = actionObject.get(actionObject.size() - 1).toString();
					log.debug("objectOID: " + objectOID);
					whiteboardObject.getRoomItems().put(objectOID, actionObject);
					wbListManagerById.setWhiteBoardObjectListRoomObjAndWhiteboardId(roomId, whiteboardObject, whiteBoardId);
				}
			} else if (action.equals("clear")) {
				WhiteboardObject whiteboardObject = wbListManagerById.getWhiteBoardObjectListByRoomIdAndWhiteboard(roomId, whiteBoardId);
				whiteboardObject.setRoomItems(new HashMap<String, List>());
				wbListManagerById.setWhiteBoardObjectListRoomObjAndWhiteboardId(roomId, whiteboardObject, whiteBoardId);
			} else if (action.equals("delete") || action.equals("undo")) {
				WhiteboardObject whiteboardObject = wbListManagerById.getWhiteBoardObjectListByRoomIdAndWhiteboard(roomId, whiteBoardId);
				String objectOID = actionObject.get(actionObject.size() - 1).toString();
				String objectType = actionObject.get(0).toString();
				log.debug("removal of objectOID: " + objectOID);

				log.debug("removal of objectOID: " + objectOID);

				// Re-Index all items in its zIndex
				if (objectType.equals("ellipse")
						|| objectType.equals("drawarrow")
						|| objectType.equals("line")
						|| objectType.equals("paint")
						|| objectType.equals("rectangle")
						|| objectType.equals("uline")
						|| objectType.equals("image")
						|| objectType.equals("letter")
						|| objectType.equals("clipart")
						|| objectType.equals("swf")
						|| objectType.equals("mindmapnode")
						|| objectType.equals("flv")) {

					Integer zIndex = Integer.valueOf(actionObject.get(actionObject.size() - 8).toString());

					log.debug("1|zIndex " + zIndex);
					log.debug("2|zIndex " + actionObject.get(actionObject.size() - 8).toString());
					log.debug("3|zIndex " + actionObject.get(actionObject.size() - 8));

					int l = 0;
					for (Object o : actionObject) {
						log.debug("4|zIndex " + l + " -- " + o);
						l++;
					}

					for (Entry<String, List> e : whiteboardObject.getRoomItems().entrySet()) {
						List actionObjectStored = e.getValue();

						Integer zIndexStored = Integer.valueOf(actionObjectStored.get(actionObjectStored.size() - 8).toString());

						log.debug("zIndexStored|zIndex " + zIndexStored + "|" + zIndex);

						if (zIndexStored >= zIndex) {
							zIndexStored -= 1;
							log.debug("new-zIndex " + zIndexStored);
						}
						actionObjectStored.set(actionObjectStored.size() - 8, zIndexStored);

						whiteboardObject.getRoomItems().put(e.getKey(), actionObjectStored);
					}
				}
				whiteboardObject.getRoomItems().remove(objectOID);

				wbListManagerById.setWhiteBoardObjectListRoomObjAndWhiteboardId(roomId, whiteboardObject, whiteBoardId);
			} else if (action.equals("size") || action.equals("editProp")
					|| action.equals("editTextMindMapNode")
					|| action.equals("editText") || action.equals("swf")
					|| action.equals("flv")
					|| action.equals("editTextMindMapColor")
					|| action.equals("editTextMindMapFontColor")) {
				WhiteboardObject whiteboardObject = wbListManagerById.getWhiteBoardObjectListByRoomIdAndWhiteboard(roomId, whiteBoardId);
				String objectOID = actionObject.get(actionObject.size() - 1).toString();
				// List roomItem = roomList.get(objectOID);
				List currentObject = whiteboardObject.getRoomItems().get(objectOID);
				if (actionObject.get(0).equals("paint")) {
					actionObject.set(1, currentObject.get(1));
				}
				whiteboardObject.getRoomItems().put(objectOID, actionObject);

				Map<String, List> roomList = whiteboardObject.getRoomItems();

				if (action.equals("swf")) {
					log.debug("actionObject " + actionObject);
					if (actionObject.get(0).equals("swf")) {
						if (actionObject.get(8) != currentObject.get(8)) {
							String baseObjectName = actionObject.get(actionObject.size() - 1).toString();
							Integer slidesNumber = Integer.valueOf(actionObject.get(8).toString());

							log.debug("updateObjectsToSlideNumber :: " + baseObjectName + "," + slidesNumber);

							for (Entry<String, List> me : roomList.entrySet()) {
								List actionObjectStored = me.getValue();

								if (actionObjectStored.get(0).equals("ellipse")
										|| actionObjectStored.get(0).equals("drawarrow")
										|| actionObjectStored.get(0).equals("line")
										|| actionObjectStored.get(0).equals("clipart")
										|| actionObjectStored.get(0).equals("paint")
										|| actionObjectStored.get(0).equals("rectangle")
										|| actionObjectStored.get(0).equals("uline")
										|| actionObjectStored.get(0).equals("image")
										|| actionObjectStored.get(0).equals("letter")) {

									Map swfObj = (Map) actionObjectStored.get(actionObjectStored.size() - 7);
									log.debug("swfObj :1: " + swfObj);

									if (swfObj != null) {
										if (swfObj.get("name").equals(baseObjectName)) {
											swfObj.put("isVisible", Integer.parseInt(swfObj.get("slide").toString()) == slidesNumber);
											actionObjectStored.set(actionObjectStored.size() - 7, swfObj);
										}
									}

									log.debug("swfObj :1: " + swfObj);
								}
							}
						}
					}
				}

				wbListManagerById.setWhiteBoardObjectListRoomObjAndWhiteboardId(roomId, whiteboardObject, whiteBoardId);
			} else if (action.equals("clearSlide")) {
				WhiteboardObject whiteboardObject = wbListManagerById.getWhiteBoardObjectListByRoomIdAndWhiteboard(roomId, whiteBoardId);

				Map roomList = whiteboardObject.getRoomItems();

				for (String objectName : (List<String>) actionObject) {
					roomList.remove(objectName);
				}

				wbListManagerById.setWhiteBoardObjectListRoomObjAndWhiteboardId(roomId, whiteboardObject, whiteBoardId);
			} else if (action.equals("whiteboardObj")) {
				WhiteboardObject whiteboardObject = wbListManagerById.getWhiteBoardObjectListByRoomIdAndWhiteboard(roomId, whiteBoardId);

				whiteboardObject.setFullFit((Boolean) actionObject.get(1));
				whiteboardObject.setZoom((Integer) actionObject.get(2));

				wbListManagerById.setWhiteBoardObjectListRoomObjAndWhiteboardId(roomId, whiteboardObject, whiteBoardId);
			} else {
				log.warn("Unkown Type: " + action + " actionObject: " + actionObject);
			}
		} catch (Exception err) {
			log.error("[addWhiteBoardObject]", err);
		}
	}
}
