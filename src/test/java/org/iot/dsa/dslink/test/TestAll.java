package org.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.event.DSIEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSITopic;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAll {

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test() {
        MainNode root = new MainNode();
        DSLink link = new V1TestLink(root);
        link.getConnection().subscribe(DSLinkConnection.CONNECTED, null, new DSISubscriber() {
            @Override
            public void onEvent(DSNode node, DSInfo child, DSIEvent event) {
                synchronized (TestAll.this) {
                    TestAll.this.notify();
                }
            }

            @Override
            public void onUnsubscribed(DSITopic topic, DSNode node, DSInfo child) {
            }
        });
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        waitForConnection();
        Assert.assertTrue(link.getConnection().isConnected());
        Assert.assertTrue(root.test());
        link.shutdown();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private synchronized void waitForConnection() {
        try {
            wait(10000);
        } catch (InterruptedException x) {
            throw new RuntimeException(x);
        }
    }

}
