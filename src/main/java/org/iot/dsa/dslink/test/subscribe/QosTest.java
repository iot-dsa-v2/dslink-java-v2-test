package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.test.AbstractTest;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.util.DSException;

/**
 * @author Aaron Hansen
 */
public abstract class QosTest extends AbstractTest implements DSLinkConnection.Listener {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final String CHANGES = "Changes Per Value";
    static final String INTERVAL = "Change Interval";
    static final String NUM_VALUES = "Num Values";

    private static final String FAILURES = "Failures";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    DSInfo changes = getInfo(CHANGES);
    DSInfo interval = getInfo(INTERVAL);
    DSInfo numValues = getInfo(NUM_VALUES);
    QosSubscriber[] subscribers;
    DSInfo[] valueInfos;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnect(final DSLinkConnection dsLinkConnection) {
        debug("QosTest.onConnect");
        if (subscribers != null) { //only on reconnect
            DSRuntime.runDelayed(new Runnable() {
                @Override
                public void run() {
                    for (QosSubscriber sub : subscribers) {
                        sub.start(dsLinkConnection);
                    }
                }
            }, 1000);
        }
    }

    @Override
    public void onDisconnect(DSLinkConnection dsLinkConnection) {
        debug("QosTest.onDisconnect");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(CHANGES, DSInt.valueOf(30)).setReadOnly(true).setTransient(true);
        declareDefault(FAILURES, DSInt.valueOf(0)).setReadOnly(true).setTransient(true);
        declareDefault(INTERVAL, DSInt.valueOf(1000)).setReadOnly(true).setTransient(true);
        declareDefault(NUM_VALUES, DSInt.valueOf(10)).setReadOnly(true).setTransient(true);
    }

    protected boolean doTest(int values, int changes, int interval) {
        boolean result = true;
        try {
            put(NUM_VALUES, DSInt.valueOf(values));
            DSLinkConnection conn = getConnection();
            conn.addListener(this);
            //remove children from last test
            clear();
            //create the node that will have all the values
            DSNode tmp = new DSNode();
            put("tmp", tmp).setTransient(true);
            //create the values and subscribers
            String base = conn.getPathInBroker(tmp) + "/Value";
            valueInfos = new DSInfo[values];
            subscribers = new QosSubscriber[values];
            debug("Creating values and subscriptions");
            for (int i = 0; i < values; i++) {
                valueInfos[i] = tmp.put("Value" + i, DSInt.valueOf(-1));
                subscribers[i] = new QosSubscriber(base + i, changes, 2);
                add("sub" + i, subscribers[i]).setTransient(true);
                subscribers[i].start(conn);
            }
            debug("Waiting for initial subscription update for each value");
            for (int i = 0; i < values; i++) {
                subscribers[i].waitForInitialUpdate();
            }
            debug("Changing the values: " + changes);
            //perform the value updates
            performUpdates(tmp, changes, interval);
            debug("Values updates complete, tallying results");
            int failures = 0;
            for (QosSubscriber sub : subscribers) {
                if (!sub.wasSuccess()) {
                    failures++;
                }
            }
            //cleanup
            DSInt v = DSInt.valueOf(-1);
            for (DSInfo info : valueInfos) {
                tmp.put(info, v);
            }
            for (int i = 0; i < values; i++) {
                subscribers[i].close();
            }
            subscribers = null;
            put(FAILURES, DSInt.valueOf(failures));
            if (failures > 0) {
                debug("Failures: " + failures);
                result = false;
            }
        } catch (Exception x) {
            error(getPath(), x);
            DSException.throwRuntime(x);
        } finally {
            if (subscribers != null) {
                for (QosSubscriber sub : subscribers) {
                    sub.close();
                }
            }
        }
        return result;
    }

    protected abstract void performUpdates(DSNode values, int changes, int interval);

}
