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

import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.data.extension.client.AuthenticationExtensionClientInput;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientInputs;
import com.webauthn4j.springframework.security.challenge.ChallengeRepository;
import com.webauthn4j.springframework.security.options.AssertionOptions;
import com.webauthn4j.springframework.security.options.AssertionOptionsProvider;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.Assert;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FIDO Server Endpoint for assertion options processing
 * With this endpoint, non-authorized user can observe requested username existence and his/her credentialId list.
 */
public class FidoServerAssertionOptionsEndpointFilter extends ServerEndpointFilterBase {

    /**
     * Default name of path suffix which will validate this filter.
     */
    public static final String FILTER_URL = "/webauthn/assertion/options";

    //~ Instance fields
    // ================================================================================================

    private final AssertionOptionsProvider optionsProvider;
    private final ChallengeRepository challengeRepository;

    public FidoServerAssertionOptionsEndpointFilter(ObjectConverter objectConverter, AssertionOptionsProvider optionsProvider, ChallengeRepository challengeRepository) {
        super(FILTER_URL, objectConverter);
        this.optionsProvider = optionsProvider;
        this.challengeRepository = challengeRepository;
        checkConfig();
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        checkConfig();
    }

    @SuppressWarnings("squid:S2177")
    private void checkConfig() {
        Assert.notNull(optionsProvider, "optionsProvider must not be null");
    }


    @Override
    protected ServerResponse processRequest(HttpServletRequest request) {
        InputStream inputStream;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            ServerPublicKeyCredentialGetOptionsRequest serverRequest =
                    objectConverter.getJsonConverter().readValue(inputStream, ServerPublicKeyCredentialGetOptionsRequest.class);
            Challenge challenge = serverEndpointFilterUtil.encodeUserVerification(new DefaultChallenge(), serverRequest.getUserVerification());
            challengeRepository.saveChallenge(challenge, request);
            //TODO: UsernamePasswordAuthenticationToken should not be used here in this way
            AssertionOptions options = optionsProvider.getAssertionOptions(request, new UsernamePasswordAuthenticationToken(serverRequest.getUsername(), null, Collections.emptyList()));
            List<ServerPublicKeyCredentialDescriptor> credentials = options.getAllowCredentials().stream()
                    .map(credential -> new ServerPublicKeyCredentialDescriptor(credential.getType(), Base64UrlUtil.encodeToString(credential.getId()), credential.getTransports()))
                    .collect(Collectors.toList());
            AuthenticationExtensionsClientInputs<AuthenticationExtensionClientInput> authenticationExtensionsClientInputs;
            if (serverRequest.getExtensions() != null) {
                authenticationExtensionsClientInputs = serverRequest.getExtensions();
            } else {
                authenticationExtensionsClientInputs = options.getExtensions();
            }

            return new ServerPublicKeyCredentialGetOptionsResponse(
                    Base64UrlUtil.encodeToString(options.getChallenge().getValue()),
                    options.getTimeout(),
                    options.getRpId(),
                    credentials,
                    serverRequest.getUserVerification(),
                    authenticationExtensionsClientInputs);
        }
        catch (DataConversionException e){
            throw new com.webauthn4j.springframework.security.exception.DataConversionException("Failed to convert data", e);
        }
    }

}
