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
package com.tengo.client.gwt.agent.gin;

import com.google.inject.Singleton;
import com.google.inject.Provides;
import com.google.inject.Provider;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.activity.shared.ActivityManager;

import com.tengo.client.gwt.agent.view.*;
import com.tengo.client.gwt.agent.view.impl.*;
import com.tengo.client.gwt.agent.activity.*;
import com.tengo.client.gwt.agent.AgentPlaceHistoryMapper;
import com.tengo.client.gwt.agent.AgentActivityMapper;

public class AgentGinModule extends AbstractGinModule {

    @Provides 
    @Singleton
    PlaceController getPlaceController(EventBus s) {
        return new PlaceController(s);
    }
    @Provides 
    @Singleton
    ActivityManager getActivityManager(ActivityMapper m, EventBus s) {
        return new ActivityManager(m, s);
    }
    @Provides 
    @Singleton
    PlaceHistoryHandler getPlaceHistoryHandler(PlaceHistoryMapper m) {
        return new PlaceHistoryHandler(m);
    }


    /**
     * Provide appropriate bindings for classes
     * Use singletons for views as well. Can use separate instances for 
     * Activities.
     */
    protected void configure() {
        bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
        bind(LoginView.class).to(LoginViewImpl.class).in(Singleton.class);
        bind(MainView.class).to(MainViewImpl.class).in(Singleton.class);
        bind(ActivityMapper.class).to(AgentActivityMapper.class)
            .in(Singleton.class);
        bind(PlaceHistoryMapper.class).to(AgentPlaceHistoryMapper.class)
            .in(Singleton.class);
    }
}
