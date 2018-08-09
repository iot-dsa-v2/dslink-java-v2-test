package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.test.AbstractTest;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;

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
        debug("onConnect " + getPath());
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
        declareDefault(CHANGES, DSInt.valueOf(30));
        declareDefault(FAILURES, DSInt.valueOf(0));
        declareDefault(INTERVAL, DSInt.valueOf(1000));
        declareDefault(NUM_VALUES, DSInt.valueOf(5));
    }

    @Override
    protected boolean doTest() {
        int v = numValues.getElement().toInt();
        int c = changes.getElement().toInt();
        int i = interval.getElement().toInt();
        return doTest(v, c, i);
    }

    protected boolean doTest(int values, int changes, int interval) {
        boolean result = true;
        DSNode tmp = new DSNode();
        try {
            debug(debug() ? String.format(
                    "Values=%s, Changes=%s, Interval=%s, %s",values,changes,interval,getPath())
                          : null);
            put(NUM_VALUES, DSInt.valueOf(values));
            DSLinkConnection conn = getConnection();
            conn.addListener(this);
            //remove children from last test
            clear();
            //create the node that will have all the values
            put("tmp", tmp).setTransient(true);
            //create the values and subscribers
            String base = conn.getPathInBroker(tmp) + "/Value";
            valueInfos = new DSInfo[values];
            subscribers = new QosSubscriber[values];
            debug("Creating values and subscriptions");
            for (int i = 0; i < values; i++) {
                valueInfos[i] = tmp.put("Value" + i, DSInt.valueOf(-2));
                subscribers[i] = new QosSubscriber(base + i, changes, 2);
                add("sub" + i, subscribers[i]).setTransient(true);
                subscribers[i].start(conn);
            }
            //safely reset the values in case broker has old value
            for (int i = 0; i < values; i++) {
                valueInfos[i] = tmp.put("Value" + i, DSInt.valueOf(-1));
            }
            //give things a chance to stabilize
            try {
                Thread.sleep(1000);
            } catch (Exception x) {
                warn(getPath(), x);
            }
            debug("Waiting for initial subscription update for each value");
            int failures = 0;
            for (int i = 0; i < values; i++) {
                try {
                    subscribers[i].waitForInitialUpdate();
                } catch (Exception x) {
                    result = false;
                    failures++;
                    error(subscribers[i].getPath(), x);
                }
            }
            if (result) {
                debug("Changes per value: " + changes);
                //perform the value updates
                performUpdates(tmp, changes, interval);
                debug("Values updates complete, tallying results");
                for (QosSubscriber sub : subscribers) {
                    if (!sub.wasSuccess()) {
                        failures++;
                    }
                }
            }
            put(FAILURES, DSInt.valueOf(failures));
            if (failures > 0) {
                debug("Failures: " + failures);
                result = false;
            }
        } catch (Exception x) {
            result = false;
            error(getPath(), x);
        } finally {
            if (valueInfos != null) {
                for (int i = 0; i < values; i++) {
                    tmp.put(valueInfos[i], DSInt.valueOf(-1));
                }
            }
            if (subscribers != null) {
                for (QosSubscriber sub : subscribers) {
                    sub.close();
                }
            }
            valueInfos = null;
            subscribers = null;
        }
        return result;
    }

    protected abstract void performUpdates(DSNode values, int changes, int interval);

}
