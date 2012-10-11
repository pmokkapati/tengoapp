/*
 * @author prasadm80@gmail.com
 */
package com.tengo.client.gwt.agent;

import com.google.gwt.user.client.Window;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.PlaceHistoryHandler;

import com.tengo.client.gwt.agent.gin.AgentGinjector;
import com.tengo.client.gwt.agent.place.LoginPlace;

public class Main implements EntryPoint {
    private final AgentGinjector injector = GWT.create(AgentGinjector.class);

    private SimplePanel appWidget = new SimplePanel();

    public void onModuleLoad() {
        // Start activity manager
        ActivityManager mgr = injector.getActivityManager();
        mgr.setDisplay(appWidget);

        // Start PlaceHistoryHander
        PlaceHistoryHandler h = injector.getPlaceHistoryHandler();
        h.register(injector.getPlaceController(), injector.getEventBus(),
                new LoginPlace());

        //RootPanel.get().add(injector.getLoginView());
        RootPanel.get().add(appWidget);
        h.handleCurrentHistory();
    }
}
