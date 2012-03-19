/*
 * 
 * Copyright 2011 by Tengo, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Tengo, Inc.
 *
 * @author psm
 */
package com.tengo.inject.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import com.tengo.inject.module.TropoModule;
import com.tengo.inject.module.DBModule;
import com.tengo.sqldb.inject.module.TransactionModule;

public class ServletContextListener extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
            new TropoModule(),
            new DBModule(),
            new TransactionModule(),
            new ServletModule() {
                @Override
                protected void configureServlets() {
                    serve("/mexico/sms").with(
                        com.tengo.server.sms.impl.CalixtaServer.class);
                    serve("/sms").with(
                        com.tengo.server.sms.impl.TropoServer.class);
                    serve("/ivr", "/ivr/*").with(
                        com.tengo.server.ivr.impl.TropoServer.class);
                }
        });
    }
}
