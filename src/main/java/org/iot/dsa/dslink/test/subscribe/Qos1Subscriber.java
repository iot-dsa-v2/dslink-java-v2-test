package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.requester.ErrorType;
import org.iot.dsa.dslink.requester.OutboundStream;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * A one time use subscriber.
 */
public class Qos1Subscriber extends DSNode implements OutboundSubscribeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    private static final int TIMEOUT = 10000;

    private static final String COMPLETE = "Complete";
    private static final String DURATION = "Duration";
    private static final String OUT_OF_ORDER = "Out_Of_Order";
    private static final String SKIPPED = "Skipped";
    private static final String TOTAL_LOST = "Total_Lost";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private long lastTs;
    private int lastValue = -1;
    private boolean open;
    private int outOfOrder;
    private String path;
    private int skipped;
    private long started;
    private OutboundStream stream;
    private int values;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public Qos1Subscriber() {
    }

    public Qos1Subscriber(String path, int values) {
        this.path = path;
        this.values = values;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onClose() {
        open = false;
    }

    @Override
    public void onError(ErrorType err, String msg) {
    }

    @Override
    public void onInit(String path, int qos, OutboundStream stream) {
        open = true;
        this.stream = stream;
    }

    @Override
    public synchronized void onUpdate(DSDateTime timestamp, DSElement value, DSStatus status) {
        int val = value.toInt();
        lastTs = System.currentTimeMillis();
        if (val >= 0) {
            if (val > lastValue) {
                if (val != (lastValue + 1)) {
                    skipped += val - (lastValue + 1);
                }
                lastValue = val;
            } else {
                outOfOrder++;
            }
        }
        notify();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    protected void declareDefaults() {
        declareDefault(COMPLETE, DSBool.FALSE).setReadOnly(true);
        declareDefault(OUT_OF_ORDER, DSInt.valueOf(0)).setReadOnly(true);
        declareDefault(SKIPPED, DSInt.valueOf(0)).setReadOnly(true);
        declareDefault(TOTAL_LOST, DSInt.valueOf(0)).setReadOnly(true);
        declareDefault(DURATION, DSInt.valueOf(0)).setReadOnly(true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    void start(DSLinkConnection conn) {
        started = System.currentTimeMillis();
        conn.getRequester().subscribe(path, 1, this);
    }

    synchronized void waitForInitialUpdate() {
        if (lastTs == 0) {
            try {
                wait(10000);
            } catch (Exception x) {
                error(getPath(), x);
            }
        }
        if (lastTs == 0) {
            throw new IllegalStateException("Initial update not received");
        }
    }

    /**
     * Waits for the test to complete or for there to be no updates for some period of time.
     */
    synchronized boolean wasSuccess() {
        int target = values - 1;
        while (open && (lastValue != target)) {
            try {
                wait(TIMEOUT);
            } catch (Exception x) {
                warn(getPath(), x);
            }
            if ((System.currentTimeMillis() - lastTs) > TIMEOUT) {
                break;
            }
        }
        put(COMPLETE, DSBool.TRUE);
        put(DURATION, DSInt.valueOf((int) (System.currentTimeMillis() - started)));
        put(OUT_OF_ORDER, DSInt.valueOf(outOfOrder));
        put(SKIPPED, DSInt.valueOf(skipped));
        put(TOTAL_LOST, DSInt.valueOf(skipped - outOfOrder));
        stream.closeStream();
        if (outOfOrder > 0) {
            return false;
        }
        if (skipped > 0) {
            return false;
        }
        return lastValue == target;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
