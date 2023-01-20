package com.microservices.orderservice.intercept;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class OAuthRequestInterceptor implements RequestInterceptor {

    @Autowired
    private OAuth2AuthorizedClientManager auth2AuthorizedClientManager;

    @Override
    public void apply(RequestTemplate template) {

        // override the request & pass the Authorization: Bearer+{access token} in the
        OAuth2AuthorizedClient authorizedClient = this.auth2AuthorizedClientManager.authorize(
                OAuth2AuthorizeRequest
                        .withClientRegistrationId("internal-client") // as mentioned in application.yml ie. clientName
                        .principal("internal") // as mentioned in applixation.yml ie. scope
                        .build());

        // adding header for feign service call (for internal api calls)
        template.header("Authorization",
                "Bearer " + authorizedClient.getAccessToken().getTokenValue());

    }

}
