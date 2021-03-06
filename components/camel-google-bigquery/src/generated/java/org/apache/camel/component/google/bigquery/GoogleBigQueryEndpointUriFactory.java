/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.google.bigquery;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.camel.spi.EndpointUriFactory;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
public class GoogleBigQueryEndpointUriFactory extends org.apache.camel.support.component.EndpointUriFactorySupport implements EndpointUriFactory {

    private static final String BASE = ":projectId:datasetId:tableId";

    private static final Set<String> PROPERTY_NAMES;
    static {
        Set<String> set = new HashSet<>(8);
        set.add("projectId");
        set.add("datasetId");
        set.add("tableId");
        set.add("connectionFactory");
        set.add("lazyStartProducer");
        set.add("useAsInsertId");
        set.add("basicPropertyBinding");
        set.add("synchronous");
        PROPERTY_NAMES = set;
    }

    @Override
    public boolean isEnabled(String scheme) {
        return "google-bigquery".equals(scheme);
    }

    @Override
    public String buildUri(String scheme, Map<String, Object> properties) throws URISyntaxException {
        String syntax = scheme + BASE;
        String uri = syntax;

        Map<String, Object> copy = new HashMap<>(properties);

        uri = buildPathParameter(syntax, uri, "projectId", null, true, copy);
        uri = buildPathParameter(syntax, uri, "datasetId", null, true, copy);
        uri = buildPathParameter(syntax, uri, "tableId", null, false, copy);
        uri = buildQueryParameters(uri, copy);
        return uri;
    }

    @Override
    public Set<String> propertyNames() {
        return PROPERTY_NAMES;
    }

    @Override
    public boolean isLenientProperties() {
        return false;
    }
}

