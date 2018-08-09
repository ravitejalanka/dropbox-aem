<%--

  MailChimp Configuration component.

  MailChimp Configuration

--%><%
%><%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false" %><%
%><%
	// TODO add you code here
%> 

<div>
    <h3>Dropbox Settings</h3>   
</div>
<%
	String accessToken = properties.get("access-token", null);
	String path = properties.get("dropbox-path", "/content/dam/dropbox");
	String clientIdentifier = properties.get("dropbox-client-identifier", null);

%> 

<div>
    <span>Access Token : </span><span>
    <% if(accessToken != null){
     %><%= accessToken %>
    <%}else{%>
    Please provide Access token to configure Dropbox
    <%}%></span>
</div>

<div>
    <span>Path : </span><span><%= path %>
    </span>
</div>

<div>
    <span>Client Identifier : </span><span>
    <% if(clientIdentifier == null){
		clientIdentifier = "dropbox-default";%>
    <%}%><%=clientIdentifier%></span>
</div>



<% if(accessToken != null){%>

<script>
    function fetchDatafromDropbox(){

        var accessToken = '<%=accessToken%>';
		var path = '<%=path%>';
        var clientIdentifier = '<%=clientIdentifier%>';    
        
}

</script>
<button onclick="fetchDatafromDropbox()"> Fetch Data </button><%}%>