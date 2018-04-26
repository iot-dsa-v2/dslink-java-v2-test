package org.iot.dsa.dslink.test;

import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.dslink.DSLinkConnection.Listener;
import org.iot.dsa.util.DSException;
/**
 * The root and only node of this link.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode {


    private static DSIRequester requester;
    private static final Object requesterLock = new Object();

    public MainNode() {
    }

    
    /**
     * Defines the permanent children of this node type, their existence is guaranteed in all
     * instances.  This is only ever called once per, type per process.
     */
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
    }

    
    @Override
    protected void onStarted() {
        getLink().getConnection().addListener(new Listener() {
            @Override
            public void onConnect(DSLinkConnection connection) {
                MainNode.setRequester(getLink().getConnection().getRequester());
            }

            @Override
            public void onDisconnect(DSLinkConnection connection) {
            }
        });
    }

    public static DSIRequester getRequester() {
        synchronized (requesterLock) {
            while (requester == null) {
                try {
                    requesterLock.wait();
                } catch (InterruptedException e) {
                    DSException.throwRuntime(e);
                }
            }
            return requester;
        }
    }

    public static void setRequester(DSIRequester requester) {
        synchronized (requesterLock) {
            MainNode.requester = requester;
            requesterLock.notifyAll();
        }
    }
}
