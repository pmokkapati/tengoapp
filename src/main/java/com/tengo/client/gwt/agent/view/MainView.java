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
package com.tengo.client.gwt.agent.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Agent Login interface
 */
public interface MainView extends IsWidget {

    void setPresenter(Presenter p);

    public interface Presenter {
        void goTo(Place place);
    }
}

