<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>

  <xsl:template match="/">
    <HTML>
      <HEAD><TITLE>Weather Report</TITLE></HEAD>  
      <BODY>
        <H1><font color="blue">Today's Weather</font></H1>
        <TABLE width="500">
		<th>City</th>
		<th>Low</th>
		<th>High</th>
		<th>Wind Direction</th>
		<th>Wind Speed</th>
      	<xsl:apply-templates/>
        </TABLE>
      </BODY>
    </HTML>
  </xsl:template>
  
  <xsl:template match="CityWeather">
    <tr>
	<td>
		<xsl:value-of select="City"/>
	</td>
	<td align="center">
		<xsl:value-of select="Temperature/Low"/>
	</td>
	<td align="center">
		<xsl:value-of select="Temperature/High"/>
	</td>
	<td align="center">
		<xsl:value-of select="Wind/direction"/>
	</td>
	<td align="center">
		<xsl:value-of select="Wind/speed"/>
	</td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
