package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;

/**
 * This tests Quality of Service 1. It verifies that all value changes are received in order.
 *
 * The number of values is configurable.  For every value, there is a unique subscriber.  The
 * changes per value and the change interval are also configurable.
 *
 * @author Aaron Hansen
 */
public class Qos1Test extends QosTest {

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        put(NUM_VALUES, DSInt.valueOf(5));
        put(CHANGES, DSInt.valueOf(1000));
        put(INTERVAL, DSInt.valueOf(10));
    }

    @Override
    protected void performUpdates(DSNode values, int changes, int interval) {
        DSInt v;
        for (int i = 0; i < changes; i++) {
            v = DSInt.valueOf(i);
            for (DSInfo info : valueInfos) {
                values.put(info, v);
            }
            if (interval > 0) {
                try {
                    Thread.sleep(interval);
                } catch (Exception x) {
                    warn(getPath(), x);
                }
            }
        }

    }

}
