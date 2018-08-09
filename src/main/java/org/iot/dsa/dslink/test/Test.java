package org.iot.dsa.dslink.test;

public interface Test {

    /**
     * Returning false skips the test.
     */
    public boolean isEnabled();

    /**
     * Return true if the test passes.
     */
    public boolean test();

}
