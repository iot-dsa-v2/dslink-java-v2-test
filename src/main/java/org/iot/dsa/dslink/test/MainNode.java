package org.iot.dsa.dslink.test;

import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.dslink.test.subscribe.SimpleSubscribeTest;
import org.iot.dsa.dslink.test.subscribe.SubscribeChild;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.util.DSException;
/**
 * The root and only node of this link.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode {
    
    private static final String SUBCHILD = "Subscribe";
    private static final String RUN_TESTS = "Run_All_Tests";


    public DSInfo subscribeChild = getInfo(SUBCHILD);

    public MainNode() {
    }

    
    /**
     * Defines the permanent children of this node type, their existence is guaranteed in all
     * instances.  This is only ever called once per, type per process.
     */
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(RUN_TESTS, makeRunAllTestsAction());
        declareDefault(SUBCHILD, new SubscribeChild());
    }
    
    private DSAction makeRunAllTestsAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((MainNode) info.getParent()).runAllTests();
                return null;
            }
        };
        return act;
    }
    
    private void runAllTests() {
        try {
            new SimpleSubscribeTest(this).runSimpleTest();
            info("Simple Subscribe Test Success");
        } catch (Exception e) {
            info(e);
            DSException.throwRuntime(e);
        }
    }   

}
