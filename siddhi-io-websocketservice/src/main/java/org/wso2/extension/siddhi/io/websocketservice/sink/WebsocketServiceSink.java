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

package org.wso2.extension.siddhi.io.websocketservice.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.util.Map;
import javax.net.ssl.SSLException;

/**
 * websocketservice sink extension.
 */

@Extension(
        name = "websocketservice",
        namespace = "sink",
        description = "description ",
        examples = @Example(description = "TBD", syntax = "TBD")
)
public class WebsocketServiceSink extends Sink {
    private static final Logger log = LoggerFactory.getLogger(WebsocketServiceSink.class);
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String TARGET = "target";
    private String host;
    private int port;
    private String target;
    private StreamDefinition streamDefinition;
    private WebSocketClient webSocketClient = null;

    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{String.class};
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[0];
    }

    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        this.host = optionHolder.validateAndGetStaticValue(HOST);
        this.port = Integer.parseInt(optionHolder.validateAndGetStaticValue(PORT));
        this.target = optionHolder.validateAndGetStaticValue(TARGET);
        this.streamDefinition = streamDefinition;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        String url = System.getProperty("url", String.format("ws://%s:%d/%s",
                                                             host, port, target));
        webSocketClient = new WebSocketClient(url);
        try {
            webSocketClient.handhshake();
        } catch (InterruptedException e) {
            throw new SiddhiAppRuntimeException("InterruptedException occurred while connecting with websocketClient "
                                                        + "in " + streamDefinition);
        } catch (URISyntaxException e) {
            throw new SiddhiAppRuntimeException("URISyntaxException occurred while connecting with websocketClient in"
                                                        + streamDefinition);
        } catch (SSLException e) {
            throw new SiddhiAppRuntimeException("SSLException occurred while connecting with websocketClient in " +
                                                        streamDefinition);
        } catch (ProtocolException e) {
            throw new SiddhiAppRuntimeException("ProtocolException occurred while connecting with websocketClient in "
                                                        + streamDefinition);
        }
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
            webSocketClient.sendText((String) payload);
    }

    @Override
    public void disconnect() {
        if (webSocketClient != null) {
            try {
                webSocketClient.shutDown();
            } catch (InterruptedException e) {
                log.debug("Error while disconnecting the websocket client");
            }
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }
}
