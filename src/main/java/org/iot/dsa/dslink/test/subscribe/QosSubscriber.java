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
 *
 * @author Aaron Hansen
 */
public class QosSubscriber extends DSNode implements OutboundSubscribeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    private static final int TIMEOUT = 10000;

    private static final String COMPLETE = "Complete";
    private static final String DURATION = "Duration";
    private static final String OUT_OF_ORDER = "Out Of Order";
    private static final String SKIPPED = "Skipped";
    private static final String TOTAL_LOST = "Total Lost";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private long lastTs;
    private int lastValue = -1;
    private boolean open;
    private int outOfOrder;
    private String path;
    private int qos = 1;
    private int skipped;
    private long started;
    private OutboundStream stream;
    private int values;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public QosSubscriber() {
    }

    public QosSubscriber(String path, int values) {
        this.path = path;
        this.values = values;
    }

    public QosSubscriber(String path, int values, int qos) {
        this.path = path;
        this.values = values;
        this.qos = qos;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onClose() {
        trace("QosSubscriber.onClose");
        open = false;
    }

    @Override
    public void onError(ErrorType err, String msg) {
        trace("QosSubscriber.onError");
    }

    @Override
    public void onInit(String path, int qos, OutboundStream stream) {
        trace("QosSubscriber.onInit");
        open = true;
        this.stream = stream;
    }

    @Override
    public synchronized void onUpdate(DSDateTime timestamp, DSElement value, DSStatus status) {
        int val = value.toInt();
        trace(trace() ? (getPath() + " " + val) : null);
        lastTs = System.currentTimeMillis();
        if (val < 0) {
            lastValue = val;
            skipped = 0;
            outOfOrder = 0;
        } else {
            if (val > lastValue) {
                if (val != (lastValue + 1)) {
                    skipped += val - (lastValue + 1);
                    debug(debug() ? getPath() + " skip: " + val : null);
                }
                lastValue = val;
            } else {
                outOfOrder++;
                debug(debug() ? getPath() + " outOfOrder: " + val : null);
            }
        }
        notify();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(COMPLETE, DSBool.FALSE).setReadOnly(true);
        declareDefault(OUT_OF_ORDER, DSInt.valueOf(0)).setReadOnly(true);
        declareDefault(SKIPPED, DSInt.valueOf(0)).setReadOnly(true);
        declareDefault(TOTAL_LOST, DSInt.valueOf(0)).setReadOnly(true);
        declareDefault(DURATION, DSInt.valueOf(0)).setReadOnly(true);
    }

    @Override
    protected void onStopped() {
        close();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    void close() {
        trace("QosSubscriber.close");
        if (stream != null) {
            stream.closeStream();
        }
        stream = null;
    }

    void start(DSLinkConnection conn) {
        trace("QosSubscriber.start");
        started = System.currentTimeMillis();
        conn.getRequester().subscribe(path, qos, this);
    }

    synchronized void waitForInitialUpdate() {
        if (lastTs == 0) {
            try {
                wait(TIMEOUT);
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
            error("Fail (out of order values) " + getPath());
            return false;
        }
        if (skipped > 0) {
            error("Fail (skipped values) " + getPath());
            return false;
        }
        if (lastValue != target) {
            error("Fail target not met (" + lastValue + " != " + target + ") " + getPath());
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
