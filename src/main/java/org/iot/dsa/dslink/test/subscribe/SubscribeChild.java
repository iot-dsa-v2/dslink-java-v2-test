package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;

public class SubscribeChild extends DSNode implements Runnable {
    
    public static final String COUNTER = "Counter";
    
    public final DSInfo counter = getInfo(COUNTER);
    
    private DSRuntime.Timer timer;
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(COUNTER, DSInt.valueOf(0)).setTransient(true).setReadOnly(true);
    }
    
    /**
     * Starts the timer.
     */
    @Override
    protected void onSubscribed() {
        // Use DSRuntime for timers and its thread pool.
        timer = DSRuntime.run(this, System.currentTimeMillis() + 1000l, 100l);
    }

    /**
     * Cancels an active timer if there is one.
     */
    @Override
    protected void onStopped() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Cancels the timer.
     */
    @Override
    protected void onUnsubscribed() {
        timer.cancel();
        timer = null;
    }

    /**
     * Called by the timer, increments the counter.
     */
    @Override
    public void run() {
        synchronized (counter) {
            DSInt value = (DSInt) counter.getValue();
            put(counter, DSInt.valueOf(value.toInt() + 1));
        }
    }

}
