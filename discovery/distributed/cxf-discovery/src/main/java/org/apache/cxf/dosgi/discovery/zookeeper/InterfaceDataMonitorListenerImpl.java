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
package org.apache.cxf.dosgi.discovery.zookeeper;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.dosgi.discovery.local.LocalDiscovery;
import org.apache.cxf.dosgi.discovery.local.LocalDiscoveryUtils;
import org.apache.zookeeper.ZooKeeper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

public class InterfaceDataMonitorListenerImpl implements DataMonitorListener {
    private static final Logger LOG = Logger.getLogger(InterfaceDataMonitorListenerImpl.class.getName());

    final ZooKeeper zookeeper;
    final String znode;
    final String interFace;
    final EndpointListenerTrackerCustomizer.Interest discoveredServiceTracker;
    final String scope;
    
    private final BundleContext bctx;
    
    // This map is *only* accessed in the change() method
    private Map<String, Map<String, Object>> nodes = new HashMap<String, Map<String,Object>>();
    
    public InterfaceDataMonitorListenerImpl(ZooKeeper zk, String intf, EndpointListenerTrackerCustomizer.Interest dst,String scope, BundleContext bc) {
        zookeeper = zk;
        znode = Util.getZooKeeperPath(intf);
        interFace = intf;
        discoveredServiceTracker = dst;
        bctx = bc;
        this.scope = scope;
    }
    
    public synchronized void change() {
        Map<String, Map<String, Object>> newNodes = new HashMap<String, Map<String,Object>>();
        Map<String, Map<String, Object>> prevNodes = nodes;
        try {
            LOG.info("Zookeeper callback on node: " + znode);
            List<String> children = zookeeper.getChildren(znode, false);
            
            for (String child : children) {
                byte[] data = zookeeper.getData(znode + '/' + child, false, null);
                LOG.info("Child: " + znode + "/" + child);
                
                List<Element> elements = LocalDiscoveryUtils.getElements(new ByteArrayInputStream(data));
                EndpointDescription epd = null;
                if(elements.size()>0)
                    epd = LocalDiscoveryUtils.getEndpointDescription(elements.get(0));
                else{
                    LOG.warning("No Discovery information found for node: "+znode + "/" + child);
                    continue;
                }
                    
                LOG.finest("Properties: "+epd.getProperties());
                
                newNodes.put(child, epd.getProperties());
                Map<String, Object> prevVal = prevNodes.remove(child);
                
                if (prevVal == null) {
                    // This guy is new

                    for (ServiceReference sref : discoveredServiceTracker.relatedServiceListeners) {
                        if (bctx.getService(sref) instanceof EndpointListener) {
                            EndpointListener epl = (EndpointListener)bctx.getService(sref);
                            
                            LOG.info("calling EndpointListener; "+epl+ "from bundle " + sref.getBundle().getSymbolicName() );
                            
                            epl.endpointAdded(epd, scope);
                        }
                    }
                } else if (!prevVal.equals(epd.getProperties())){
                    // There's been a modification
//                    ServiceEndpointDescriptionImpl sed = new ServiceEndpointDescriptionImpl(Collections.singletonList(interFace), m);
//                    DiscoveredServiceNotification dsn = new DiscoveredServiceNotificationImpl(Collections.emptyList(),
//                        Collections.singleton(interFace), DiscoveredServiceNotification.MODIFIED, sed);
//                    discoveredServiceTracker.serviceChanged(dsn);
                    
                   // TODO
                    
                }                
            }

            for (Map<String, Object> props : prevNodes.values()) {
                // whatever's left in prevNodes now has been removed from Discovery
                EndpointDescription epd = new EndpointDescription(props);
                
                for (ServiceReference sref : discoveredServiceTracker.relatedServiceListeners) {
                    if (bctx.getService(sref) instanceof EndpointListener) {
                        EndpointListener epl = (EndpointListener)bctx.getService(sref);
                        LOG.info("calling EndpointListener endpointRemoved: "+epl+ "from bundle " + sref.getBundle().getSymbolicName() );
                        epl.endpointRemoved(epd, scope);
                    }
                }
                
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Problem processing Zookeeper callback: "+e.getMessage(), e);
        } finally {
            nodes = newNodes;
        }
    }

    public void inform(ServiceReference sref) {
        LOG.fine("need to inform the service reference of maybe already existing endpoints");
        // TODO Auto-generated method stub
        
    }
}
