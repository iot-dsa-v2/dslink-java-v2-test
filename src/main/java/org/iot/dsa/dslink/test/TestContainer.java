package org.iot.dsa.dslink.test;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.action.DSAbstractAction;
import org.iot.dsa.node.action.DSAction;

public class TestContainer extends AbstractTest implements Test {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final String FAIL = "Fail";
    static final String PASS = "Pass";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo fail = getInfo(FAIL);
    private DSInfo pass = getInfo(PASS);

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(FAIL, DSInt.valueOf(0)).setReadOnly(true).setTransient(true);
        declareDefault(PASS, DSInt.valueOf(0)).setReadOnly(true).setTransient(true);
    }

    @Override
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

    protected int getFail() {
        return fail.getElement().toInt();
    }

    protected int getPass() {
        return pass.getElement().toInt();
    }

    @Override
    protected DSAbstractAction getRunAction() {
        return DSAction.DEFAULT;
    }

}
