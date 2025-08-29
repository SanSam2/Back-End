package org.example.sansam.global.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {
//
//    @Value("${spring.data.elasticsearch.endpoint}")
//    private String elasticsearchEndpoint;
//
//    @Value("${spring.data.elasticsearch.apiKey}")
//    private String apiKey;

    @Bean
    public ElasticsearchClient elasticsearchClient(ObjectMapper objectMapper) {
        RestClient restClient = RestClient.builder(HttpHost.create("http://localhost:9200"))
                .build();

        // Boot의 ObjectMapper에는 JavaTimeModule 등록 O
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(objectMapper)
        );

        return new ElasticsearchClient(transport);
    }
}
