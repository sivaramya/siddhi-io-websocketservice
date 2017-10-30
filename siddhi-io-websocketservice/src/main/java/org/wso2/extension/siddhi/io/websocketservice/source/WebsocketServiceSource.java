/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.extension.siddhi.io.websocketservice.source;

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.Map;

/**
 * websocketservice source extension.
 */

@Extension(
        name = "websocketservice",
        namespace = "source",
        description = "description ",
        examples = @Example(description = "TBD", syntax = "TBD")
)

public class WebsocketServiceSource extends Source {
    private static final String HOST = "host";
    private static final String PORT = "port";
    private String host;
    private int port;
    private WebsocketServer websocketServer = null;
    private SourceEventListener sourceEventListener;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder, String[] strings,
                     ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        this.host = optionHolder.validateAndGetStaticValue(HOST);
        this.port = Integer.parseInt(optionHolder.validateAndGetStaticValue(PORT));
        this.sourceEventListener = sourceEventListener;
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{String.class};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        websocketServer = new WebsocketServer(host, port);
        try {
            websocketServer.setSourceEventListener(sourceEventListener);
            websocketServer.start();
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException("Error while starting the websocket server");
        }
    }

    @Override
    public void disconnect() {
        if (websocketServer != null) {
            websocketServer.stop();
        }
    }

    @Override public void destroy() {

    }

    @Override public void pause() {

    }

    @Override public void resume() {

    }

    @Override public Map<String, Object> currentState() {
        return null;
    }

    @Override public void restoreState(Map<String, Object> map) {

    }
}
