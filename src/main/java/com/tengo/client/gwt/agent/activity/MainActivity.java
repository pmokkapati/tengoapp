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

import com.tengo.client.gwt.agent.view.MainView;
import com.tengo.client.gwt.agent.place.MainPlace;
import com.tengo.client.gwt.shared.Util;

/**
 *  Agent Main activity
 */
public class MainActivity extends AbstractActivity 
        implements MainView.Presenter {
    private PlaceController _placeController;
    private MainView _view;


    @Inject
    public MainActivity(PlaceController  placeController, MainView view) {
        _placeController = placeController;
        _view = view;
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
