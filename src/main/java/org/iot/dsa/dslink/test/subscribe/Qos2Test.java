package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;

/**
 * @author Aaron Hansen
 */
public class Qos2Test extends QosTest {

    @Override
    protected boolean doTest() {
        int v = numValues.getElement().toInt();
        int c = changes.getElement().toInt();
        int i = interval.getElement().toInt();
        return doTest(v, c, i);
    }

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
