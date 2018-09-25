package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.dslink.test.TestContainer;

/**
 * The container for subscriptions related tests.
 */
public class Subscriptions extends TestContainer {

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Qos1", new Qos1Test());
        declareDefault("Qos2", new Qos2Test());
    }

}
