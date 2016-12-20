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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.openmeetings.core.data.whiteboard.EmoticonsManager;
import org.apache.openmeetings.core.remote.red5.ScopeApplicationAdapter;
import org.apache.openmeetings.core.remote.util.SessionVariablesUtil;
import org.apache.openmeetings.db.dao.room.RoomDao;
import org.apache.openmeetings.db.dao.server.ISessionManager;
import org.apache.openmeetings.db.entity.room.Client;
import org.apache.openmeetings.db.entity.room.Room;
import org.apache.openmeetings.util.OpenmeetingsVariables;
import org.apache.openmeetings.util.stringhandlers.ChatString;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.api.service.IServiceCapableConnection;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author sebawagner
 *
 */
public class ChatService implements IPendingServiceCallback {
	private static final Logger log = Red5LoggerFactory.getLogger(ChatService.class, OpenmeetingsVariables.webAppRootKey);
	
	@Autowired
	private ScopeApplicationAdapter scopeApplicationAdapter;
	@Autowired
	private ISessionManager sessionManager = null;
	@Autowired
	private EmoticonsManager emoticonsManager;
	@Autowired
	private RoomDao roomDao;
	
	//number of items in the chat room history
	private static final int chatRoomHistory = 50;
	
	private static Map<Long,List<Map<String,Object>>> myChats = new LinkedHashMap<Long,List<Map<String,Object>>>();
	
	private static String parseDateAsTimeString() {
		Calendar cal=Calendar.getInstance();
		
		int h = cal.get(Calendar.HOUR_OF_DAY);
		int m = cal.get(Calendar.MINUTE);

		String str_h,str_m;
		if (h<10) 
			str_h = "0"+Integer.toString(h);
		else
			str_h = Integer.toString(h);

		if (m<10) 
			str_m = "0"+Integer.toString(m);
		else
			str_m = Integer.toString(m);
		
		return str_h+':'+str_m;
	}

	public void sendChatMessage(String message) {
		IConnection current = Red5.getConnectionLocal();
		Client client = sessionManager.getClientByStreamId(current.getClient().getId(), null);
		List<String> msg = new ArrayList<String>();
		msg.add("chat"); //'privatechat'
		msg.add(""); //date-time
		msg.add("newtextmessage");
		msg.add(client.getUsername());
		msg.add(message);
		msg.add(client.getUsercolor());
		msg.add(client.getPublicSID()); //om[6] = parent.parent.isPrivate ? parent.parent.parent.refObj.publicSID : canvas.publicSID;
		msg.add("false");// canvas.isrtl;
		msg.add("" + client.getUserId());
		Room room = roomDao.get(client.getRoomId());
		msg.add("" + (room.isChatModerated() && !(client.getIsMod() || client.getIsSuperModerator())));
		sendMessageWithClient(msg);
	}
	
