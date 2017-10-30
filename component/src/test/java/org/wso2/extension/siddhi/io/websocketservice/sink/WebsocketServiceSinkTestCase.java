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

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class WebsocketServiceSinkTestCase {
    private static final Logger log = Logger.getLogger(WebsocketServiceSinkTestCase.class);
    private AtomicInteger eventCount = new AtomicInteger(0);
    private List<String> receivedEventNameList;
    private int waitTime = 50;
    private int timeout = 30000;

    @BeforeMethod
    public void init() {
        eventCount.set(0);
    }

    @Test
    public void websocketSinkAndSourceTestCase() throws InterruptedException {
        log.info("---------------------------------------------------------------------------------------------");
        log.info("websocket sink and source test case");
        log.info("---------------------------------------------------------------------------------------------");
        receivedEventNameList = new ArrayList<>(3);
        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(
                        "@App:name('TestExecutionPlan') " +
                                "define stream FooStream1 (symbol string); " +
                                "@info(name = 'query1') " +
                                "@source(type='websocketservice', host='localhost', port='8080', " +
                                "@map(type='xml'))" +
                                "Define stream BarStream1 (symbol string);" +
                                "from FooStream1 select symbol insert into BarStream1;");
        siddhiAppRuntime.addCallback("BarStream1", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    log.info(event);
                    eventCount.incrementAndGet();
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        siddhiAppRuntime.start();
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(
                "@App:name('TestExecutionPlan') " +
                        "define stream FooStream1 (symbol string); " +
                        "@info(name = 'query1') " +
                        "@sink(type='websocketservice', host='localhost', port='8080', target ='wso2', " +
                        "@map(type='xml'))" +
                        "Define stream BarStream1 (symbol string);" +
                        "from FooStream1 select symbol insert into BarStream1;");
        InputHandler fooStream = executionPlanRuntime.getInputHandler("FooStream1");
        executionPlanRuntime.start();
        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"WSO2"}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"IBM"}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"WSO2"}));
        fooStream.send(arrayList.toArray(new Event[3]));
        List<String> expected = new ArrayList<>(2);
        expected.add("WSO2");
        expected.add("IBM");
        expected.add("WSO2");
        SiddhiTestHelper.waitForEvents(waitTime, 3, eventCount, timeout);
        AssertJUnit.assertEquals(expected, receivedEventNameList);
        AssertJUnit.assertEquals(3, eventCount.get());
        siddhiAppRuntime.shutdown();

    }

}
