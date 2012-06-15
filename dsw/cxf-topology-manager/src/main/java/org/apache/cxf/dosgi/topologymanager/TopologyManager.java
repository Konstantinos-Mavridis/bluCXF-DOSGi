/** 
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information 
 * regarding copyright ownership. The ASF licenses this file 
 * to you under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations 
 * under the License. 
 */
package org.apache.cxf.dosgi.topologymanager;

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * <li>This class keeps a list of currently imported and exported endpoints <li>It requests the import/export
 * from RemoteAdminServices
 */
public class TopologyManager {

    private final static Logger LOG = Logger.getLogger(TopologyManager.class.getName());

    private ExecutorService execService = new ThreadPoolExecutor(5, 10, 50, TimeUnit.SECONDS,
                                                                 new LinkedBlockingQueue<Runnable>());

    private final RemoteServiceAdminList remoteServiceAdminList;

    private ServiceListenerImpl serviceListerner;

    /**
     * Holds all services that are exported by this TopologyManager for each ServiceReference that should be
     * exported a map is maintained which contains information on the endpoints for each RemoteAdminService
     * 
     * <pre>
     * Bsp.:
     * ServiceToExort_1
     * ---&gt; RemoteAdminService_1 (CXF HTTP)
     * --------&gt; List&lt;EndpointDescription&gt; -&gt; {http://localhost:1234/greeter, http://localhost:8080/hello}
     * ---&gt; RemoteAdminService_2 (ActiveMQ JMS/OpenWire)
     * --------&gt; List&lt;EndpointDescription&gt; -&gt; {OpenWire://127.0.0.1:123/testQueue}
     * ServiceToExort_2
     * ---&gt; RemoteAdminService_1 (CXF HTTP)
     * --------&gt; List&lt;EndpointDescription&gt; -&gt; {empty} // not exported yet or not suitable
     * 
     * </pre>
     */
    private final Map<ServiceReference, 
                      Map<RemoteServiceAdmin, Collection<ExportRegistration>>> exportedServices = 
        new LinkedHashMap<ServiceReference, Map<RemoteServiceAdmin, Collection<ExportRegistration>>>();

    private BundleContext bctx;

    private ServiceTracker stEndpointListeners;

    public TopologyManager(BundleContext ctx, final RemoteServiceAdminList rsaList) {
        bctx = ctx;

        remoteServiceAdminList = rsaList;

        stEndpointListeners = new ServiceTracker(ctx, EndpointListener.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                LOG.fine("TopologyManager: new EndpointListener that wants to be informed about whats going on ... ");

                notify(reference);

                return super.addingService(reference);
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
                LOG.fine("TopologyManager: EndpointListener changed ... ");
                notify(reference);
                super.modifiedService(reference, service);
            }

            private void notify(ServiceReference reference) {
                synchronized (exportedServices) {
                    for (Map<RemoteServiceAdmin, Collection<ExportRegistration>> exports : exportedServices.values()) {
                        for (Collection<ExportRegistration> regs : exports.values()) {
                            if (regs != null)
                                notifyListenerOfAddingIfAppropriate(reference, regs);
                        }
                    }
                }
            }
        };

