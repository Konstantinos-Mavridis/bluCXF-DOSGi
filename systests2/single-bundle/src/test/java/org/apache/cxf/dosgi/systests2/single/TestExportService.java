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
package org.apache.cxf.dosgi.systests2.single;

import org.apache.cxf.dosgi.systests2.common.AbstractTestExportService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith( JUnit4TestRunner.class )
public class TestExportService extends AbstractTestExportService {
    @Configuration
    public static Option[] configure() {
        return CoreOptions.options(
//                // this just adds all what you write here to java vm argumenents of the (new) osgi process.
//                vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5006" ),
//                // this is necessary to let junit runner not timout the remote process before attaching debugger
//                // setting timeout to 0 means wait as long as the remote service comes available.
//                waitForFrameworkStartup(),

                // Run this one in Felix
                CoreOptions.frameworks(CoreOptions.felix()),
                
                CoreOptions.mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.apache.cxf.dosgi").artifactId("cxf-dosgi-ri-singlebundle-distribution").versionAsInProject(),
                
                // The following two bundles start the greeter demo which registers a remote service 
                CoreOptions.mavenBundle().groupId("org.apache.cxf.dosgi.samples").artifactId("cxf-dosgi-ri-samples-greeter-interface").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.apache.cxf.dosgi.samples").artifactId("cxf-dosgi-ri-samples-greeter-impl").versionAsInProject(),
                
                // This bundle contains the common system testing code
                CoreOptions.mavenBundle().groupId("org.apache.cxf.dosgi.systests").artifactId("cxf-dosgi-ri-systests2-common").versionAsInProject()
        );
    }
    
    @Test
    public void testAccessEndpoint() throws Exception {
        // call into base test. Inheriting the test doesn't properly report failures.
        baseTestAccessEndpoint();
    }
}
