package org.apache.cxf.dosgi.samples.ds.consumer;

import org.apache.cxf.dosgi.samples.ds.AdderService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This Activator simply registers a service tracker to indicate its interest in the
 * AdderService which causes the service to get registered by the Listener Hook.
 * It is a workaround for the problem that the current ListenerHook is incompatible
 * with the Equinox DS implementation which doesn't specify a filter when looking up 
 * a service. See also DOSGI-73.
 */
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
