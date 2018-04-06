/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class WebPath {

    private static final String WEB_INF_DIR_NAME = "WEB-INF";
    private static String web_inf_path;

    public static String getWebInfPath() throws UnsupportedEncodingException {
        if (web_inf_path == null) {
            web_inf_path = URLDecoder.decode(WebPath.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF8");
            web_inf_path = web_inf_path.substring(0, web_inf_path.lastIndexOf(WEB_INF_DIR_NAME) + WEB_INF_DIR_NAME.length());
        }
        return web_inf_path;
    }
}
