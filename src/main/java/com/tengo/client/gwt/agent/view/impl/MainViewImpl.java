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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.event.dom.client.ClickEvent;

import com.tengo.client.gwt.agent.view.MainView;

/**
 * Agent Login implementation
 */
public class MainViewImpl extends Composite implements MainView {

    private static MainViewImplUiBinder _uiBinder = 
            GWT.create(MainViewImplUiBinder.class);

    interface MainViewImplUiBinder extends UiBinder<Widget, MainViewImpl> {}

    @UiField Anchor _depositButton;
    @UiField Anchor _withdrawButton;
    @UiField Anchor _newAccountButton;

    private Presenter _presenter;


    public MainViewImpl() {
        initWidget(_uiBinder.createAndBindUi(this));
    }

    @UiHandler("_depositButton")
    void onClickDeposit(ClickEvent e) {
        // _presenter.goTo(DepositPlace);
    }
    @UiHandler("_withdrawButton")
    void onClickWithdraw(ClickEvent e) {
        // _presenter.goTo(DepositPlace);
    }
    @UiHandler("_newAccountButton")
    void onClickNewAccount(ClickEvent e) {
        // _presenter.goTo(DepositPlace);
    }

    @Override
    public void setPresenter(Presenter p) {
        _presenter = p;
    }
}
