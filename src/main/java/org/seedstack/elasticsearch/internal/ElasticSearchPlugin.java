/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.elasticsearch.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.seedstack.elasticsearch.ElasticSearchConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This plugin manages clients used to access ElasticSearch instances.
 */
public class ElasticSearchPlugin extends AbstractSeedPlugin {
    private static final int DEFAULT_ELASTIC_SEARCH_PORT = 9200;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchPlugin.class);
    private final Map<String, RestHighLevelClient> elasticSearchClients = new HashMap<>();

    @Override
    public String name() {
        return "elasticsearch";
    }

    @Override
    public InitState initialize(InitContext initContext) {
        ElasticSearchConfig elasticSearchConfig = getConfiguration(ElasticSearchConfig.class);

        if (!elasticSearchConfig.getClients().isEmpty()) {
            for (Entry<String, ElasticSearchConfig.ClientConfig> clientEntry : elasticSearchConfig.getClients().entrySet()) {
                String clientName = clientEntry.getKey();
                ElasticSearchConfig.ClientConfig clientConfig = clientEntry.getValue();

                List<String> hosts = clientConfig.getHosts();
                LOGGER.info("Creating ElasticSearch client {} for remote instance at {}", clientName, hosts);
                elasticSearchClients.put(clientName, buildRestClient(clientName, clientConfig));
            }
        } else {
            LOGGER.info("No ElasticSearch client configured, ElasticSearch support disabled");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new ElasticSearchModule(elasticSearchClients);
    }

    @Override
    public void stop() {
        for (Entry<String, RestHighLevelClient> entry : elasticSearchClients.entrySet()) {
            LOGGER.info("Closing ElasticSearch client {}", entry.getKey());
            try {
                ((Proxy) entry.getValue()).setHandler(new DefaultHandler());
                entry.getValue().close();
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to properly close ElasticSearch client %s", entry.getKey()), e);
            }
        }
    }

    private RestHighLevelClient buildRestClient(String clientName, ElasticSearchConfig.ClientConfig clientConfig) {
        Settings.Builder settingsBuilder = Settings.builder();
        if (clientConfig.getProperties() != null) {
            clientConfig.getProperties().forEach((key, value) -> settingsBuilder.put((String) key, (String) value));
        }
        List<HttpHost> hosts = buildHosts(clientName, clientConfig);
        RestHighLevelClient restClient = null;
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(RestHighLevelClient.class);
            factory.setFilter(new MethodFilter() {
                @Override
                public boolean isHandled(Method m) {
                    return !m.getName().equals("finalize");
                }
            });

            Class[] parameterTypes = {RestClientBuilder.class};
            RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]));
            Object[] parameters = {builder};

            restClient = (RestHighLevelClient) factory.create(parameterTypes, parameters, new NoCloseHandler());
        } catch (NoSuchMethodException | IllegalArgumentException |
                InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_CREATE_PROXY).put("class", RestHighLevelClient.class.getName());
        }
        return restClient;
    }

    private List<HttpHost> buildHosts(String clientName, ElasticSearchConfig.ClientConfig clientConfig) {
        List<HttpHost> hosts = new ArrayList<>();

        for (String host : clientConfig.getHosts()) {
            String[] hostInfo = host.split(":");
            if (hostInfo.length > 2) {
                throw SeedException.createNew(ElasticSearchErrorCode.INVALID_HOST)
                        .put("clientName", clientName)
                        .put("host", host);
            }
            String address = hostInfo[0].trim();
            int port = DEFAULT_ELASTIC_SEARCH_PORT;
            try {
                if (hostInfo.length > 1) {
                    port = Integer.parseInt(hostInfo[1]);
                }
            } catch (NumberFormatException e) {
                throw SeedException.wrap(e, ElasticSearchErrorCode.INVALID_PORT)
                        .put("clientName", clientName)
                        .put("host", host)
                        .put("port", hostInfo[1]);
            }
            hosts.add(new HttpHost(address, port, "http"));
        }
        return hosts;
    }
}