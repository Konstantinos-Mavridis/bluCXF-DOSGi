<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" version="1.0" encoding="UTF-8" indent="yes"/>
  <xsl:template match="/">
org.ops4j.pax.web.session.timeout=30
org.osgi.framework.startlevel=<xsl:value-of select="count(//bundles/felix_deps) + count(//bundles/bundle) + 2"/>
felix.auto.start.2=http://www.apache.org/dist/felix/org.osgi.compendium-1.2.0.jar
    <xsl:for-each select="//bundles/bundle">
      <xsl:variable name="i" select="position() + count(//bundles/felix_deps) + 2"/>
felix.auto.start.<xsl:value-of select="$i"/>=file:apache-cxf-dosgi-ri-1.0/dosgi_bundles/<xsl:value-of select="substring-after(text(), '.dir/apache-cxf-dosgi-ri-1.0/dosgi_bundles/')"/>
    </xsl:for-each>
  </xsl:template>
</xsl:transform>

