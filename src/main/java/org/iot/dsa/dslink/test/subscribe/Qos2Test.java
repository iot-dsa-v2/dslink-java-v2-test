package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;

/**
 * @author Aaron Hansen
 */
public class Qos2Test extends QosTest {

    @Override
    protected void performUpdates(DSNode values, int changes, int interval) {
        DSInt v;
        int reconnect = (changes / 3);
        for (int i = 0; i < changes; i++) {
            v = DSInt.valueOf(i);
            for (DSInfo info : valueInfos) {
                values.put(info, v);
            }
            if (i == reconnect) {
                try {
                    //Lenient, let things settle before disconnecting.
                    Thread.sleep(1000);
                } catch (Exception x) {
                    warn(getPath(), x);
                }
                debug(debug() ? "Force disconnect " + getPath() : null);
                getConnection().disconnect();
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
