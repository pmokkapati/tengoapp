/*
 * 
 * Copyright 2012 by Tengo, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Tengo, Inc.
 *
 * @author psm
 */
package com.tengo.client.gwt.agent.view.impl;

import com.google.gwt.user.client.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.event.dom.client.ClickEvent;

import com.tengo.client.gwt.agent.view.LoginView;

/**
 * Agent Login implementation
 */
public class LoginViewImpl extends Composite implements LoginView {

    private static LoginViewImplUiBinder _uiBinder = 
            GWT.create(LoginViewImplUiBinder.class);

    interface LoginViewImplUiBinder extends UiBinder<Widget, LoginViewImpl> {}

    @UiField TextBox _loginId;
    @UiField PasswordTextBox _password;
    @UiField Button _loginButton;

    private Presenter _presenter;


    public LoginViewImpl() {
        initWidget(_uiBinder.createAndBindUi(this));
    }

    @UiHandler("_loginButton")
    void onClickLogin(ClickEvent e) {
        _presenter.login(this);
    }

    @Override
    public String getLoginId() {
        return _loginId.getValue();
    }
    @Override
    public String getPassword() {
        return _password.getValue();
    }

    @Override
    public void setInvalidLogin(String msg) {
        // TODO
    }

    @Override
    public void setPresenter(Presenter p) {
        _presenter = p;
    }
}
