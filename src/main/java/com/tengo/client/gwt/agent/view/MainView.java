/*
 * @author prasadm80@gmail.com
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

