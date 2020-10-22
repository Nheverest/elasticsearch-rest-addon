/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.elasticsearch;

import org.elasticsearch.plugins.Plugin;
import org.hibernate.validator.constraints.NotEmpty;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Config("elasticSearch")
public class ElasticSearchConfig {
    private Map<String, ClientConfig> clients = new HashMap<>();

    public Map<String, ClientConfig> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    public ElasticSearchConfig addClient(String name, ClientConfig config) {
        this.clients.put(name, config);
        return this;
    }

    public static class ClientConfig {
        @NotEmpty
        @SingleValue
        private List<String> hosts = new ArrayList<>();
        private Properties properties = new Properties();
        private Set<Class<? extends Plugin>> plugins = new HashSet<>();

        public List<String> getHosts() {
            return Collections.unmodifiableList(hosts);
        }

        public ClientConfig addHost(String host) {
            this.hosts.add(host);
            return this;
        }

        public Properties getProperties() {
            return properties;
        }

        public ClientConfig setProperty(String key, String value) {
            this.properties.setProperty(key, value);
            return this;
        }

        public Set<Class<? extends Plugin>> getPlugins() {
            return Collections.unmodifiableSet(plugins);
        }

        public ClientConfig addPlugin(Class<? extends Plugin> plugin) {
            this.plugins.add(plugin);
            return this;
        }
    }
}
