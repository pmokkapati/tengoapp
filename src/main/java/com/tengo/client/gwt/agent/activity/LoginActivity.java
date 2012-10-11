/*
 * @author prasadm80@gmail.com
 */
package com.tengo.client.gwt.agent.activity;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.Command;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.inject.Inject;

import com.tengo.client.gwt.agent.view.LoginView;
import com.tengo.client.gwt.agent.place.LoginPlace;
import com.tengo.client.gwt.agent.place.MainPlace;
import com.tengo.client.gwt.shared.Util;

/**
 * Agent login Activity class
 */
public class LoginActivity extends AbstractActivity 
        implements LoginView.Presenter {
    private PlaceController _placeController;
    private LoginView _view;


    @Inject
    public LoginActivity(PlaceController  placeController, LoginView view) {
        _placeController = placeController;
        _view = view;
    } 

    /**
     * Function to login. Verifies login and password and does a login
     */
    public boolean login(LoginView view) {
        String loginId = view.getLoginId();
        String password = view.getPassword();
        if ( Util.isEmpty(loginId) ) {
            view.setInvalidLogin("Login id cannot be empty");
            return false;
        }
        if ( Util.isEmpty(password) ) {
            view.setInvalidLogin("password cannot be empty");
            return false;
        }
        //TODO 
        // Make GWT rpc call
        Command c = new Command() {
            public void execute() {
                LoginActivity.this.goTo(new MainPlace());
            }
        };
        c.execute();
        return true;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        _view.setPresenter(this);
        containerWidget.setWidget(_view.asWidget());
    }

    @Override
    public void goTo(Place place) {
        _placeController.goTo(place);
    }
}
