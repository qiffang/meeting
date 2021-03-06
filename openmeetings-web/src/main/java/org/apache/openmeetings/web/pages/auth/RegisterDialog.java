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
package org.apache.openmeetings.web.pages.auth;

import static org.apache.openmeetings.db.util.UserHelper.getMinLoginLength;
import static org.apache.openmeetings.db.util.UserHelper.getMinPasswdLength;
import static org.apache.openmeetings.util.OpenmeetingsVariables.CONFIG_DEFAULT_GROUP_ID;
import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;
import static org.apache.openmeetings.web.app.Application.getBean;
import static org.apache.openmeetings.web.app.WebSession.AVAILABLE_TIMEZONES;
import static org.apache.wicket.validation.validator.StringValidator.minimumLength;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.openmeetings.db.dao.basic.ConfigurationDao;
import org.apache.openmeetings.db.dao.user.IUserManager;
import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.web.app.Application;
import org.apache.openmeetings.web.app.WebSession;
import org.apache.openmeetings.web.common.LanguageDropDown;
import org.apache.openmeetings.web.util.CountryDropDown;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import com.googlecode.wicket.jquery.core.JQueryBehavior;
import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.ui.widget.dialog.AbstractFormDialog;
import com.googlecode.wicket.jquery.ui.widget.dialog.DialogButton;
import com.googlecode.wicket.jquery.ui.widget.dialog.DialogButtons;
import com.googlecode.wicket.jquery.ui.widget.dialog.DialogIcon;
import com.googlecode.wicket.jquery.ui.widget.dialog.MessageDialog;
import com.googlecode.wicket.kendo.ui.panel.KendoFeedbackPanel;

public class RegisterDialog extends AbstractFormDialog<String> {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Red5LoggerFactory.getLogger(RegisterDialog.class, webAppRootKey);
	private DialogButton cancelBtn = new DialogButton("cancel", Application.getString(122));
	private DialogButton registerBtn = new DialogButton("register", Application.getString(121));
	private final KendoFeedbackPanel feedback = new KendoFeedbackPanel("feedback", new Options("button", true));
	private final IModel<String> tzModel = Model.of(WebSession.get().getClientTZCode());
	private final DropDownChoice<String> tzDropDown = new DropDownChoice<String>("tz", tzModel, AVAILABLE_TIMEZONES);
	private RegisterForm form;
	private SignInDialog s;
	private String firstName;
	private String lastName;
	private String login;
	private String password;
	private String email;
	private String country;
	private Long lang;

	final MessageDialog confirmRegistration;
	private boolean sendConfirmation = false;
	private boolean sendEmailAtRegister = false;

