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
package org.apache.openmeetings.web.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.openmeetings.web.app.WebSession;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

public class CountryDropDown extends DropDownChoice<String>  {
	private static final long serialVersionUID = 1L;

	public CountryDropDown(String id) {
		this(id, null);
	}
	
	public static String getCountryName(String code) {
		return new Locale.Builder().setRegion(code).build().getDisplayCountry(WebSession.get().getLocale());
	}
	
	public CountryDropDown(String id, IModel<String> model) {
		super(id, model, Arrays.asList(Locale.getISOCountries()));
		setChoiceRenderer(new IChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getIdValue(String code, int index) {
				return code;
			}
			
			@Override
			public Object getDisplayValue(String code) {
				return getCountryName(code);
			}

			@Override
			public String getObject(String id, IModel<? extends List<? extends String>> choices) {
				for (String code : choices.getObject()) {
					if (code.equals(id)) {
						return code;
					}
				}
				return null;
			}
		});
	}
}
