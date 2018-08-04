package org.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConnection;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAll implements DSLinkConnection.Listener {

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnect(DSLinkConnection connection) {
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void onDisconnect(DSLinkConnection connection) {
    }

    @Test
    public void test() {
        MainNode root = new MainNode();
        DSLink link = new V1TestLink(root);
        link.getConnection().addListener(this);
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        waitForConnection();
        //Logger.getLogger("").setLevel(Level.ALL);
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
