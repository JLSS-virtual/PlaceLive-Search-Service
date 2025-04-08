package com.jlss.placelive.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // Changed: Using new Elasticsearch Java client with a custom RestClientTransport and JacksonJsonpMapper.
        RestClientBuilder builder = RestClient.builder(
                        new HttpHost("localhost", 9200, "http"))
                .setDefaultHeaders(new org.apache.http.Header[]{
                        new BasicHeader("Authorization", "Basic " +
                                Base64.getEncoder().encodeToString("elastic:QjswXAAW++lajzr5R0*a".getBytes()))
                })
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    try {
                        return httpClientBuilder
                                .setSSLContext(SSLContext.getDefault())
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                });

        RestClient restClient = builder.build();

        // Changed: Using RestClientTransport with the new client mapper.
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
