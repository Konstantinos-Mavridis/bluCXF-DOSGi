bluCXF-DOSGi
============
An implementation of the Apache CXF-DOSGi "AdderService" example using Aries Blueprint activation

required packages
----------------------------------------------------------------------------------------------------------------
equinox-SDK-3.8.1.zip
cxf-dosgi-ri-multibundle-distribution-1.4.0-dir.zip

config.ini
----------------------------------------------------------------------------------------------------------------
osgi.framework=file\:plugins/org.eclipse.osgi_3.8.1.v20120830-144521.jar
osgi.bundles=\
reference\:file\:org.eclipse.core.runtime_3.8.0.v20120521-2346.jar@start,\
reference\:file\:org.eclipse.equinox.common_3.6.100.v20120522-1841.jar@start,\
reference\:file\:org.eclipse.core.jobs_3.5.300.v20120622-204750.jar@start,\
reference\:file\:org.eclipse.equinox.registry_3.5.200.v20120522-1841.jar@start,\
reference\:file\:org.eclipse.equinox.preferences_3.5.0.v20120522-1841.jar@start,\
reference\:file\:org.eclipse.core.contenttype_3.4.200.v20120523-2004.jar@start,\
reference\:file\:org.eclipse.equinox.app_1.3.100.v20120522-1841.jar@start,\
reference\:file\:org.eclipse.equinox.util_1.0.400.v20120522-2049.jar@start,\
reference\:file\:org.eclipse.osgi.services_3.3.100.v20120522-1822.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/geronimo-osgi-registry-1.1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/geronimo-annotation_1.0_spec-1.1.1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.specs.activation-api-1.1-2.2.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/geronimo-jpa_2.0_spec-1.1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/geronimo-javamail_1.4_spec-1.7.1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/geronimo-servlet_3.0_spec-1.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/geronimo-ws-metadata_2.0_spec-1.1.3.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.bundles.jdom-1.1.2_1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/commons-lang-2.6.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/spring-core-3.0.6.RELEASE.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/spring-beans-3.0.6.RELEASE.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/jetty-all-server-7.4.2.v20110526.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/pax-web-spi-1.0.11.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/pax-web-runtime-1.0.11.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/pax-web-jetty-1.0.11.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/pax-logging-api-1.7.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/pax-logging-service-1.7.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.bundles.jaxb-impl-2.2.1.1_2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.bundles.wsdl4j-1.6.2_6.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/xmlsec-1.5.3.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/xmlschema-core-2.0.3.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.bundles.asm-3.3.1_1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.bundles.xmlresolver-1.2_5.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/neethi-3.0.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/stax2-api-3.1.1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/woodstox-core-asl-4.1.4.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.bundles.commons-pool-1.5.4_1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.specs.saaj-api-1.3-2.2.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.specs.stax-api-1.0-2.2.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.specs.jaxb-api-2.2-2.2.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.specs.jaxws-api-2.2-2.2.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.specs.jsr339-api-m10-2.2.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.servicemix.bundles.joda-time-1.5.2_4.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.aries.util-0.3.1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.aries.proxy-0.3.1.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.aries.blueprint-0.3.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.osgi.enterprise-4.2.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.felix.configadmin-1.2.8.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/org.apache.felix.fileinstall-3.1.10.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-api-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-core-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-frontend-simple-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-frontend-jaxws-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-frontend-jaxrs-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-databinding-jaxb-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-databinding-aegis-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-bindings-xml-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-bindings-soap-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-transports-http-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-transports-http-jetty-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-ws-policy-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-rt-rs-extension-providers-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-bundle-compatible-2.7.2.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-dosgi-ri-dsw-cxf-1.4.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-dosgi-ri-topology-manager-1.4.0.jar@start,\
reference\:file\:cxf-dosgi-ri-multibundle/cxf-dosgi-ri-discovery-local-1.4.0.jar@start,\
reference\:file\:cxf-dosgi-ri-samples-blueprint/cxf-dosgi-ri-samples-blueprint-interface-1.5-SNAPSHOT.jar@start,\
reference\:file\:cxf-dosgi-ri-samples-blueprint/cxf-dosgi-ri-samples-blueprint-impl-1.5-SNAPSHOT.jar@start
osgi.noShutdown=true

osgi.console.enable.builtin=true
eclipse.ignoreApp=true
org.ops4j.pax.web.session.timeout=30

note
-------------------------------------------------------------------------------------------------------------------
for the client-side replace the line:

   reference\:file\:cxf-dosgi-ri-samples-blueprint/cxf-dosgi-ri-samples-blueprint-impl-1.5-SNAPSHOT.jar@start

with:

   reference\:file\:cxf-dosgi-ri-samples-blueprint/cxf-dosgi-ri-samples-blueprint-client-1.5-SNAPSHOT.jar@start

