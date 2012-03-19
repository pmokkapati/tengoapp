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

import com.google.gwt.place.shared.WithTokenizers;
import com.google.gwt.place.shared.PlaceHistoryMapper;

import com.tengo.client.gwt.agent.place.*;

/**
 * Place history mapper
 */
@WithTokenizers({LoginPlace.Tokenizer.class, MainPlace.Tokenizer.class})
public interface AgentPlaceHistoryMapper extends PlaceHistoryMapper {
}
