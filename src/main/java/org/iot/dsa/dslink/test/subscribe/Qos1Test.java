package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.test.MainNode;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.time.DSTime;

public class Qos1Test extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final String CHANGES = "Changes-Per-Value";
    private static final String INTERVAL = "Change-Interval";
    private static final String FAILURES = "Failures";
    private static final String LAST_DURATION = "Last-Duration";
    private static final String LAST_RESULT = "Last-Result";
    private static final String LAST_START = "Last-Start";
    private static final String RUN = "Run";
    private static final String RUNNING = "Running";
    private static final String VALUES = "Values";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo run = getInfo(RUN);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public ActionResult onInvoke(DSInfo info, final ActionInvocation request) {
        if (info == run) {
            DSRuntime.run(new Runnable() {
                @Override
                public void run() {
                    test(request);
                }
            });
            return null;
        }
        return super.onInvoke(info, request);
    }

    /**
     * Uses the current values for subscriptions, values, changes and interval.
     *
     * @return True if successful.
     */
    public boolean test() {
        return test(getElement(VALUES).toInt(),
                    getElement(CHANGES).toInt(),
                    getElement(INTERVAL).toInt());
    }

    public boolean test(int values, int changes, int interval) {
        synchronized (this) {
            if (getElement(RUNNING).toBoolean()) {
                throw new IllegalStateException("Already running");
            }
            put(RUNNING, DSBool.TRUE);
        }
        boolean result = true;
        long time = System.currentTimeMillis();
        put(LAST_START, DSString.valueOf(DSTime.encode(time, false)));
        put(VALUES, DSInt.valueOf(values));
        try {
            //TODO Make sure we are connected
            MainNode main = (MainNode) getParent().getParent();
            DSLinkConnection conn = main.getLink().getConnection();
            //remove children from last test
            clear();
            //create the node that will have all the values
            DSNode tmp = new DSNode();
            put("tmp", tmp).setTransient(true);
            //create the values and subscribers
            String base = conn.getPathInBroker(tmp) + "/Value";
            DSInfo[] infos = new DSInfo[values];
            Qos1Subscriber[] subs = new Qos1Subscriber[values];
            for (int i = 0; i < values; i++) {
                infos[i] = tmp.put("Value" + i, DSInt.valueOf(-1));
                subs[i] = new Qos1Subscriber(base + i, changes);
                add("sub" + i, subs[i]).setTransient(true);
                subs[i].start(conn);
            }
            for (int i = 0; i < values; i++) {
                subs[i].waitForInitialUpdate();
            }
            //perform the value updates
            DSInt v;
            for (int i = 0; i < changes; i++) {
                v = DSInt.valueOf(i);
                for (DSInfo info : infos) {
                    tmp.put(info, v);
                }
                if (interval > 0) {
                    try {
                        Thread.sleep(interval);
                    } catch (Exception x) {
                        warn(getPath(), x);
                    }
                }
            }
            int failures = 0;
            for (Qos1Subscriber sub : subs) {
                if (!sub.wasSuccess()) {
                    failures++;
                }
            }
            put(FAILURES, DSInt.valueOf(failures));
            time = System.currentTimeMillis();
            put(LAST_DURATION, DSString.valueOf(time + "ms"));
            if (failures > 0) {
                put(LAST_RESULT, DSString.valueOf("Fail"));
                result = false;
            } else {
                put(LAST_RESULT, DSString.valueOf("Success"));
                System.out.println("Success");
            }
        } catch (RuntimeException x) {
            put(LAST_RESULT, DSString.valueOf("Fail"));
            error(getPath(), x);
            throw x;
        } catch (Exception x) {
            put(LAST_RESULT, DSString.valueOf("Fail"));
            error(getPath(), x);
            throw new RuntimeException(x);
        } finally {
            put(RUNNING, DSBool.FALSE);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        DSAction action = new DSAction();
        action.addDefaultParameter(VALUES, DSInt.valueOf(1), null);
        action.addDefaultParameter(CHANGES, DSInt.valueOf(10000), null);
        action.addDefaultParameter(INTERVAL, DSInt.valueOf(1), "Millis");
        declareDefault(RUN, action);

        declareDefault(CHANGES, DSInt.valueOf(1)).setReadOnly(true).setTransient(true);
        declareDefault(FAILURES, DSInt.valueOf(0)).setReadOnly(true).setTransient(true);
        declareDefault(INTERVAL, DSInt.valueOf(1)).setReadOnly(true).setTransient(true);
        declareDefault(LAST_START, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_DURATION, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_RESULT, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(RUNNING, DSBool.FALSE).setReadOnly(true).setTransient(true);
        declareDefault(VALUES, DSInt.valueOf(1)).setReadOnly(true).setTransient(true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Privates Methods
    ///////////////////////////////////////////////////////////////////////////

    private void test(ActionInvocation request) {
        DSMap params = request.getParameters();
        final int values = params.get(VALUES, 1);
        final int changes = params.get(CHANGES, 10000);
        final int interval = params.get(INTERVAL, 1);
        DSRuntime.run(new Runnable() {
            @Override
            public void run() {
                test(values, changes, interval);
            }
        });
    }

}
