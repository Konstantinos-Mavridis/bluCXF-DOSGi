package org.apache.cxf.dosgi.samples.blueprint.consumer;

import org.apache.cxf.dosgi.samples.blueprint.AdderService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {    
    private ServiceTracker tracker;

    public void start(BundleContext context) throws Exception {
        tracker = new ServiceTracker(context, AdderService.class.getName(), null);
        tracker.open();
    }

    public void stop(BundleContext context) throws Exception {
        tracker.close();
    }
}
