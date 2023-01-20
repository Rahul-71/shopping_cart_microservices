package com.microservices.orderservice.intercept;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private OAuth2AuthorizedClientManager auth2AuthorizedClientManager;

    public RestTemplateInterceptor(OAuth2AuthorizedClientManager auth2AuthorizedClientManager) {
        this.auth2AuthorizedClientManager = auth2AuthorizedClientManager;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        // override the request & pass the Authorization: Bearer+{access token} in the
        // header
        OAuth2AuthorizedClient authorizedClient = this.auth2AuthorizedClientManager.authorize(
                OAuth2AuthorizeRequest
                        .withClientRegistrationId("internal-client") // as mentioned in application.yml ie. clientName
                        .principal("internal") // as mentioned in applixation.yml ie. scope
                        .build());

        request.getHeaders().add("Authorization",
                "Bearer " + authorizedClient.getAccessToken().getTokenValue());

        return execution.execute(request, body);
    }

}
