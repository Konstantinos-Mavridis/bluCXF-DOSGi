package org.apache.cxf.dosgi.samples.ds.impl;

import org.apache.cxf.dosgi.samples.ds.AdderService;

public class AdderServiceImpl implements AdderService {
    public int add(int a, int b) {
        int result = a + b;
        System.out.println("Adder service invoked: " + a + " + " + b + " = " + result);
        return result;
    }
}
