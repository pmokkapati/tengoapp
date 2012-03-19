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

import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;

import com.tengo.client.gwt.agent.view.*;
@GinModules({AgentGinModule.class})
public interface AgentGinjector extends Ginjector {
    EventBus getEventBus();
    PlaceController getPlaceController();
    ActivityManager getActivityManager();
    PlaceHistoryHandler getPlaceHistoryHandler();
    ActivityMapper getActivityMapper();
    LoginView getLoginView();
}