        serviceListerner = new ServiceListenerImpl(bctx, this);

    }

    protected void removeRemoteServiceAdmin(RemoteServiceAdmin rsa) {
        synchronized (exportedServices) {
            for (Map.Entry<ServiceReference, Map<RemoteServiceAdmin, Collection<ExportRegistration>>> exports : exportedServices
                .entrySet()) {
                if (exports.getValue().containsKey(rsa)) {
                    // service was handled by this RemoteServiceAdmin
                    Collection<ExportRegistration> endpoints = exports.getValue().get(rsa);
                    // TODO for each notify discovery......

                    try {
                        ServiceReference[] refs = Utils.getEndpointListeners(bctx);
                        if (refs != null) {
                            for (ServiceReference sref : refs) {
                                notifyListenersOfRemovalIfAppropriate(sref, endpoints);
                            }
                        }
                    } catch (InvalidSyntaxException e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                    }

                    // remove all management information for the RemoteServiceAdmin
                    exports.getValue().remove(rsa);
                }
            }
        }
    }

    protected void triggerExportImportForRemoteServiceAdmin(RemoteServiceAdmin rsa) {
        LOG.finer("TopologyManager: triggerExportImportForRemoteSericeAdmin()");

        synchronized (exportedServices) {
            for (Map.Entry<ServiceReference, Map<RemoteServiceAdmin, Collection<ExportRegistration>>> exports : exportedServices
                .entrySet()) {
                if (exports.getValue().containsKey(rsa)) {
                    // already handled....
                    LOG.fine("TopologyManager: service from bundle "
                             + exports.getKey().getBundle().getSymbolicName()
                             + "is already handled by this RSA");
                } else {
                    // trigger export of this service....
                    LOG.fine("TopologyManager: service from bundle "
                             + exports.getKey().getBundle().getSymbolicName()
                             + " is to be exported by this RSA");
                    triggerExport(exports.getKey());
                }

            }
        }

    }

    public void start() {
        stEndpointListeners.open();
        serviceListerner.start();

        try {
            checkExistingServices();
        } catch (InvalidSyntaxException e) {
            LOG.log(Level.FINER, "Failed to check existing services.", e);
        }
    }

    public void stop() {
        execService.shutdown();
        stEndpointListeners.close();
        serviceListerner.stop();
    }

    void removeService(ServiceReference sref) {
        synchronized (exportedServices) {
            if (exportedServices.containsKey(sref)) {
                Map<RemoteServiceAdmin, Collection<ExportRegistration>> rsas = exportedServices.get(sref);
                for (Map.Entry<RemoteServiceAdmin, Collection<ExportRegistration>> entry : rsas.entrySet()) {
                    if (entry.getValue() != null) {
                    	Collection<ExportRegistration> registrations = entry.getValue();
                        notifyListenersOfRemovalIfAppropriate(registrations);
                        for (ExportRegistration exReg : registrations) {
                            if (exReg != null) {
                                 exReg.close();
                            }
                        }
                    }
                }

                exportedServices.remove(sref);
            }
        }
    }

    private void notifyListenersOfRemovalIfAppropriate(Collection<ExportRegistration> registrations) {
    	for (ServiceReference endpointReference : stEndpointListeners.getServiceReferences()) {
    	    notifyListenersOfRemovalIfAppropriate(endpointReference, registrations);
    	}
    }
    
    protected void exportService(ServiceReference sref) {

        // add to local list of services that should/are be exported
        synchronized (exportedServices) {
            LOG.info("TopologyManager: adding service to exportedServices list to export it --- from bundle:  "
                      + sref.getBundle().getSymbolicName());
            exportedServices.put(sref,
                                 new LinkedHashMap<RemoteServiceAdmin, Collection<ExportRegistration>>());
        }

        // trigger the export
        triggerExport(sref);

    }

    private void triggerExport(final ServiceReference sref) {
        execService.execute(new Runnable() {
            @SuppressWarnings("unchecked")
            public void run() {
                LOG.finer("TopologyManager: exporting service ...");

                Map<RemoteServiceAdmin, Collection<ExportRegistration>> exports = null;

                synchronized (exportedServices) {
                    exports = Collections.synchronizedMap(exportedServices.get(sref));
                }
                // FIXME: Not thread safe...?
                if (exports != null) {
                    if(remoteServiceAdminList == null || remoteServiceAdminList.size() == 0) {
                        LOG.log(Level.SEVERE, "No RemoteServiceAdmin available! Unable to export service from bundle {0}, interfaces: {1}",
                                new Object[]{sref.getBundle().getSymbolicName(), sref.getProperty(org.osgi.framework.Constants.OBJECTCLASS)});
                    }

                    synchronized (remoteServiceAdminList) {
                        for (final RemoteServiceAdmin remoteServiceAdmin : remoteServiceAdminList) {
                            LOG
                                .info("TopologyManager: handling remoteServiceAdmin "
                                      + remoteServiceAdmin);

                            if (exports.containsKey(remoteServiceAdmin)) {
                                // already handled by this remoteServiceAdmin
                                LOG.fine("TopologyManager: already handled by this remoteServiceAdmin -> skipping");
                            } else {
                                // TODO: additional parameter Map ?
                                LOG.fine("TopologyManager: exporting ...");
                                Collection<ExportRegistration> endpoints = remoteServiceAdmin
                                    .exportService(sref, null);
                                if (endpoints == null) {
                                    // TODO export failed -> What should be done here?
                                    LOG.severe("TopologyManager: export failed");
                                    exports.put(remoteServiceAdmin, null);
                                } else {
                                    LOG.info("TopologyManager: export sucessful Endpoints:" + endpoints);
                                    // enqueue in local list of endpoints
                                    exports.put(remoteServiceAdmin, endpoints);

                                    // publish to endpoint listeners
                                    nofifyListeners(endpoints);
                                }
                            }
                        }
                    }
                }
            }

        });
    }

    protected void nofifyListeners(Collection<ExportRegistration> exportRegistrations) {
        try {
            // Find all EndpointListeners; They must have the Scope property otherwise they have to be ignored
            ServiceReference[] refs = Utils.getEndpointListeners(bctx);

            if (refs != null) {
                for (ServiceReference sref : refs) {
                    notifyListenerOfAddingIfAppropriate(sref, exportRegistrations);
                }
            }

        } catch (InvalidSyntaxException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    /**
     * Notifies the listener if he is interested in the provided registrations
     * 
     * @param sref The ServiceReference for an EndpointListener
     * @param exportRegistrations the registrations, the listener should be informed about
     */
    protected void notifyListenerOfAddingIfAppropriate(ServiceReference sref,
                                                       Collection<ExportRegistration> exportRegistrations) {

        // if (sref.getBundle().equals(bctx.getBundle())) {
        // LOG
        // .info("TopologyManager: notifyListenerOfAddingIfAppropriate() called for own listener -> skipping ");
        // return;
        // }

        EndpointListener epl = (EndpointListener)bctx.getService(sref);

        LOG.finer("TopologyManager: notifyListenerOfAddingIfAppropriate() ");

        try {

            List<Filter> filters = Utils.normalizeScope(sref, bctx);

            for (ExportRegistration exReg : exportRegistrations) {

                // FIXME!!!!!!!!!!!!! There needs to be a better way ?!?!?!
                Map props = exReg.getExportReference().getExportedEndpoint().getProperties();
                Dictionary d = new Hashtable(props);

                if (LOG.isLoggable(Level.FINE)) {
                    for (Filter filter : filters) {
                        LOG.fine("Matching: " + filter + "  against " + d);
                    }
                }

                for (Filter filter : filters) {
                    if (filter.match(d)) {
                        LOG.fine("Listener mached one of the Endpoints !!!!: " + epl);

                        epl.endpointAdded(exReg.getExportReference().getExportedEndpoint(), filter
                                .toString());
                    }
                }
            }

        } catch (InvalidSyntaxException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    protected void notifyListenersOfRemovalIfAppropriate(ServiceReference sref,
                                                         Collection<ExportRegistration> exportRegistrations) {

        EndpointListener epl = (EndpointListener)bctx.getService(sref);

        LOG.finer("TopologyManager: notifyListenerOfREMOVALIfAppropriate() ");

        List<Filter> filters;
        try {
            filters = Utils.normalizeScope(sref, bctx);

            for (ExportRegistration exReg : exportRegistrations) {

                // FIXME!!!!!!!!!!!!! There needs to be a better way ?!?!?!
            	ExportReference ref = exReg.getExportReference(); 
            	EndpointDescription endpoint = ref.getExportedEndpoint(); 
                Map props = endpoint.getProperties();
                Dictionary d = new Hashtable(props);

                if (LOG.isLoggable(Level.FINE)) {
                    for (Filter filter : filters) {
                        LOG.fine("Matching: " + filter + "  against " + d);
                    }
                }

                for (Filter filter : filters) {
                    if (filter.match(d)) {
                        LOG.fine("Listener matched one of the Endpoints !!!! --> calling removed() ...");

                        epl.endpointRemoved(exReg.getExportReference().getExportedEndpoint(), filter
                            .toString());
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    private void checkExistingServices() throws InvalidSyntaxException {
        ServiceReference[] references = bctx
            .getServiceReferences(null, "(" + RemoteConstants.SERVICE_EXPORTED_INTERFACES + "=*)");
        // + "("+org.apache.cxf.dosgi.dsw.Constants.EXPORTED_INTERFACES_OLD + "=*))");

        if (references != null) {
            for (ServiceReference sref : references) {
                exportService(sref);
            }
        }
    }

    public void removeExportRegistration(ExportRegistration exportRegistration) {
        ServiceReference sref = exportRegistration.getExportReference().getExportedService();
        if (sref != null) {
            synchronized (exportedServices) {

                Map<RemoteServiceAdmin, Collection<ExportRegistration>> ex = exportedServices.get(sref);
                if (ex != null) {
                    EndpointDescription ep = exportRegistration.getExportReference().getExportedEndpoint();
                    for (Map.Entry<RemoteServiceAdmin, Collection<ExportRegistration>> export : ex.entrySet()) {
                        export.getValue().contains(exportRegistration);
                    }
                }
            }
        } else {
            // the manager will be notified by its own service listener about this case
        }
    }

    /**
     * This method is called once a RemoteServiceAdminEvent for an removed export reference is received.
     * However the current implementation has no special support for multiple topology managers, therefore this method
     * does nothing for the moment.
     */
    public void removeExportReference(ExportReference anyObject) {
        // TODO Auto-generated method stub
        // LOG.severe("NOT implemented !!!");
    }

}
