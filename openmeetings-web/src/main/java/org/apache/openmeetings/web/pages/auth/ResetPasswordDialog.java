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

import static org.apache.openmeetings.db.util.UserHelper.getMinPasswdLength;
import static org.apache.openmeetings.web.app.Application.getBean;
import static org.apache.wicket.validation.validator.StringValidator.minimumLength;

import java.util.Arrays;
import java.util.List;

import org.apache.openmeetings.db.dao.basic.ConfigurationDao;
import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.web.app.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import com.googlecode.wicket.jquery.core.JQueryBehavior;
import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.ui.widget.dialog.AbstractFormDialog;
import com.googlecode.wicket.jquery.ui.widget.dialog.DialogButton;
import com.googlecode.wicket.jquery.ui.widget.dialog.DialogButtons;
import com.googlecode.wicket.jquery.ui.widget.dialog.DialogIcon;
import com.googlecode.wicket.jquery.ui.widget.dialog.MessageDialog;
import com.googlecode.wicket.kendo.ui.panel.KendoFeedbackPanel;

public class ResetPasswordDialog extends AbstractFormDialog<String> {
	private static final long serialVersionUID = 1L;
	private DialogButton resetBtn = new DialogButton("reset", Application.getString(327));
	private Form<String> form;
	private final KendoFeedbackPanel feedback = new KendoFeedbackPanel("feedback", new Options("button", true));
	private PasswordTextField password;
	private final User user;
	final MessageDialog confirmReset;

	public ResetPasswordDialog(String id, final User user) {
		super(id, Application.getString(325));
		this.user = user;
		add(form = new Form<String>("form") {
			private static final long serialVersionUID = 1L;
			private TextField<String> login;
			private PasswordTextField confirmPassword;
			{
				add(feedback.setOutputMarkupId(true));
				add(login = new TextField<String>("login", Model.of(user.getLogin())));
				login.setOutputMarkupId(true);
				add(password = new PasswordTextField("password", new Model<String>()));
				password.setOutputMarkupId(true);
				password.setLabel(Model.of(Application.getString(328)));
				ConfigurationDao cfgDao = getBean(ConfigurationDao.class);
				password.setRequired(false).add(minimumLength(getMinPasswdLength(cfgDao)));
				add(confirmPassword = new PasswordTextField("confirmPassword", new Model<String>()));
				confirmPassword.setOutputMarkupId(true);
				confirmPassword.setLabel(Model.of(Application.getString(329)));
				confirmPassword.setRequired(true).add(minimumLength(getMinPasswdLength(cfgDao)));

				add(new AjaxButton("submit") { // FAKE button so "submit-on-enter" works as expected
					private static final long serialVersionUID = 1L;

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						ResetPasswordDialog.this.onSubmit(target);
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						ResetPasswordDialog.this.onError(target);
					}
				});
			}

			@Override
			protected void onValidate() {
				String pass = password.getConvertedInput();
				if (pass != null && !pass.isEmpty() && !pass.equals(confirmPassword.getConvertedInput())) {
					error(Application.getString(232));
				}
				super.onValidate();
			}

		});
		confirmReset = new MessageDialog("confirmReset", Application.getString(325), Application.getString(332), 
				DialogButtons.OK, DialogIcon.INFO) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onConfigure(JQueryBehavior behavior) {
				super.onConfigure(behavior);
				behavior.setOption("dialogClass", Options.asString("no-close"));
				behavior.setOption("closeOnEscape", false);
			}

			@Override
			public void onClose(IPartialPageRequestHandler handler, DialogButton button) {
				setResponsePage(Application.get().getSignInPageClass());
			}
		};
		add(confirmReset);
	}

	@Override
	public void onConfigure(JQueryBehavior behavior) {
		super.onConfigure(behavior);
		behavior.setOption("autoOpen", true);
		behavior.setOption("dialogClass", Options.asString("no-close"));
		behavior.setOption("closeOnEscape", false);
	}

	@Override
	protected List<DialogButton> getButtons() {
		return Arrays.asList(resetBtn);
	}

	@Override
	public DialogButton getSubmitButton() {
		return resetBtn;
	}

	@Override
	public Form<?> getForm() {
		return form;
	}

	@Override
	protected void onError(AjaxRequestTarget target) {
		target.add(feedback);
	}

	@Override
	protected void onSubmit(AjaxRequestTarget target) {
		try {
			getBean(UserDao.class).resetPassword(user, password.getConvertedInput());
		} catch (Exception e) {
			error(e.getMessage());
		}
	}

	@Override
	public void onClose(IPartialPageRequestHandler handler, DialogButton button) {
		if (resetBtn.equals(button)) {
			confirmReset.open(handler);
		} else {
			setResponsePage(Application.get().getSignInPageClass());
		}
	}
}
