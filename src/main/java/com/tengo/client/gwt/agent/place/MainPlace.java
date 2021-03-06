/*
 * @author prasadm80@gmail.com
 */
package com.tengo.client.gwt.agent.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

/**
 * Main view place. Place to move to after agent logs in
 */
public class MainPlace extends Place {
    
    public static class Tokenizer implements PlaceTokenizer<MainPlace> {
        @Override
        public String getToken(MainPlace place) {
            return "main";
        }
        @Override
        public MainPlace getPlace(String token) {
            return new MainPlace();
        }
    }
}
