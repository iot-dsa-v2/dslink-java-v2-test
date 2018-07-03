package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.test.MainNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

public class Subscriptions extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    public static final String QOS1 = "QOS1";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo runAll = getInfo(MainNode.RUNALL);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public ActionResult onInvoke(final DSInfo info, final ActionInvocation request) {
        if (info == runAll) {
            DSRuntime.run(new Runnable() {
                @Override
                public void run() {
                    runAll();
                }
            });
            return null;
        }
        return super.onInvoke(info, request);
    }

    public void runAll() {
        Qos1Test node = (Qos1Test) get(QOS1);
        node.test();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(QOS1, new Qos1Test());
        declareDefault(MainNode.RUNALL, DSAction.DEFAULT);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Privates Methods
    ///////////////////////////////////////////////////////////////////////////

}
