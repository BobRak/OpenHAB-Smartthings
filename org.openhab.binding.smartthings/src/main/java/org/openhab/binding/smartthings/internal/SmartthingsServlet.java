/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.internal;

import static org.openhab.binding.smartthings.SmartthingsBindingConstants.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.smartthings.handler.SmartthingsBridgeHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Receives all Http data from the Smartthings Hub
 *
 * @author Bob Raker
 *
 */
@SuppressWarnings("serial")
public class SmartthingsServlet extends HttpServlet {
    private static final String PATH = "/smartthings";
    private Logger logger = LoggerFactory.getLogger(SmartthingsServlet.class);
    private HttpService httpService;
    SmartthingsBridgeHandler bridgeHandler;
    private EventAdmin eventAdmin;
    private Gson gson;

    protected void activate(Map<String, Object> config) {
        // Get a Gson instance
        gson = new Gson();
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
            logger.info("Started Smartthings servlet at " + PATH);
        } catch (Exception e) {
            logger.error("Could not start Smartthings servlet service: {}", e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        try {
            httpService.unregister(PATH);
        } catch (IllegalArgumentException ignored) {
        }
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    protected void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        logger.debug("Smartthings servlet service() called with: {}: {} {}", req.getRemoteAddr(), req.getMethod(),
                path);

        // See what is in the path
        String[] pathParts = path.replace(PATH + "/", "").split("/");
        logger.debug("Smartthing servlet function requested: {} with Method: {}", pathParts[0], req.getMethod());

        if (pathParts.length != 1) {
            logger.error(
                    "Smartthing servlet recieved a path with zero or more than one parts. Only one part is allowed. path {}",
                    path);
            return;
        }

        if (pathParts[0].equals("state")) {
            // This is device state info returned from Smartthings
            Reader rdr = req.getReader();
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = rdr.read()) != -1) {
                sb.append((char) c);
            }
            rdr.close();
            logger.debug("Smartthing servlet processing \"state\" request. data: {}", sb.toString());
            publishEvent(STATE_EVENT_TOPIC, "data", sb.toString());
        } else if (pathParts[0].equals("discovery")) {
            // This is discovery data returned from Smartthings
            Reader rdr = req.getReader();
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = rdr.read()) != -1) {
                sb.append((char) c);
            }
            rdr.close();
            logger.debug("Smartthing servlet processing \"discovery\" request. data: {}", sb.toString());
            publishEvent(DISCOVERY_EVENT_TOPIC, "data", sb.toString());
        } else if (pathParts[0].equals("error")) {
            // This is an error message from smartthings
            Reader rdr = req.getReader();
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = rdr.read()) != -1) {
                sb.append((char) c);
            }
            rdr.close();
            logger.debug("Smartthing servlet processing \"error\" request. data: {}", sb.toString());
            Map<String, Object> map = new HashMap<String, Object>();
            map = gson.fromJson(sb.toString(), map.getClass());
            StringBuffer msg = new StringBuffer("Error message from Smartthings: ");
            msg.append(map.get("message"));
            logger.error("{}", msg.toString());
        } else {
            logger.error("Smartthing servlet recieved a path that is not supported {}", pathParts[0]);
        }

        // Return an http-204 - No response
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
    }

    private void publishEvent(String topic, String name, String data) {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(name, data);
        Event event = new Event(topic, props);
        eventAdmin.postEvent(event);
    }

}
