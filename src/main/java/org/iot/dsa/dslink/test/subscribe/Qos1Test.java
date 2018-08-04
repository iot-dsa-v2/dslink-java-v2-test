package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.DSAbstractAction;
import org.iot.dsa.node.action.DSAction;

/**
 * @author Aaron Hansen
 */
public class Qos1Test extends QosTest {

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        //change default values
        put(NUM_VALUES, DSInt.valueOf(10));
        put(CHANGES, DSInt.valueOf(100000));
        put(INTERVAL, DSInt.valueOf(0));
    }

    @Override
    protected boolean doTest() {
        int v = numValues.getElement().toInt();
        int c = changes.getElement().toInt();
        int i = interval.getElement().toInt();
        return doTest(v, c, i);
    }

    @Override
    protected DSAbstractAction getRunAction() {
        DSAction action = new DSAction();
        action.addDefaultParameter(NUM_VALUES, DSInt.valueOf(10), null);
        action.addDefaultParameter(CHANGES, DSInt.valueOf(100000), null);
        action.addDefaultParameter(INTERVAL, DSInt.valueOf(0), "Millis");
        return action;
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

    @Override
    protected void runTest(ActionInvocation request) {
        DSMap params = request.getParameters();
        put(numValues, DSInt.valueOf(params.get(NUM_VALUES, 10)));
        put(changes, DSInt.valueOf(params.get(CHANGES, 100000)));
        put(interval, DSInt.valueOf(params.get(INTERVAL, 0)));
        super.runTest(request);
    }

}
