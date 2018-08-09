package com.db.aem.service;
import java.text.ParseException;
import java.util.Locale;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.dropbox.core.DbxClient;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.polling.importer.ImportException;
import com.day.cq.polling.importer.Importer;
import com.db.aem.helper.DropboxUtil;


@Service(value = Importer.class)
@Component
@Property(name = Importer.SCHEME_PROPERTY, value = "fetchDropboxData", propertyPrivate = true)
public class DropboxImporter implements Importer {

    /** The logger. */
    private final static Logger LOGGER = LoggerFactory
            .getLogger(DropboxImporter.class);


    @Override
    public void importData(String scheme, String dataSource, final Resource resource, String username, String password) throws ImportException{
        LOGGER.info(":::::::::::::::::::::::::::   Dropbox first :::::::::::::::::::::::::::::::");
        LOGGER.info(" :: scheme :: "  +  scheme + " :: dataSource :: " + dataSource  + " :: Resource :: " + resource);
    }

    @Override
    public void importData(final String scheme, final String dataSource,
                           final Resource resource) throws ImportException {
        try {

            LOGGER.info(":::::::::::::::::::::::::::   Dropbox :::::::::::::::::::::::::::::::");
            LOGGER.info(" :: scheme :: "  +  scheme + " :: dataSource :: " + dataSource  + " :: Resource :: " + resource);
            ResourceResolver resolver = resource.getResourceResolver();
            if(resolver != null){
                Resource dataSourceresource  = resolver.getResource(dataSource);
                if(dataSourceresource != null){
                    Node dataSourceNode = dataSourceresource.adaptTo(Node.class);
                    if(dataSourceNode != null){
                        String accessToken = dataSourceNode.getProperty("access-token").getString();
                        String clientIdentifier = dataSourceNode.getProperty("dropbox-client-identifier").getString();
                        JSONArray dataArray = null;
                        if(clientIdentifier != null && accessToken != null){
                            DbxClient client = DropboxUtil.getClient(clientIdentifier, Locale.getDefault().toString(), accessToken);
                            if(client != null){
                                dataArray = new JSONArray();
                                DropboxUtil.fetchChildren("/", client, dataArray, resource);
                                if(dataArray.length() > 0){
                                     DropboxUtil.writeNode(dataArray, resource);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (RepositoryException  e) {
            LOGGER.error("Dropbox  RepositoryException", e);
        }catch(JSONException je){
            LOGGER.error("Dropbox JSONException", je);
        }catch(ParseException p){
            LOGGER.error("ParseException", p);
        }

    }
}
