/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webauthn4j.springframework.security.fido.server.endpoint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.converter.util.JsonConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.UserVerificationRequirement;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.springframework.security.WebAuthnAssertionAuthenticationToken;
import com.webauthn4j.springframework.security.WebAuthnAuthenticationParameters;
import com.webauthn4j.springframework.security.WebAuthnAuthenticationRequest;
import com.webauthn4j.springframework.security.fido.server.validator.ServerPublicKeyCredentialValidator;
import com.webauthn4j.springframework.security.server.ServerPropertyProvider;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;

public class FidoServerAssertionResultEndpointFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * Default name of path suffix which will validate this filter.
     */
    public static final String FILTER_URL = "/webauthn/assertion/result";

    private final JsonConverter jsonConverter;
    private final ServerPropertyProvider serverPropertyProvider;
    private final ServerPublicKeyCredentialValidator<ServerAuthenticatorAssertionResponse> serverPublicKeyCredentialValidator;
    private final TypeReference<ServerPublicKeyCredential<ServerAuthenticatorAssertionResponse>> credentialTypeRef
            = new TypeReference<ServerPublicKeyCredential<ServerAuthenticatorAssertionResponse>>() {
    };
    private final CollectedClientDataConverter collectedClientDataConverter;
    private final ServerEndpointFilterUtil serverEndpointFilterUtil;

    public FidoServerAssertionResultEndpointFilter(
            ObjectConverter objectConverter,
            ServerPropertyProvider serverPropertyProvider,
            RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);

        this.jsonConverter = objectConverter.getJsonConverter();
        this.serverPropertyProvider = serverPropertyProvider;
        this.serverPublicKeyCredentialValidator = new ServerPublicKeyCredentialValidator<>();

        this.setAuthenticationSuccessHandler(new FidoServerAssertionResultEndpointSuccessHandler(objectConverter));
        this.setAuthenticationFailureHandler(new FidoServerAssertionResultEndpointFailureHandler(objectConverter));

        this.collectedClientDataConverter = new CollectedClientDataConverter(objectConverter);
        this.serverEndpointFilterUtil = new ServerEndpointFilterUtil(objectConverter);

        checkConfig();
    }

    public FidoServerAssertionResultEndpointFilter(ObjectConverter objectConverter, ServerPropertyProvider serverPropertyProvider, String defaultFilterProcessesUrl) {
        this(objectConverter, serverPropertyProvider, new AntPathRequestMatcher(defaultFilterProcessesUrl, HttpMethod.POST.name()));
    }

    public FidoServerAssertionResultEndpointFilter(ObjectConverter objectConverter, ServerPropertyProvider serverPropertyProvider) {
        this(objectConverter, serverPropertyProvider, new AntPathRequestMatcher(FILTER_URL, HttpMethod.POST.name()));
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        checkConfig();
    }

    @SuppressWarnings("squid:S2177")
    private void checkConfig() {
        Assert.notNull(serverPropertyProvider, "serverPropertyProvider must not be null");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        InputStream inputStream;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try{
            ServerPublicKeyCredential<ServerAuthenticatorAssertionResponse> credential = jsonConverter.readValue(inputStream, credentialTypeRef);
            serverPublicKeyCredentialValidator.validate(credential);

            ServerAuthenticatorAssertionResponse assertionResponse = credential.getResponse();

            ServerProperty serverProperty = serverPropertyProvider.provide(request);

            CollectedClientData collectedClientData = collectedClientDataConverter.convert(assertionResponse.getClientDataJSON());
            UserVerificationRequirement userVerificationRequirement = serverEndpointFilterUtil.decodeUserVerification(collectedClientData.getChallenge());

            WebAuthnAuthenticationRequest webAuthnAuthenticationRequest = new WebAuthnAuthenticationRequest(
                    credential.getRawId() == null ? null : Base64UrlUtil.decode(credential.getRawId()),
                    assertionResponse.getClientDataJSON() == null ? null : Base64UrlUtil.decode(assertionResponse.getClientDataJSON()),
                    assertionResponse.getAuthenticatorData() == null ? null : Base64UrlUtil.decode(assertionResponse.getAuthenticatorData()),
                    assertionResponse.getSignature() == null ? null : Base64UrlUtil.decode(assertionResponse.getSignature()),
                    credential.getClientExtensionResults()
            );
            WebAuthnAuthenticationParameters webAuthnAuthenticationParameters = new WebAuthnAuthenticationParameters(
                    serverProperty,
                    userVerificationRequirement == UserVerificationRequirement.REQUIRED,
                    false
            );

            WebAuthnAssertionAuthenticationToken webAuthnAssertionAuthenticationToken =
                    new WebAuthnAssertionAuthenticationToken(webAuthnAuthenticationRequest, webAuthnAuthenticationParameters, Collections.emptyList());
            setDetails(request, webAuthnAssertionAuthenticationToken);
            return this.getAuthenticationManager().authenticate(webAuthnAssertionAuthenticationToken);
        }
        catch (DataConversionException e){
            throw new com.webauthn4j.springframework.security.exception.DataConversionException("Failed to convert data", e);
        }
    }

    protected void setDetails(HttpServletRequest request, WebAuthnAssertionAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
