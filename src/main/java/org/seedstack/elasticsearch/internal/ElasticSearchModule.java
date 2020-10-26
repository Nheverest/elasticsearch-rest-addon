/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.elasticsearch.internal;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Map;
import java.util.Map.Entry;

class ElasticSearchModule extends PrivateModule {
    private Map<String, RestHighLevelClient> elasticSearchClientWrappers;

    ElasticSearchModule(Map<String, RestHighLevelClient> elasticSearchClientWrappers) {
        this.elasticSearchClientWrappers = elasticSearchClientWrappers;
    }

    @Override
    protected void configure() {
        if (elasticSearchClientWrappers != null && !elasticSearchClientWrappers.isEmpty()) {
            for (Entry<String, RestHighLevelClient> entry : elasticSearchClientWrappers.entrySet()) {
                Key<RestHighLevelClient> clientKey = Key.get(RestHighLevelClient.class, Names.named(entry.getKey()));
                bind(clientKey).toInstance(entry.getValue());
                expose(clientKey);
            }
        }
    }
}
