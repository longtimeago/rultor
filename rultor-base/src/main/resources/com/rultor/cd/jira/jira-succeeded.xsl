<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="text"/>
    <xsl:template match="/snapshot">
        <xsl:text>I managed to create a new environment and deploy </xsl:text>
        <xsl:value-of select="tags/tag[label='on-deploy']/attributes/attribute[name='name']/value"/>
        <xsl:text>into it.&#x0A;&#x0A;</xsl:text>
        <xsl:if test="tags/tag">
            <xsl:text>Tags:&#x0A;</xsl:text>
            <xsl:apply-templates select="tags/tag"/>
            <xsl:text>&#x0A;</xsl:text>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="steps/step">
                <xsl:text>{code}</xsl:text>
                <xsl:apply-templates select="steps/step"/>
                <xsl:text>&#x0A;{code}</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>Due to some internal error there are not details to post here.</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="tag">
        <xsl:text> * </xsl:text>
        <xsl:value-of select="label"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="level"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="markdown"/>
        <xsl:text>&#x0A;</xsl:text>
    </xsl:template>
    <xsl:template match="step">
        <xsl:text>&#x0A;</xsl:text>
        <xsl:text>$ </xsl:text>
        <xsl:value-of select="summary"/>
        <xsl:text>&#x0A;  </xsl:text>
        <xsl:choose>
            <xsl:when test="level = 'INFO'">
                <xsl:text>SUCCESS</xsl:text>
            </xsl:when>
            <xsl:when test="level = 'SEVERE'">
                <xsl:text>FAILURE</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="level"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="exception"/>
    </xsl:template>
    <xsl:template match="exception">
        <xsl:text> </xsl:text>
        <xsl:value-of select="cause"/>
        <xsl:text>&#x0A;&#x0A;</xsl:text>
        <xsl:value-of select="stacktrace"/>
    </xsl:template>
</xsl:stylesheet>
