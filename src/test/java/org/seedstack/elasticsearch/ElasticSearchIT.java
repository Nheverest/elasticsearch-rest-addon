/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.elasticsearch;

import org.assertj.core.api.Assertions;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.testing.junit4.SeedITRunner;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@RunWith(SeedITRunner.class)
public class ElasticSearchIT {
    @Inject
    @Named("client1")
    RestHighLevelClient remoteClient;

    @Test
    public void clients_are_injectable() {
        assertThat(remoteClient).isNotNull();
    }

    @Test(expected = SeedException.class)
    public void assert_remote_client_close_exception() throws IOException {
        remoteClient.close();
    }

    @Test
    public void remote_indexing_and_searching() throws ElasticsearchException, IOException, JSONException {
        indexing_and_searching(remoteClient);
    }

    public void indexing_and_searching(RestHighLevelClient client) throws ElasticsearchException, IOException, JSONException {
        XContentBuilder xContentBuilder = jsonBuilder()
                .startObject()
                .field("user", "seed")
                .field("postDate", new Date())
                .field("message", "trying out Elasticsearch")
                .endObject();

        IndexRequest indexRequest = new IndexRequest("index1", "mapping1", "1")
                .source(xContentBuilder);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        assertThat(indexResponse.status().equals(RestStatus.OK));
        String index = indexResponse.getIndex();

        SearchRequest searchRequest = new SearchRequest(index);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        assertThat(response.status().equals(RestStatus.OK));
        SearchHits results = response.getHits();
        assertThat(results).isNotEmpty();
        //XContentBuilder xContentResponseBuilder = response.innerToXContent(xContentBuilder, null);
        //JSONAssert.assertEquals(xContentResponseBuilder., response.innerToXContent(xContentBuilder, null), true);
    }
}
