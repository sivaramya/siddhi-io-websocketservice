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

import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

/**
 * {@code TyrusWebsocketServer } Handle the websocket server.
 */

public class WebsocketServer {

    private final String host;
    private final int webSocketPort;
    private static final String webSocketContext = "/websockets";
    private ServerConnector serverConnector = null;
    private SourceEventListener sourceEventListener = null;

    public WebsocketServer(String host, int port) {
        this.host = host;
        this.webSocketPort = port;
    }

    public void setSourceEventListener(SourceEventListener eventListener) {
        sourceEventListener = eventListener;
    }

    public void start() throws InterruptedException {
        HttpWsConnectorFactoryImpl httpConnectorFactory = new HttpWsConnectorFactoryImpl();
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost(host);
        listenerConfiguration.setPort(webSocketPort);
        serverConnector = httpConnectorFactory.createServerConnector(ServerBootstrapConfiguration.getInstance(),
                                                                     listenerConfiguration);
        ServerConnectorFuture connectorFuture = serverConnector.start();
        connectorFuture.setWSConnectorListener(new WebSocketServerConnectorListener(sourceEventListener));
        connectorFuture.sync();
    }

    public void stop() {
        serverConnector.stop();
    }
}
