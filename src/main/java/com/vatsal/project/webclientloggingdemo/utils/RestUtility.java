package com.vatsal.project.webclientloggingdemo.utils;

import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import javax.annotation.PostConstruct;


/**
 * A utility class to get web client configurations.
 * @since v1.0
 */

@Slf4j
@Component
public class RestUtility {
    private WebClient.Builder webClientBuilder;

    @PostConstruct
    public void initialize() {
        this.webClientBuilder = getWebClientBuilder();
    }

    public WebClient.RequestHeadersSpec<?> buildConfiguration(String baseUrl, String uri, HttpMethod httpMethod, String body) {
        // host and url
        WebClient webClient = this.getBaseURI(baseUrl);

        // http method
        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = webClient.method(httpMethod);

        // uri
        WebClient.RequestBodySpec bodySpec = uriSpec.uri(uri);

        // headers
        bodySpec.headers(httpHeaders -> httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE));
        bodySpec.headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // body
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.body(Mono.just(body), String.class);
        log.info(headersSpec.toString());

        return headersSpec;
    }

    public WebClient getBaseURI(String baseUrl) {
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();
        log.info(baseUrl);
        return client;
    }

    private WebClient.Builder getWebClientBuilder() {
        var httpClient = HttpClient
                .create()
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    public <T> T sendRequest(String baseUrl, String uri, HttpMethod httpMethod, String body, Class<T> tClass) {
        WebClient.RequestHeadersSpec<?> headersSpec = buildConfiguration(baseUrl, uri, httpMethod, body);
        return getResponse(tClass, headersSpec);
    }

    private <T> T getResponse(Class<T> tClass, WebClient.RequestHeadersSpec<?> headersSpec) {
        T t;
        try {
            t = headersSpec.retrieve().bodyToMono(tClass).block();
        } catch (WebClientRequestException e) {
            log.error(e.getMessage());
            throw e;
        } catch (WebClientResponseException e) {
            var responseBody = e.getResponseBodyAsString();
            var httpStatus = e.getStatusCode();
            log.error(httpStatus.toString());
            log.error(responseBody);
            throw e;
        }
        return t;
    }
}
