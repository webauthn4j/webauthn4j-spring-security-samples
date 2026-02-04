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

package com.webauthn4j.springframework.security.webauthn.sample.app.util.jackson.deserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import com.webauthn4j.springframework.security.converter.Base64UrlStringToCollectedClientDataConverter;
import com.webauthn4j.springframework.security.webauthn.sample.app.api.CollectedClientDataForm;
import org.springframework.stereotype.Component;

/**
 * Jackson Deserializer for {@link CollectedClientDataForm}
 */
@Component
public class CollectedClientDataDeserializer extends StdDeserializer<CollectedClientDataForm> {

    private final Base64UrlStringToCollectedClientDataConverter base64UrlStringToCollectedClientDataConverter;

    @SuppressWarnings("unused")
    public CollectedClientDataDeserializer() {
        super(CollectedClientDataForm.class);
        this.base64UrlStringToCollectedClientDataConverter = com.webauthn4j.springframework.security.webauthn.sample.app.config.ApplicationContextHolder.getBean(Base64UrlStringToCollectedClientDataConverter.class);
    }

    public CollectedClientDataDeserializer(Base64UrlStringToCollectedClientDataConverter base64UrlStringToCollectedClientDataConverter) {
        super(CollectedClientDataForm.class);
        this.base64UrlStringToCollectedClientDataConverter = base64UrlStringToCollectedClientDataConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectedClientDataForm deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = p.getValueAsString();
        CollectedClientDataForm result = new CollectedClientDataForm();
        result.setCollectedClientData(base64UrlStringToCollectedClientDataConverter.convert(value));
        result.setClientDataBase64(value);
        return result;
    }
}
