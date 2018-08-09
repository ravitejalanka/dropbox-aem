package com.db.aem.helper;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * Created by Jatin on 12/16/2015.
 */
public class DropboxUtil {
    public static DbxClient getClient(String clientIdentifier, String userLocale, String accessToken){
        DbxRequestConfig config = new DbxRequestConfig(clientIdentifier, userLocale);
        DbxClient client = new DbxClient(config, accessToken);
        return client;
    }

    public static void fetchChildren(String path, DbxClient client, JSONArray files, final Resource resource){
        try{
            if(client !=  null){
                DbxEntry.WithChildren child = client.getMetadataWithChildren(path);
                System.out.println(child.hash);

                for(DbxEntry entry : child.children){
                    JSONObject fileObj =  new JSONObject();
                    if(entry.isFile()){
                        DbxEntry.File file =  entry.asFile();
                        if(file != null){
                            fileObj.put("path", file.path);
                            fileObj.put("name", file.name);
                            fileObj.put("clientMtime", file.clientMtime.toString());
                            fileObj.put("lastModified", file.lastModified.toString());
                            fileObj.put("humanSize", file.humanSize);
                            fileObj.put("shareURL", client.createShareableUrl(file.path));
                            fileObj.put("iconName", file.iconName);
                            fileObj.put("rev", file.rev);
                            fileObj.put("numBytes",file.numBytes);
                            fileObj.put("iconName", file.iconName);
                            files.put(fileObj);
                        }
                    }
                    if(entry.isFolder()){
                        DbxEntry.Folder folder = entry.asFolder();
                        fetchChildren(folder.path, client, files, resource);
                    }
                }
            }
        }catch(DbxException ex){
            ex.printStackTrace();
        }catch(JSONException ex){
            ex.printStackTrace();
        }

    }

    public static void writeNode(JSONArray dataArray, final Resource resource)
            throws RepositoryException, ParseException, JSONException {
        String targetURL = null;
        if (resource != null) {
            Node parentNode = resource.adaptTo(Node.class);
            if (parentNode != null) {
                targetURL = parentNode.getPath();
                if (targetURL != null && targetURL.trim().length() > 0) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject obj = dataArray.getJSONObject(i);
                        Node targetNode = parentNode;
                        if(obj != null){
                            String path = obj.getString("path");
                            String rev = obj.getString("rev");
                            String uploadedTime = obj.getString("clientMtime");
                            String humanSize = obj.getString("humanSize");
                            String name = obj.getString("name");
                            String shareURL = obj.getString("shareURL");
                            String lastModified = obj.getString("lastModified");
                            String numBytes = obj.getString("numBytes");
                            String iconClass = obj.getString("iconName");
                            if(path != null){
                                String dropboxRelativePath = getDropboxRelativePath(path);
                                if(dropboxRelativePath.length() > 0){
                                    targetNode = JcrUtils.getOrCreateByPath(parentNode, dropboxRelativePath.substring(1), false, null, JcrConstants.NT_FOLDER,true);
                                }
                                if(targetNode != null && targetNode.hasNode(name.trim())){
                                    System.out.println("Target Node Present :: :: " + targetNode.getPath());
                                }
                                if(targetNode != null &&  !(targetNode.hasNode(name.trim()))){
                                    Node documentNode = JcrUtils.getOrAddNode(targetNode,name.trim().replaceAll(" ", "_"),"dam:Asset");
                                    if(documentNode != null){
                                        Node documntContentNode = JcrUtils.getOrAddNode(documentNode,JcrConstants.JCR_CONTENT,"dam:AssetContent");
                                        if(documntContentNode != null){
                                            Node metadataNode = JcrUtils.getOrAddNode(documntContentNode,"metadata", "nt:unstructured");
                                            Node renditionNode = JcrUtils.getOrAddNode(documntContentNode,"renditions", "nt:folder");
                                            if (metadataNode != null) {
                                                metadataNode.setProperty("dam:size1", numBytes);
                                                metadataNode.setProperty("dam:shareURL", shareURL);
                                                metadataNode.setProperty("dam:revision", rev);
                                                metadataNode.setProperty("dc:title", name);
                                                metadataNode.setProperty("dc:format", getMimeType(iconClass));
                                                metadataNode.setProperty("dam:downloadURL", getDownloadURL(shareURL));
                                                metadataNode.setProperty("dam:humanreadablesize", humanSize);
                                                parentNode.getSession().save();
                                            }
                                        }
                                        InputStream is = null;
                                        String inputStringURL = "";
                                        if(iconClass.equalsIgnoreCase(DropboxConstant.EXCEL_FILE_ICON)){
                                            inputStringURL = DropboxConstant.EXCEL_FILE_HTTP_URL;
                                        }
                                        else if(iconClass.equalsIgnoreCase(DropboxConstant.PDF_ICON)){
                                            inputStringURL = DropboxConstant.PDF_FILE_HTTP_URL;
                                        }
                                        else if(iconClass.equalsIgnoreCase(DropboxConstant.WORD_FILE_ICON)){
                                            inputStringURL = DropboxConstant.WORD_FILE_HTTP_URL;
                                        }
                                        else{
                                            inputStringURL = DropboxConstant.PPT_FILE_HTTP_URL;
                                        }
                                        try{
                                            URL _url = new URL(inputStringURL);
                                            HttpURLConnection urlConnection = (HttpURLConnection)_url.openConnection();
                                            is = urlConnection.getInputStream();
                                            if(is.available() > 0){
                                                Resource res = resource.getResourceResolver().resolve(documentNode.getPath());
                                                Asset asset = DamUtil.resolveToAsset(res);
                                                if(asset != null){
                                                    asset.addRendition("original",is, "image/png");
                                                    parentNode.getSession().save();
                                                }
                                                is.close();
                                            }
                                        }catch(IOException io){
                                            io.printStackTrace();
                                        }


                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private static String getMD5Hash(String arg){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(arg.getBytes());
            byte byteData[] = md.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<byteData.length;i++) {
                String hex=Integer.toHexString(0xff & byteData[i]);
                if(hex.length()==1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }

    private static  String getDropboxRelativePath(String path){
        return path.substring(0, path.lastIndexOf("/"));
    }

    private static String getDownloadURL(String url){
        if(url != null && url.trim().length() > 0 && url.indexOf("dl=0") > -1)
        {
            return url.replace("dl=0", "dl=1");
        }
        return url;
    }


    /*
        Returns the mimeType of file based on icon class provided
     */
    private static String getMimeType(String iconClass){
        switch(iconClass){
            case DropboxConstant.EXCEL_FILE_ICON:
                return DropboxConstant.EXCEL_MIMETYPE;
            case DropboxConstant.PDF_ICON:
                return DropboxConstant.PDF_MIMETYPE;
            case DropboxConstant.WORD_FILE_ICON:
                return DropboxConstant.WORD_DOCUMENT_MIMETYPE;
            case DropboxConstant.PPT_ICON:
                return DropboxConstant.PPT_MIMETYPE;
        }

        return null;
    }




}
