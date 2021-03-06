/*
 * @author prasadm80@gmail.com
 */
package com.tengo.client.gwt.agent.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

/**
 * Login view place.
 */
public class LoginPlace extends Place {
    
    public static class Tokenizer implements PlaceTokenizer<LoginPlace> {
        @Override
        public String getToken(LoginPlace place) {
            return "login";
        }
        @Override
        public LoginPlace getPlace(String token) {
            return new LoginPlace();
        }
    }
}
