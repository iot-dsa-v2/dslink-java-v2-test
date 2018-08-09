# dslink-java-v2-test

* Java - version 1.6 and up.
* [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)


## Overview

This link is for testing brokers.  It acts as both a requester and
responder to test protocol messaging with an interleaving broker.

It is organized as a tree of tests.  The leaf nodes are the tests
and everything above is a test container.  The entire tree can be
run or just a subset.


## Usage

### Configuration
Individual tests have unique properties.  All tests and test containers
can be enabled/disabled.  Properties are documented below in the node
guide.  Be sure to save the configuration database when finished.

### Auto Run
This is for automated testing and when a client is not available.  Configure
the test tree and set "Auto Run" to true on the main node.  Don't forget
to save the configuration database.

### Saving
Invoke the "Save" action on _downstreamLink_/sys/Backups

### Debugging
You can add logs to the logging service to individually configure
log levels.  The log service is located at _downstreamLink_/sys/Logging.

The most relevant logs are:
  -  test.main - This is logging from the test nodes.
  -  test.sys.connection.transport - Set this to TRACE or ALL to see actual
messaging.

## Node Guide

The nodes of this link are organized as follows:

- _MainNode_ - The root node of the link.
  - _TestContainer_ - Manages the running of children tests.  There can
  be multiple levels of containers
    - _Test_ - The leaves of the tree are unique tests, although all
    tests share some common traits.

### MainNode

This is the root node of the link.  It has a counter that is updated on a short interval,
only when the node is subscribed.  It also has a simple action to reset the counter.

_Actions_
- Run - Runs all enabled tests in the subtree.

_Values_
- Auto Run - Set to true to automatically run the entire tree a few
sections after the link starts (default is false).
- Fail - The number of leaf tests that failed in the last run.
- Last Duration - How long testing took the last time it was run.
- Last Result - Pass or Fail.
- Last Start - Timestamp of the last start.
- Pass - The number of leaf test the passed in the last run.
- Running - Whether or not the tests are current running.

### TestContainer

Beside the main node and the leaf tests, everything else is a container.
This is the root node of the link.  It has a counter that is updated on a short interval,
only when the node is subscribed.  It also has a simple action to reset the counter.

_Actions_
- Run - Runs all enabled tests in the subtree.

_Values_
- Enabled - If false, won't be run by parent containers. Invoking the
run action on the container itself will run the tests in the tree rooted
by this node.
- Fail - The number of leaf tests that failed in the last run.
- Last Duration - How long testing took the last time it was run.
- Last Result - Pass or Fail.
- Last Start - Timestamp of the last start.
- Pass - The number of leaf test the passed in the last run.
- Running - Whether or not the tests are current running.

### Test

All leaf tests share these common traits:

_Actions_
- Run - Runs the test (even if it is disabled).

_Values_
- Enabled - If false, won't be run by parent containers. Invoking the
run action on test test itself will ignore the enabled state.
- Last Duration - How the last test run took in millis.
- Last Result - Pass or Fail.
- Last Start - Timestamp of the last start.
- Running - Whether or not the tests are current running.

### Qos1

This tests Quality of Service 1.  It verifies that all value changes
are received in order.

The following values are unique to this test:

_Values_
- Change Interval - Millis in between each value update.
- Changes Per Value - How many increments per value.  If this is 100,
then each value node will increment from 0 to 99.
- Failures - The number of value nodes that had a failure.
- Num Values - The number of value nodes.  For each value node there
is a cooresponding subscriber node.  Each subscriber subscribes to
a single value node.

### Qos2

This tests Quality of Service 2.  This is similar to Qos1 except that
at 1/3 of of the way through the test, the link disconnects from the
broker then reconnects.

The following values are unique to this test:

_Values_
- Change Interval - Millis in between each value update.
- Changes Per Value - How many increments per value.  If this is 100,
then each value node will increment from 0 to 99.
- Failures - The number of value nodes that had a failure.
- Num Values - The number of value nodes.  For each value node there
is a cooresponding subscriber node.  Each subscriber subscribes to
a single value node.

## Acknowledgements

SDK-DSLINK-JAVA

This software contains unmodified binary redistributions of 
[sdk-dslink-java-v2](https://github.com/iot-dsa-v2/sdk-dslink-java-v2), which is licensed 
and available under the Apache License 2.0. An original copy of the license agreement can be found 
at https://github.com/iot-dsa-v2/sdk-dslink-java-v2/blob/master/LICENSE

## History

* Version 1.0.0
  - First Release

