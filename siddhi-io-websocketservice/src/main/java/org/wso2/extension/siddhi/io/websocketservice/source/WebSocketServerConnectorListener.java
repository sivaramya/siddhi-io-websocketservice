/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import javax.websocket.Session;

/**
 * WebSocket test class for WebSocket Connector Listener.
 */
public class WebSocketServerConnectorListener implements WebSocketConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServerConnectorListener.class);

    private List<Session> sessionList = new LinkedList<>();
    private boolean isIdleTimeout = false;
    private SourceEventListener sourceEventListener = null;

    public WebSocketServerConnectorListener(SourceEventListener eventListener) {
        sourceEventListener = eventListener;
    }

    @Override
    public void onMessage(WebSocketInitMessage initMessage) {
        HandshakeFuture future = initMessage.handshake(null, true, 3000);
        future.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                sessionList.add(session);
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
            }
        });
    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        String receivedTextToClient = textMessage.getText();
        log.debug("text: " + receivedTextToClient);
        sourceEventListener.onEvent(receivedTextToClient, null);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        ByteBuffer receivedByteBufferToClient = binaryMessage.getByteBuffer();
        byte[] message = receivedByteBufferToClient.array();
        sourceEventListener.onEvent(message, null);
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        byte[] receivedByteBufferToClient = controlMessage.getByteArray();
        sourceEventListener.onEvent(receivedByteBufferToClient, null);
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
    }

    @Override
    public void onError(Throwable throwable) {
        handleError(throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        this.isIdleTimeout = true;
        try {
            Session session = controlMessage.getChannelSession();
            session.close();
        } catch (IOException e) {
            log.error("Error occurred while closing the connection: " + e.getMessage());
        }
    }

    private void handleError(Throwable throwable) {
        log.error(throwable.getMessage());
    }

    public boolean isIdleTimeout() {
        boolean temp = isIdleTimeout;
        isIdleTimeout = false;
        return temp;
    }

}
