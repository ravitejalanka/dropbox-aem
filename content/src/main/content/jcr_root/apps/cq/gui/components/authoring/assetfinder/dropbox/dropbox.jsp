<%@ page import="com.adobe.granite.security.user.util.AuthorizableUtil,
                 com.adobe.granite.ui.components.AttrBuilder,
                 com.adobe.granite.ui.components.Config,
                 com.day.cq.commons.date.RelativeTimeFormat,
                 com.day.cq.dam.api.Asset,
                 com.day.cq.dam.api.AssetReferenceResolver,
                 com.day.cq.dam.commons.util.UIHelper,
                 com.day.cq.wcm.resource.details.AssetDetails,
                 javax.jcr.Node,
                 java.util.Calendar,
                 java.util.ResourceBundle" %><%
%><%@page session="false" %><%
%><%@include file="/libs/granite/ui/global.jsp" %><%

    Node assetNode = resource.adaptTo(Node.class);
    Asset asset = resource.adaptTo(Asset.class);

    AssetDetails assetDetails = new AssetDetails(resource);

    ResourceBundle resourceBundle = slingRequest.getResourceBundle(slingRequest.getLocale());

    Config cfg = new Config(resource);
    AttrBuilder attrs = new AttrBuilder(request, xssAPI);

    // Additional classes
    attrs.addClass(cfg.get("class", String.class));

    attrs.addOther("path", assetNode.getPath());
    // attributes needed for drag&drop
    attrs.addOther("asset-group", "media");
    attrs.addOther("type", "Images");

    attrs.addOther("param", assetDetails.getParamJSON());

    attrs.addOthers(cfg.getProperties(), "id", "class");

    // mimeType
    String mimeType = assetDetails.getMimeType();
    if (mimeType != null) {
        // attribute needed for drag&drop
        attrs.addOther("asset-mimetype", mimeType);
    }

    // image source
    String imgSrc = request.getContextPath() + assetDetails.getThumbnailUrl()
            + "?ch_ck=" + UIHelper.getCacheKiller(assetNode);

    String formattedName = AuthorizableUtil.getFormattedName(
            resource.getResourceResolver(),
            asset.getModifier());

    //calculate number of asset references
    int referencesCount = 0; /*assetDetails.getReferencesSize(sling.getService(AssetReferenceResolver.class));*/

    //calculate number of comments
    int commentsCount = assetDetails.getCommentsSize();

    // get last modified date
    long lastModifiedMillis = assetDetails.getLastModified();
    Calendar lastModifiedRaw = Calendar.getInstance();
    lastModifiedRaw.setTimeInMillis(lastModifiedMillis);
    String lastModified = new RelativeTimeFormat("r", resourceBundle).format(lastModifiedMillis);

    // check if asset is new (age < 24 hours)
    Calendar twentyFourHoursAgo = Calendar.getInstance();
    twentyFourHoursAgo.add(Calendar.DATE, -1);
    boolean isNew = twentyFourHoursAgo.before(lastModifiedRaw);

%>
<article class="card-asset cq-draggable" draggable="true" <%= attrs.build() %>>
    <i class="select"></i>
    <i class="move"></i>
    <div class="card"><%
        if (isNew) {
            %><span class="flag info"><%= i18n.get("New") %></span><%
        }
        %>
        <span class="image"><img class="show-grid cq-dd-image"
            src="<%= xssAPI.encodeForHTMLAttr(imgSrc) %>"
            alt="<%= xssAPI.encodeForHTMLAttr(assetDetails.getDescription()) %>"></span>
        <a href="#" class="label">
            <h4><%= xssAPI.encodeForHTML(assetDetails.getName()) %></h4>
            <p class="type"><%= xssAPI.encodeForHTML(mimeType) %></p>
            <p class="resolution"><%= xssAPI.encodeForHTML(assetDetails.getResolution()) %></p>
            <p class="size"><%= xssAPI.encodeForHTML(asset.getMetadataValue("dam:humanreadablesize")) %></p>
            <div class="info">
                <p class="modified"><%
                %><i class="coral-Icon coral-Icon--edit coral-Icon--sizeS" title="<%= i18n.get("Modified")%>"></i> <%
                %><span class="date"><%= xssAPI.encodeForHTML(i18n.getVar(lastModified))
                %></span><span class="user"><%= xssAPI.encodeForHTML(formattedName)%></span></p>
                <p class="links"><%
                    if (referencesCount > 0) {
                %><i class="coral-Icon coral-Icon--link coral-Icon--sizeS" title="<%= i18n.get("Links") %>"></i> <%
                %><%= referencesCount %><%
                    }
                %></p>
                 <p class="comments"><%
                    if (commentsCount > 0) {
                %><i class="coral-Icon coral-Icon--comment coral-Icon--sizeS" title="<%= i18n.get("Comments") %>"></i> <%
                %><%= commentsCount %><%
                    }
                %></p>
            </div>
        </a>
    </div>
</article>