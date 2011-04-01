<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslthl="http://xslthl.sf.net"
    exclude-result-prefixes="xslthl"
    version="1.0">
  <xsl:include href="common.xsl"/>
  <xsl:param name="fop1.extensions" select="1"/>
  <xsl:attribute-set name="monospace.verbatim.properties">
    <xsl:attribute name="font-size">8pt</xsl:attribute>
  </xsl:attribute-set>
</xsl:stylesheet>