	/**
	 * sends a Chat-Message
	 * to all members of the Chatroom
	 * and all additional users (waitng for a free entry for example)
	 * 
	 * @param newMessage
	 * @return - 1 in case of success, -1 otherwise
	 */
	@SuppressWarnings("unchecked")
	public int sendMessageWithClient(Object newMessage) {
		try {
			IConnection current = Red5.getConnectionLocal();
			Client currentClient = sessionManager.getClientByStreamId(current.getClient().getId(), null);
			Long roomId = currentClient.getRoomId();			
			log.debug("roomId: " + roomId);
			
			if (roomId == null) {
				return 1; //TODO weird
			}
			Room room = roomDao.get(roomId);
			@SuppressWarnings("rawtypes")
			ArrayList messageMap = (ArrayList) newMessage;
			// adding delimiter space, cause otherwise an emoticon in the last
			// string would not be found
			String messageText = messageMap.get(4) + " ";
			List<String[]> parsedStringObjects = ChatString.parseChatString(messageText, emoticonsManager.getEmotfilesList(), room.getAllowFontStyles());
			// log.error("parsedStringObjects"+parsedStringObjects.size());
			log.debug("size:" + messageMap.size());
			messageMap.add(parsedStringObjects);
			newMessage = messageMap;			

			boolean needModeration = Boolean.valueOf("" + messageMap.get(9));
			List<Map<String, Object>> myChatList = myChats.get(roomId);
			if (myChatList == null) myChatList = new LinkedList<Map<String, Object>>();
			
			HashMap<String, Object> hsm = new HashMap<String, Object>();
			hsm.put("message", newMessage);
			String publicSID = "" + messageMap.get(6);
			if (!publicSID.equals(currentClient.getPublicSID())) {
				hsm.put("client", sessionManager.getClientByPublicSID("" + messageMap.get(6), null));
				//need to remove unconfirmed chat message if any
				for (int i = myChatList.size() - 1; i > -1; --i) {
					Client msgClient = (Client)myChatList.get(i).get("client");
					@SuppressWarnings("rawtypes")
					List msgList = (List)myChatList.get(i).get("message");
					if (publicSID.equals(msgClient.getPublicSID())
						&& ("" + msgList.get(4)).equals(messageMap.get(4))
						&& ("" + msgList.get(1)).equals(messageMap.get(1))
						&& Boolean.valueOf("" + msgList.get(9))) {
						myChatList.remove(i);
						break;
					}
				}
				
			} else {
				// add server time
				messageMap.set(1, parseDateAsTimeString());
				hsm.put("client", currentClient);
			}

			if (myChatList.size() == chatRoomHistory) myChatList.remove(0);
			myChatList.add(hsm);
			myChats.put(roomId, myChatList);
			log.debug("SET CHATROOM: " + roomId);

			//broadcast to everybody in the room/domain
			for (IConnection conn : current.getScope().getClientConnections()) {
				if (conn != null) {
					if (conn instanceof IServiceCapableConnection) {
						
						Client rcl = this.sessionManager.getClientByStreamId(conn.getClient().getId(), null);
						
						if (rcl == null) {
							continue;
						}
						if (rcl.isScreenClient()) {
    						continue;
    					}
						if (needModeration && !rcl.getIsMod() && !Boolean.TRUE.equals(rcl.getIsSuperModerator())) {
							continue;
						}
						((IServiceCapableConnection) conn).invoke("sendVarsToMessageWithClient",new Object[] { hsm }, this);
    			 	}
    			}
			}
		} catch (Exception err) {
			log.error("[ChatService sendMessageWithClient] ",err);
			return -1;
		}
		return 1;
	}

	//FIXME copy/past need to be removed
	@SuppressWarnings("unchecked")
	public int sendMessageWithClientByPublicSID(Object newMessage, String publicSID) {
		try {
			if (publicSID == null) {
				log.warn("Null publicSID was passed");
				return -1;
			}
			IConnection current = Red5.getConnectionLocal();
			Client currentClient = sessionManager.getClientByStreamId(current.getClient().getId(), null);
			Long room_id = currentClient.getRoomId();
			Room room = roomDao.get(room_id);
			log.debug("room_id: " + room_id);

			@SuppressWarnings("rawtypes")
			ArrayList messageMap = (ArrayList) newMessage;
			// adding delimiter space, cause otherwise an emoticon in the last
			// string would not be found
			String messageText = messageMap.get(4).toString() + " ";
			// add server time
			messageMap.set(1, parseDateAsTimeString());
			List<String[]> parsedStringObjects = ChatString.parseChatString(messageText, emoticonsManager.getEmotfilesList(), room.getAllowFontStyles());
			// log.error("parsedStringObjects"+parsedStringObjects.size());
			log.debug("size:" + messageMap.size());
			messageMap.add(parsedStringObjects);
			newMessage = messageMap;

			HashMap<String, Object> hsm = new HashMap<String, Object>();
			hsm.put("client", currentClient);
			hsm.put("message", newMessage);

			// broadcast to everybody in the room/domain
			for (IConnection conn : current.getScope().getClientConnections()) {
				if (conn != null) {
					if (conn instanceof IServiceCapableConnection) {
						IClient client = conn.getClient();
						String clientPublicSid = SessionVariablesUtil.getPublicSID(client);
						if (SessionVariablesUtil.isScreenClient(client)) {
							// screen sharing clients do not receive events
							continue;
						}
						if (clientPublicSid == null) {
							continue;
						}

						if (publicSID.equals(clientPublicSid) || clientPublicSid.equals(currentClient.getPublicSID())) {
							((IServiceCapableConnection) conn).invoke("sendVarsToMessageWithClient", new Object[] { hsm }, this);
						}
					}
				}
			}
		} catch (Exception err) {
			log.error("[ChatService sendMessageWithClient] ", err);
			return -1;
		}
		return 1;
	}

