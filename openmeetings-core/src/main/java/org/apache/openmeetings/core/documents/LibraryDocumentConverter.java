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
package org.apache.openmeetings.core.documents;

import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.openmeetings.util.OmFileHelper;
import org.apache.openmeetings.util.stringhandlers.StringComparer;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class LibraryDocumentConverter {
	private static final Logger log = Red5LoggerFactory.getLogger(LibraryDocumentConverter.class, webAppRootKey);
	
	private static final String fileExt = ".wml";
	
	public static String writeToLocalFolder(String fileName, @SuppressWarnings("rawtypes")ArrayList objList) {
		try {
			log.debug("filePath: " + OmFileHelper.getUploadWmlDir().getCanonicalPath());
			
			String fileNameExtName = fileName.substring(fileName.length()-4,fileName.length());
			if (fileNameExtName.equals(fileExt)){
				fileName = StringComparer.getInstance().compareForRealPaths(fileName.substring(0, fileName.length()-4));
			} else {
				fileName = StringComparer.getInstance().compareForRealPaths(fileName.substring(0, fileName.length()));
			}
			
			if (fileName.length() <= 0){
				//return new Long(-21);
				return "-20";
			}
			//Add the Folder for the wmlFiles if it does not exist yet
			File file = new File(OmFileHelper.getUploadWmlDir(), fileName + fileExt);
			
			if (file.exists()){
				return "-20";
			}		
			
			XStream xStream = new XStream(new XppDriver());
			xStream.setMode(XStream.NO_REFERENCES);
			String xmlString = xStream.toXML(objList);	
			
			log.debug("Write to " + file);
			
			try (OutputStream os = new FileOutputStream(file);
					Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8))
			{
				out.write(xmlString);
				out.flush();
			}
	    
		    //return new Long(1);
		    
			return file.getCanonicalPath();
		} catch (Exception err){
			log.error("writeToLocalFolder",err);
		}
		
		return null;
	}
}
