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

import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FidoServerAssertionResultEndpointSuccessHandler implements AuthenticationSuccessHandler {

    private final ServerEndpointFilterUtil serverEndpointFilterUtil;

    public FidoServerAssertionResultEndpointSuccessHandler(ObjectConverter objectConverter) {
        this.serverEndpointFilterUtil = new ServerEndpointFilterUtil(objectConverter);
    }

    public FidoServerAssertionResultEndpointSuccessHandler() {
        this(new ObjectConverter());
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Authentication authentication) throws IOException {

        AssertionResultSuccessResponse successResponse = new AssertionResultSuccessResponse();
        serverEndpointFilterUtil.writeResponse(httpServletResponse, successResponse);

    }
}
