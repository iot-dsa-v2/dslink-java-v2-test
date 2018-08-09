package org.iot.dsa.dslink.test;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAbstractAction;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.time.DSDateTime;

/**
 * All leaf tests should subclass this.  All they need to do is implement doTest().
 *
 * @author Aaron Hansen
 */
public abstract class AbstractTest extends DSNode implements Test {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final String ENABLED = "Enabled";
    static final String LAST_DURATION = "Last Duration";
    static final String LAST_RESULT = "Last Result";
    static final String LAST_START = "Last Start";
    static final String RUN = "Run";
    static final String RUNNING = "Running";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo duration = getInfo(LAST_DURATION);
    private DSInfo enabled = getInfo(ENABLED);
    private DSInfo lastResult = getInfo(LAST_RESULT);
    private DSInfo lastStart = getInfo(LAST_START);
    private DSInfo runAction = getInfo(RUN);
    private DSInfo running = getInfo(RUNNING);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isEnabled() {
        return enabled.getElement().toBoolean();
    }

    @Override
    public ActionResult onInvoke(DSInfo action, ActionInvocation request) {
        if (action == runAction) {
            runTest(request);
            return null;
        }
        return super.onInvoke(action, request);
    }

    @Override
    public boolean test() {
        synchronized (this) {
            if (running.getElement().toBoolean()) {
                error("Attempt to run an already running test: " + getPath());
                throw new IllegalStateException("Test already running");
            }
            put(running, DSBool.TRUE);
        }
        try {
            info("Test " + getPath());
            DSDateTime time = DSDateTime.currentTime();
            put(lastStart, time);
            boolean res = false;
            try {
                res = doTest();
            } catch (Exception x) {
                error(getPath(), x);
                res = false;
            }
            long dur = System.currentTimeMillis() - time.timeInMillis();
            String str = dur + "ms";
            put(duration, DSString.valueOf(str));
            debug(getPath() + " completed in " + str);
            str = res ? "Pass" : "Fail";
            put(lastResult, DSString.valueOf(str));
            info(str + " " + getPath());
            return res;
        } finally {
            put(running, DSBool.FALSE);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(RUN, getRunAction());
        declareDefault(ENABLED, DSBool.TRUE);
        declareDefault(LAST_START, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_DURATION, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_RESULT, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(RUNNING, DSBool.FALSE).setReadOnly(true).setTransient(true);
    }

    protected abstract boolean doTest();

    protected DSLinkConnection getConnection() {
        return getMain().getLink().getConnection();
    }

    protected MainNode getMain() {
        return (MainNode) getAncestor(MainNode.class);
    }

    /**
     * Override point, returns DSAction.DEFAULT.
     */
    protected DSAbstractAction getRunAction() {
        return DSAction.DEFAULT;
    }

    /**
     * Override point for getting parameters from the action if needed.
     */
    protected void runTest(ActionInvocation request) {
        DSRuntime.run(new Runnable() {
            @Override
            public void run() {
                test();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
