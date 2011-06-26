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
package org.apache.cxf.dosgi.dsw.service;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.cxf.endpoint.Server;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;



/// *************************** FIXME: some old methods might be in here **** 
public class ExportRegistrationImpl implements ExportRegistration {

    private Logger LOG = Logger.getLogger(ExportRegistrationImpl.class.getName());
    
    private Server server;
    private boolean closed = false;
    private ServiceReference serviceReference = null;
    private EndpointDescription endpointDescription;
    private Throwable exception = null;

    private ExportRegistrationImpl parent = null;
    private int instanceCount = 1;

    private RemoteServiceAdminCore rsaCore;
    
    private ExportReference exportReference;
    
    private ServiceTracker serviceTracker;
    
    // provide a clone of the provided exp.Reg that is linked to this instance 
    public ExportRegistrationImpl(ExportRegistrationImpl exportRegistration) {

        parent = exportRegistration;
        serviceReference = parent.getExportedService();
        endpointDescription = parent.getEndpointDescription();
        exception = parent.getException();
        rsaCore = parent.getRsaCore();
        parent.instanceAdded();
    }

    private synchronized void instanceAdded() {
        ++instanceCount;
    }

    public ExportRegistrationImpl(ServiceReference sref, EndpointDescription endpoint, RemoteServiceAdminCore remoteServiceAdminCore) {
        serviceReference = sref;
        endpointDescription = endpoint;
        parent = this;
        rsaCore = remoteServiceAdminCore;
    }

    public synchronized void close() {
        if (closed)
            return;
        closed = true;

        rsaCore.removeExportRegistration(this);
        
        parent.instanceClosed();
    }

    private synchronized void instanceClosed() {
        --instanceCount;
        if (instanceCount <= 0) {
            // really close the ExReg
            // TODO close it and remove from management structure .... !

            LOG.fine("really closing ExportRegistartion now! ");
            
            
            
            if (server != null) {
                // FIXME: is this done like this ?
                server.stop();
            }
        }
    }

    private void closeAll() {
        if(parent!=this){
            parent.closeAll();
            return;
        }
        // FIXME: close all clients !!!! 
        close();
    }
    
    public EndpointDescription getEndpointDescription() {
        if (!closed)
            return endpointDescription;
        else
            return null;
    }

    public Throwable getException() {
        if (!closed)
            return exception;
        else
            return null;
    }

    protected ServiceReference getServiceReference() {
        return serviceReference;
    }
    
    public ServiceReference getExportedService() throws IllegalStateException {
        if (!closed)
            return serviceReference;
        else
            return null;
    }

    public void setEndpointdescription(EndpointDescription epd) {
        endpointDescription = epd;
    }

    @Override
    public String toString() {
        String r = "Endpoint Desctiption for ServiceReference " + serviceReference;
        r += "\n";

        r += "*** EndpointDescription: **** \n";
        if (endpointDescription == null) {
            r += "---> NULL <---- \n";
        } else {
            Set<Map.Entry<String,Object>> props = endpointDescription.getProperties().entrySet();
            for (Map.Entry entry : props) {
                r += entry.getKey() + "  => " + entry.getValue() + "\n";
            }
        }
        r += "\n";
        r += "*** Exception: " + exception + " **** \n";
        r += "*** isClosed : " + closed + " ****\n";
        r += "\n";

        return r;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public void setException(Throwable ex) {
        exception = ex;
    }

    public ExportReference getExportReference() {
        if(exportReference==null){
            exportReference = new ExportReferenceImpl(this);
        }
        return exportReference;
    }

    protected EndpointDescription getEndpointDescriptionAlways() {
        return endpointDescription;
    }

    /**
     * Start the service tracker that monitors the osgi service that 
     * is exported by this exportRegistration
     * */
    public void startServiceTracker(BundleContext bctx) {
        
        // only the parent should do this
        if(parent!=this){
            parent.startServiceTracker(bctx);
            return;
        }
        
        // do it only once
        if(serviceTracker!=null){
            return;
        }
        
        Filter f;
        final Long sid = (Long)serviceReference.getProperty(Constants.SERVICE_ID);
        try {
            f = bctx.createFilter("("+Constants.SERVICE_ID+"="+sid+")");
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
            LOG.warning("Service tracker could not be started. The service will not be automatically unexported.");
            return;
        }
        serviceTracker = new ServiceTracker(bctx, f, new ServiceTrackerCustomizer() {
            
            public void removedService(ServiceReference sr, Object s) {
                LOG.info("Service ["+sid+"] has ben unregistered: Removing service export");
                close();
            }
            
            public void modifiedService(ServiceReference sr, Object s) {
                // FIXME:
                LOG.warning("Service modifications after the service is exported are currently not supported. The export is not modified!");
            }
            
            public Object addingService(ServiceReference sr) {
                return sr;
            }
        });
        serviceTracker.open();
    }

    public void setRsaCore(RemoteServiceAdminCore rsaCore) {
        this.rsaCore = rsaCore;
    }

    public RemoteServiceAdminCore getRsaCore() {
        return rsaCore;
    }
    
}
