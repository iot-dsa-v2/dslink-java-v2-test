package org.iot.dsa.dslink.test;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.dslink.test.subscribe.Subscriptions;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.time.DSDateTime;

/**
 * Root hub of all tests and provides an action to run them all.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode implements Test {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final String AUTORUN = "Auto Run";
    static final String RUN = "Run";
    static final String FAIL = "Fail";
    static final String PASS = "Pass";
    static final String LAST_DURATION = "Last Duration";
    static final String LAST_RESULT = "Last Result";
    static final String LAST_START = "Last Start";
    static final String RUNNING = "Running";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo autoRun = getInfo(AUTORUN);
    private DSInfo duration = getInfo(LAST_DURATION);
    private DSInfo fail = getInfo(FAIL);
    private DSInfo lastResult = getInfo(LAST_RESULT);
    private DSInfo lastStart = getInfo(LAST_START);
    private DSInfo pass = getInfo(PASS);
    private DSInfo runAction = getInfo(RUN);
    private DSInfo running = getInfo(RUNNING);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public ActionResult onInvoke(DSInfo action, ActionInvocation request) {
        if (action == runAction) {
            test();
            return null;
        }
        return super.onInvoke(action, request);
    }

    @Override
    public boolean test() {
        synchronized (this) {
            if (running.getElement().toBoolean()) {
                throw new IllegalStateException("Test already running");
            }
            put(running, DSBool.TRUE);
        }
        try {
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
            info(getPath() + " " + str);
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
        declareDefault(RUN, DSAction.DEFAULT);
        declareDefault(AUTORUN, DSBool.FALSE);
        declareDefault(FAIL, DSInt.valueOf(0)).setReadOnly(true).setTransient(true);
        declareDefault(PASS, DSInt.valueOf(0)).setReadOnly(true).setTransient(true);
        declareDefault(LAST_START, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_DURATION, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_RESULT, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(RUNNING, DSBool.FALSE).setReadOnly(true).setTransient(true);
        declareDefault("Subscriptions", new Subscriptions());
        declareDefault("Help", DSString.valueOf(
                "https://github.com/iot-dsa-v2/dslink-java-v2-test#dslink-java-v2-test"))
                .setReadOnly(true).setTransient(true);
    }

    protected boolean doTest() {
        int pass = 0;
        int fail = 0;
        DSInfo info = getFirstInfo(Test.class);
        Test test;
        boolean res;
        while (info != null) {
            test = (Test) info.getObject();
            try {
                res = test.test();
            } catch (Exception x) {
                error(getPath(), x);
                res = false;
            }
            if (test instanceof TestContainer) {
                TestContainer container = (TestContainer) test;
                pass += container.getPass();
                fail += container.getFail();
            } else {
                if (res) {
                    pass++;
                } else {
                    fail++;
                }
            }
            info = info.next(Test.class);
        }
        put(this.fail, DSInt.valueOf(fail));
        put(this.pass, DSInt.valueOf(pass));
        if (fail > 0) {
            info(getPath() + " failures: " + fail);
            info(getPath() + " successes: " + pass);
            return false;
        }
        return true;
    }

    @Override
    protected void onStable() {
        if (autoRun.getElement().toBoolean()) {
            DSRuntime.runDelayed(new Runnable() {
                public void run() {
                    test();
                }
            }, 2500);
        }
    }

}