	public RegisterDialog(String id) {
		super(id, Application.getString(113));
		add(form = new RegisterForm("form"));
		form.setOutputMarkupId(true);
		tzDropDown.setOutputMarkupId(true);

		confirmRegistration = new MessageDialog("confirmRegistration", Application.getString(235),
				Application.getString(674), DialogButtons.OK, DialogIcon.INFO) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onConfigure(JQueryBehavior behavior) {
				super.onConfigure(behavior);
				behavior.setOption("dialogClass", Options.asString("no-close"));
				behavior.setOption("closeOnEscape", false);
			}

			@Override
			public void onClose(IPartialPageRequestHandler handler, DialogButton button) {
				s.open(handler);
			}
		};
		add(confirmRegistration);
		reset();
	}

	@Override
	public void onConfigure(JQueryBehavior behavior) {
		super.onConfigure(behavior);
		behavior.setOption("dialogClass", Options.asString("no-close"));
		behavior.setOption("closeOnEscape", false);
	}

	public void setSignInDialog(SignInDialog s) {
		this.s = s;
	}

	public void setClientTimeZone() {
		tzModel.setObject(WebSession.get().getClientTZCode());
	}
	
	@Override
	public int getWidth() {
		return 400;
	}

	@Override
	protected List<DialogButton> getButtons() {
		return Arrays.asList(registerBtn, cancelBtn);
	}

	public void reset() {
		firstName = null;
		lastName = null;
		login = null;
		password = null;
		form.confirmPassword.setModelObject(null);
		email = null;
		lang = WebSession.get().getLanguageByBrowserLocale();
		country = WebSession.get().getBrowserLocale().getCountry();
	}

	public void onOpen(AjaxRequestTarget target) {
		ConfigurationDao cfgDao = getBean(ConfigurationDao.class);
		String baseURL = cfgDao.getBaseUrl();
		sendEmailAtRegister = 1 == cfgDao.getConfValue("sendEmailAtRegister", Integer.class, "0");
		sendConfirmation = !Strings.isEmpty(baseURL)
				&& 1 == cfgDao.getConfValue("sendEmailWithVerficationCode", Integer.class, "0");
		long messageCode = 236;
		if (sendConfirmation && sendEmailAtRegister) {
			messageCode = 674;
		} else if (sendConfirmation) {
			messageCode = 1591;
		}
		confirmRegistration.setModelObject(Application.getString(messageCode));
		reset();
		target.add(form);
	}

	@Override
	public void onClose(IPartialPageRequestHandler handler, DialogButton button) {
		if (!registerBtn.equals(button)) {
			s.open(handler);
		}
	}

	@Override
	public DialogButton getSubmitButton() {
		return registerBtn;
	}

	@Override
	public Form<Void> getForm() {
		return form;
	}

	@Override
	protected void onError(AjaxRequestTarget target) {
		target.add(feedback);
	}

	@Override
	protected void onSubmit(AjaxRequestTarget target) {
		String hash = UUID.randomUUID().toString();

		try {
			getBean(IUserManager.class).registerUserInit(UserDao.getDefaultRights(), login, password, lastName
					, firstName, email, null /* age/birthday */, "" /* street */
					, "" /* additionalname */, "" /* fax */, "" /* zip */, country
					, "" /* town */, lang, true /* sendWelcomeMessage */
					, Arrays.asList(getBean(ConfigurationDao.class).getConfValue(CONFIG_DEFAULT_GROUP_ID, Long.class, null)),
					"" /* phone */, false, sendConfirmation, TimeZone.getTimeZone(tzModel.getObject()),
					false /* forceTimeZoneCheck */, "" /* userOffers */, "" /* userSearchs */, false /* showContactData */,
					true /* showContactDataToContacts */, hash);

		} catch (Exception e) {
			log.error("[registerUser]", e);
		}

		confirmRegistration.open(target);
	}

	@Override
	protected void onDetach() {
		tzModel.detach();
		super.onDetach();
	}
	
	class RegisterForm extends StatelessForm<Void> {
		private static final long serialVersionUID = 1L;
		private PasswordTextField confirmPassword;
		private PasswordTextField passwordField;
		private RequiredTextField<String> emailField;
		private RequiredTextField<String> loginField;
		private RequiredTextField<String> firstNameField;
		private RequiredTextField<String> lastNameField;
		private LanguageDropDown langField;
		private CountryDropDown countryField;

		public RegisterForm(String id) {
			super(id);
			add(feedback.setOutputMarkupId(true));
			ConfigurationDao cfgDao = getBean(ConfigurationDao.class);
			add(firstNameField = new RequiredTextField<String>("firstName", new PropertyModel<String>(
					RegisterDialog.this, "firstName")));
			firstNameField.setLabel(Model.of(Application.getString(117)));
			add(lastNameField = new RequiredTextField<String>("lastName", new PropertyModel<String>(
					RegisterDialog.this, "lastName")));
			lastNameField.setLabel(Model.of(Application.getString(118)));
			add(loginField = new RequiredTextField<String>("login", new PropertyModel<String>(RegisterDialog.this,
					"login")));
			loginField.setLabel(Model.of(Application.getString(114)));
			loginField.add(minimumLength(getMinLoginLength(cfgDao)));
			add(passwordField = new PasswordTextField("password", new PropertyModel<String>(RegisterDialog.this,
					"password")));
			passwordField.setLabel(Model.of(Application.getString(115)));
			passwordField.setResetPassword(true).add(minimumLength(getMinPasswdLength(cfgDao)));
			add(confirmPassword = new PasswordTextField("confirmPassword", new Model<String>()).setResetPassword(true));
			confirmPassword.setLabel(Model.of(Application.getString(116)));
			add(emailField = new RequiredTextField<String>("email", new PropertyModel<String>(RegisterDialog.this,
					"email")));
			emailField.setLabel(Model.of(Application.getString(119)));
			emailField.add(RfcCompliantEmailAddressValidator.getInstance());
			add(langField = new LanguageDropDown("lang", new PropertyModel<Long>(RegisterDialog.this, "lang")));
			langField.setRequired(true).setLabel(Model.of(Application.getString(111)));
			add(tzDropDown.setRequired(true).setLabel(Model.of(Application.getString(1143))));
			add(countryField = new CountryDropDown("country", new PropertyModel<String>(RegisterDialog.this, "country")));
			countryField.setRequired(true).setLabel(Model.of(Application.getString(120)));
			add(new AjaxButton("submit") { // FAKE button so "submit-on-enter" works as expected
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					RegisterDialog.this.onSubmit(target);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					RegisterDialog.this.onError(target);
				}
			});
		}

		@Override
		protected void onValidate() {
			if (passwordField.getConvertedInput() == null
					|| !passwordField.getConvertedInput().equals(confirmPassword.getConvertedInput())) {
				error(Application.getString(232));
			}
			if (!getBean(UserDao.class).checkEmail(emailField.getConvertedInput(), User.Type.user, null, null)) {
				error(Application.getString(1000));
			}
			if (!getBean(UserDao.class).checkLogin(loginField.getConvertedInput(), User.Type.user, null, null)) {
				error(Application.getString(105));
			}
		}
	}
}
