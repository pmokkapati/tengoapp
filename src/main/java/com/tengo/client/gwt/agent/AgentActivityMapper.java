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
package com.tengo.client.gwt.agent;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;
import com.google.inject.Provider;

import com.tengo.client.gwt.agent.place.*;
import com.tengo.client.gwt.agent.activity.*;

public class AgentActivityMapper implements ActivityMapper {

    private Map<Class<? extends Place>, Provider<? extends Activity>> 
        _providerMap = new HashMap<Class<? extends Place>, 
        Provider<? extends Activity>>();

    @Inject
    public AgentActivityMapper(
            final Provider<LoginActivity> loginProvider,
            final Provider<MainActivity> mainProvider) {
        _providerMap.put(LoginPlace.class, loginProvider);
        _providerMap.put(MainPlace.class, mainProvider);
    }
    
    @Override
    public Activity getActivity(Place place) {
        Provider<? extends Activity> provider = _providerMap.get(
                place.getClass());
        return (provider != null ? provider.get() : null);
   }
}
