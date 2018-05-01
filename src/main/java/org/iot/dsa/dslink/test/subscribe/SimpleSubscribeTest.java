package org.iot.dsa.dslink.test.subscribe;

import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.requester.AbstractSubscribeHandler;
import org.iot.dsa.dslink.requester.ErrorType;
import org.iot.dsa.dslink.test.MainNode;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

public class SimpleSubscribeTest {
    
    private MainNode mainNode;
    
    public SimpleSubscribeTest(MainNode mainNode) {
        this.mainNode = mainNode;
    }
    
    public void runSimpleTest() throws InterruptedException {
        testSubscribe("downstream" + mainNode.subscribeChild.getNode().getPath() + "/" + SubscribeChild.COUNTER, 0, null);
    }
    
    public void testSubscribe(String path, int qos, DSElement expectedUpdate) throws InterruptedException {
        DSIRequester requester = mainNode.getLink().getConnection().getRequester();
                
        SimpleSubscribeHandler handler = new SimpleSubscribeHandler();
        requester.subscribe(path, qos, handler);
        synchronized (handler) {
            if (handler.update == null) {
                handler.wait(5000);
            }
        }
        
        if (handler.update == null) {
            throw new RuntimeException("Failed to recieve an update");
        } else if (expectedUpdate != null && !expectedUpdate.equals(handler.update)) {
            throw new RuntimeException("Expected update with value " + expectedUpdate.toString() + ", got " + handler.update.toString());
        }
        
        handler.getStream().closeStream();
    }
    
    private static class SimpleSubscribeHandler extends AbstractSubscribeHandler{
        DSElement update = null;
        
        @Override
        public synchronized void onError(ErrorType type, String msg) {
            notify();
        }
        
        @Override
        public synchronized void onClose() {
            notify();
        }
        
        @Override
        public synchronized void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status) {
            update = value;
            notify();
        }
    }

}
