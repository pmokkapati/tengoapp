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
package com.tengo.inject.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class TropoModule extends AbstractModule {
    private final String _ivrToken = "09056ee05d77034facc7d2d80c86e920937ff9d9bcc39d4971ac149636da7513490d46360da3089d8eb9f997";
    private final String _smsToken = "090572c857942c4cbda3ae325e1d4d3f7ce0c1bd4e993344680d44a14143160303e70be1af87e0781f77cebf";
    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("TropoIVRToken"))
            .to(_ivrToken);
        bindConstant().annotatedWith(Names.named("TropoSMSToken"))
            .to(_smsToken);
    }
}
