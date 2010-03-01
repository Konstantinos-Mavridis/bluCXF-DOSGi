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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;

public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    
    public static String[] normalizeStringPlus(Object object) {

        if (object instanceof String) {
            String s = (String)object;
            String[] ret = new String[1];
            ret[0] = s;
            return ret;
        }

        if (object instanceof String[]) {
            return (String[])object;
        }
        if (object instanceof Collection) {
            Collection col = (Collection)object;
            ArrayList<String> ar = new ArrayList<String>(col.size());
            for (Object o : col) {
                if (o instanceof String) {
                    String s = (String)o;
                    ar.add(s);
                }else{
                    LOG.warning("stringPlus contained non string element in list ! Element was skipped");
                }
            }
            return ar.toArray(new String[ar.size()]);
        }

        return null;
    }
    
    
    public static String remoteServiceAdminEventTypeToString(int type){
        switch (type) {
        case RemoteServiceAdminEvent.EXPORT_ERROR:
            return "EXPORT_ERROR";
        case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
            return "EXPORT_REGISTRATION";
        case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
            return "EXPORT_UNREGISTRATION";
        case RemoteServiceAdminEvent.EXPORT_WARNING:
            return "EXPORT_WARNING";
        case RemoteServiceAdminEvent.IMPORT_ERROR:
            return "IMPORT_ERROR";
        case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
            return "IMPORT_REGISTRATION";
        case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
            return "IMPORT_UNREGISTRATION";
        case RemoteServiceAdminEvent.IMPORT_WARNING:
            return "IMPORT_WARNING";    
        default:
            return "UNKNOWN_EVENT";
        }
    }
    
    
    public static String[] getAllRequiredIntents(Map serviceProperties){
        // Get the intents that need to be supported by the RSA
        String[] requiredIntents = Utils.normalizeStringPlus(serviceProperties.get(RemoteConstants.SERVICE_EXPORTED_INTENTS));
        if(requiredIntents==null){
            requiredIntents = new String[0];
        }
        
        { // merge with extra intents;
            String[] requiredExtraIntents = Utils.normalizeStringPlus(serviceProperties.get(RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
            if(requiredExtraIntents!= null && requiredExtraIntents.length>0){
                         
                requiredIntents = mergeArrays(requiredIntents, requiredExtraIntents);
            }
        }
        
        return requiredIntents;
    }

    public static String[] getInetntsImplementedByTheService(Map serviceProperties){
        // Get the Intents that are implemented by the service 
        String[] serviceIntents = Utils.normalizeStringPlus(serviceProperties.get(RemoteConstants.SERVICE_INTENTS));
        
        return serviceIntents;
    }
 
    
    public static String[] mergeArrays(String[] a1,String[] a2){
        if(a1==null) return a2;
        if(a2==null) return a1;
        
        List<String> list = new ArrayList<String>(a1.length+a2.length);

        for (String s : a1) {
            list.add(s);  
        }
        
        for (String s : a2) {
            if(!list.contains(s))
                list.add(s);  
        }
        
        return list.toArray(new String[list.size()]);
    }
}
