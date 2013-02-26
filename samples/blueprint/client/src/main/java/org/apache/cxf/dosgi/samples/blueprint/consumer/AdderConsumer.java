package org.apache.cxf.dosgi.samples.blueprint.consumer;

import org.apache.cxf.dosgi.samples.blueprint.AdderService;

public class AdderConsumer {
    private AdderService adder;
    
    public void bindAdder(AdderService a) {
        adder = a;
    }
    
    public void unbindAdder(AdderService a) {
        adder = null;
    }
    
    public void start() {
        System.out.println("Declarative Service consumer component.");
        System.out.println("Using adder service: 1 + 1 = " + adder.add(1, 1));
    }
}
