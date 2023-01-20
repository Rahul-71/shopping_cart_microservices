package com.microservice.cloudgateway.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.cloudgateway.model.AuthenticationResponse;

@RestController
@RequestMapping(path = "/authenticate")
public class AuthenticationController {

    @GetMapping(path = "/login")
    public ResponseEntity<AuthenticationResponse> login(
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model,
            @RegisteredOAuth2AuthorizedClient("okta") OAuth2AuthorizedClient client) {

        String userId = oidcUser.getEmail();
        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();
        long expiresAt = accessToken.getExpiresAt().getEpochSecond();

        List<String> authorityList = oidcUser
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .userId(userId)
                .accessToken(accessToken.getTokenValue())
                .refreshToken(refreshToken.getTokenValue())
                .expiresAt(expiresAt)
                .authorityList(authorityList)
                .build();

        return ResponseEntity.ok(authenticationResponse);
    }
}
