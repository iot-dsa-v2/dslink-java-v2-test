package org.iot.dsa.dslink.test;

import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.dslink.test.subscribe.Subscriptions;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * Root hub of all tests and provides an action to run them all.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    public static final String SUBSCRIPTIONS = "Subscriptions";
    public static final String RUNALL = "Run-All-Tests";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo runAll = getInfo(RUNALL);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public ActionResult onInvoke(DSInfo action, ActionInvocation request) {
        if (action == runAll) {
            runAll();
            return null;
        }
        return super.onInvoke(action, request);
    }

    public void runAll() {
        Subscriptions s = (Subscriptions) get(SUBSCRIPTIONS);
        s.runAll();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(RUNALL, DSAction.DEFAULT);
        declareDefault(SUBSCRIPTIONS, new Subscriptions());
    }

}

