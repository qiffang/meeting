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

import static org.apache.openmeetings.util.OmFileHelper.thumbImagePrefix;
import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.apache.openmeetings.util.OmFileHelper;
import org.apache.openmeetings.util.process.ConverterProcessResult;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

public class CreateLibraryPresentation {
	private static final Logger log = Red5LoggerFactory.getLogger(CreateLibraryPresentation.class, webAppRootKey);
	
	public static ConverterProcessResult generateXMLDocument(File targetDirectory, String originalDocument, 
			String pdfDocument, String swfDocument){
		ConverterProcessResult returnMap = new ConverterProcessResult();
		returnMap.setProcess("generateXMLDocument");		
		try {
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("presentation");
			
			File file = new File(targetDirectory, originalDocument);
			root.addElement("originalDocument")
				.addAttribute("lastmod", (new Long(file.lastModified())).toString())
				.addAttribute("size", (new Long(file.length())).toString())	        
				.addText(originalDocument);

			if (pdfDocument!=null){
				File pdfDocumentFile = new File(targetDirectory, pdfDocument);
				root.addElement("pdfDocument")
					.addAttribute("lastmod", (new Long(pdfDocumentFile.lastModified())).toString())
					.addAttribute("size", (new Long(pdfDocumentFile.length())).toString())	   		        
					.addText(pdfDocument);
			}

			if (swfDocument!=null){
				File swfDocumentFile = new File(targetDirectory, originalDocument);
				root.addElement("swfDocument")
					.addAttribute("lastmod", (new Long(swfDocumentFile.lastModified())).toString())
					.addAttribute("size", (new Long(swfDocumentFile.length())).toString())
					.addText(swfDocument);
			}

			Element thumbs = root.addElement( "thumbs" );

			//Second get all Files of this Folder
			FilenameFilter ff = new FilenameFilter() {
				@Override
				public boolean accept(File b, String name) {
					File f = new File(b, name);
					return f.isFile();
				}
			};	
			
			String[] allfiles = targetDirectory.list(ff);			
			if(allfiles!=null){
				Arrays.sort(allfiles);
				for(int i=0; i<allfiles.length; i++){
					File thumbfile = new File(targetDirectory, allfiles[i]);
					if (allfiles[i].startsWith(thumbImagePrefix)){
						thumbs.addElement( "thumb" )
							.addAttribute("lastmod", (new Long(thumbfile.lastModified())).toString())
							.addAttribute("size", (new Long(thumbfile.length())).toString())
							.addText(allfiles[i]);
					}
				}
			}
			
			// lets write to a file
			XMLWriter writer = new XMLWriter(new FileOutputStream(new File(targetDirectory, OmFileHelper.libraryFileName)));
			writer.write(document);
			writer.close();
			
			returnMap.setExitValue("0");
			
			return returnMap;
		} catch (Exception err) {
			log.error("Error while generateXMLDocument", err);
			returnMap.setError(err.getMessage());
			returnMap.setExitValue("-1");
			return returnMap;
		}
	}
}