	public List<Map<String,Object>> clearChat() {
		try {
			IConnection current = Red5.getConnectionLocal();
			Client currentClient = this.sessionManager.getClientByStreamId(current.getClient().getId(), null);
			Long room_id = currentClient.getRoomId();
			
			Long chatroom = room_id;
			log.debug("### GET CHATROOM: "+chatroom);
			
			List<Map<String,Object>> myChatList = myChats.get(chatroom);
			myChatList = new LinkedList<Map<String,Object>>();
			
			myChats.put(chatroom,myChatList);
			
			HashMap<String,Object> hsm = new HashMap<String,Object>();
			
			scopeApplicationAdapter.sendMessageToCurrentScope("clearChatContent", hsm, true);
			
			return myChatList;
			
		} catch (Exception err) {
			log.error("[clearChat] ",err);
			return null;
		}
	}
	
	public List<Map<String,Object>> getRoomChatHistory() {
		try {
			IConnection current = Red5.getConnectionLocal();
			Client currentClient = this.sessionManager.getClientByStreamId(current.getClient().getId(), null);
			Long roomId = currentClient.getRoomId();
			
			log.debug("GET CHATROOM: " + roomId);
			
			List<Map<String,Object>> myChatList = myChats.get(roomId);
			if (myChatList==null) myChatList = new LinkedList<Map<String,Object>>();
			
			if (!currentClient.getIsMod() && !currentClient.getIsSuperModerator()) {
				//current user is not moderator, chat history need to be filtered
				List<Map<String,Object>> tmpChatList = new LinkedList<Map<String,Object>>(myChatList);
				for (int i = tmpChatList.size() - 1; i > -1; --i) {
					@SuppressWarnings("rawtypes")
					List msgList = (List)tmpChatList.get(i).get("message");
					if (Boolean.valueOf("" + msgList.get(9))) { //needModeration
						tmpChatList.remove(i);
					}
				}
				myChatList = tmpChatList;
			}
			
			return myChatList;
		} catch (Exception err) {
			log.error("[getRoomChatHistory] ",err);
			return null;
		}
	}
	
	/**
	 * gets the chat history by string for non-conference-clients
	 * 
	 * @param room_id
	 * @return - chat history of the room given, null in case of exception
	 */
	public List<Map<String,Object>> getRoomChatHistoryByString(Long room_id) {
		try {
			Long chatroom = room_id;
			log.debug("GET CHATROOM: "+chatroom);
			
			List<Map<String,Object>> myChatList = myChats.get(chatroom);
			if (myChatList==null) myChatList = new LinkedList<Map<String,Object>>();	
			
			return myChatList;
		} catch (Exception err) {
			log.error("[getRoomChatHistory] ",err);
			return null;
		}
	}	
	
	@Override
	public void resultReceived(IPendingServiceCall arg0) {
		// TODO Auto-generated method stub
		log.error("resultReceived ChatService "+arg0);
	}
	
	public List<List<String>> getAllPublicEmoticons(){
		try {
			List<List<String>> publicemotes = new LinkedList<List<String>>();
			List<List<String>> allEmotes = emoticonsManager.getEmotfilesList();
			for (List<String> emot : allEmotes){
				List<String> emotPub = new LinkedList<>();
				if (emot.get((emot.size()-1)).equals("y")){
					emotPub.add(emot.get(0));
					emotPub.add(emot.get(1).replace("\\", ""));
					if (emot.size()>4) {
						emotPub.add(emot.get(2).replace("\\", ""));
						emotPub.add(emot.get(3));
						emotPub.add(emot.get(4));
					} else {
						emotPub.add(emot.get(2));
						emotPub.add(emot.get(3));
					}
					publicemotes.add(emotPub);
				}
			}
			return publicemotes;
		} catch (Exception err) {
			log.error("[getAllPublicEmoticons] ",err);
			return null;
		}
	}
	
}
