<%@page session="false"%><%--
  Copyright 1997-2008 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Download component

  Draws a download link with icons.

--%><%@ page import="com.day.cq.wcm.api.WCMMode,
                     com.day.cq.wcm.api.components.DropTarget,
                     com.day.cq.wcm.foundation.Download,
                     com.day.cq.wcm.foundation.Placeholder,
                     com.day.cq.xss.ProtectionContext,
					com.day.cq.dam.api.Asset,
org.apache.sling.api.resource.ResourceResolver,
org.apache.sling.api.resource.Resource,
                     com.day.cq.xss.XSSProtectionException,
                     com.day.cq.xss.XSSProtectionService,
org.apache.sling.api.resource.ValueMap,
                     com.day.text.Text,
                     org.apache.commons.lang3.StringEscapeUtils,
                     java.util.Map" %><%
%><%@include file="/libs/foundation/global.jsp"%>

<cq:includeClientLib css="dropbox"/>
<%
    //drop target css class = dd prefix + name of the drop target in the edit config
    String ddClassName = DropTarget.CSS_CLASS_PREFIX + "file";
	ValueMap map = resource.adaptTo(ValueMap.class);
	String fileReferencePath = null;
    String downloadURL = "";
    if(map != null){
        fileReferencePath = map.get("fileReference") != null ? map.get("fileReference").toString() : null;
	ResourceResolver resolver = resource.getResourceResolver();
	if(resolver != null && fileReferencePath != null){
    Resource res = resolver.resolve(fileReferencePath);
    if(res != null){
		Asset asset = res.adaptTo(Asset.class);
        if(asset != null){
			downloadURL = asset.getMetadataValue("dam:downloadURL");
        }else{
			downloadURL = res.getPath();
        }

    }
    //out.println(dropboxResource.getResourceMetadata().get("dam:downloadURL").toString());
    }}
    Download dld = new Download(resource);

    if (dld.hasContent()) {
        dld.addCssClass(ddClassName);
        String title = dld.getTitle(true);
        String href = Text.escape(dld.getHref(), '%', true);
        String displayText = dld.getInnerHtml() == null ? dld.getFileName() : dld.getInnerHtml().toString();
        String description = dld.getDescription();
        XSSProtectionService xss = sling.getService(XSSProtectionService.class);
        if (xss != null) {
            try {
                displayText = xss.protectForContext(ProtectionContext.PLAIN_HTML_CONTENT, displayText);
            } catch (XSSProtectionException e) {
                log.warn("Unable to protect link display text from XSS: {}", displayText);
            }
            try {
                description = xss.protectForContext(ProtectionContext.PLAIN_HTML_CONTENT, description);
            } catch (XSSProtectionException e) {
                log.warn("Unable to protect link description from XSS: {}", description);
            }
        }

        %><div>
            <span class="icon type_<%= dld.getIconType() %>"><img src="/etc/designs/default/0.gif" alt="*"></span>
            <a href="<%= downloadURL %>" title="<%=StringEscapeUtils.escapeHtml4(title)%>"><%= StringEscapeUtils.escapeHtml4(displayText) %></a><br>
            <small><%= StringEscapeUtils.escapeHtml4(description) %></small>
        </div><div class="clear"></div><%

    }else  if (WCMMode.fromRequest(request) == WCMMode.EDIT) {
        String classicPlaceholder =
                "<img src=\"/libs/cq/ui/resources/0.gif\" class=\"cq-file-placeholder " + ddClassName + "\" alt=\"\">";
        String placeholder = Placeholder.getDefaultPlaceholder(slingRequest, component, classicPlaceholder,
                ddClassName, null);

        %><%= placeholder %><%
    }
%>
