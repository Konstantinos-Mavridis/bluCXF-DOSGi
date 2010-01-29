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
package org.apache.cxf.dosgi.systests2.multi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.cxf.dosgi.systests2.common.AbstractTestExportService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

@RunWith( JUnit4TestRunner.class )
public class TestExportService extends AbstractTestExportService {
    @Inject
    BundleContext bundleContext;

    @Configuration
    public static Option[] configure() throws Exception {
        Map<Integer, String> bundles = new TreeMap<Integer, String>();
        int startLevel = MultiBundleTools.getDistroBundles(bundles, false);
        
        List<Option> opts = new ArrayList<Option>();

        // Run this test under Felix. 
        opts.add(CoreOptions.frameworks(CoreOptions.felix()));
        
        opts.add(CoreOptions.systemProperty("org.osgi.framework.startlevel.beginning").value("" + startLevel));
        opts.add(CoreOptions.mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").versionAsInProject());
        for(Map.Entry<Integer, String> entry : bundles.entrySet()) {
            opts.add(CoreOptions.bundle(entry.getValue()).startLevel(entry.getKey()));
        }
        opts.add(CoreOptions.mavenBundle().groupId("org.apache.cxf.dosgi.samples").artifactId("cxf-dosgi-ri-samples-greeter-interface").versionAsInProject());
        opts.add(CoreOptions.mavenBundle().groupId("org.apache.cxf.dosgi.samples").artifactId("cxf-dosgi-ri-samples-greeter-impl").versionAsInProject());
        opts.add(CoreOptions.mavenBundle().groupId("org.apache.cxf.dosgi.systests").artifactId("cxf-dosgi-ri-systests2-common").versionAsInProject());
        
        return CoreOptions.options(opts.toArray(new Option[opts.size()]));
    }

    
    @Test
    public void testAccessEndpoint() throws Exception {
        // call into base test. Inheriting the test doesn't properly report failures.
        baseTestAccessEndpoint();
    }
}
