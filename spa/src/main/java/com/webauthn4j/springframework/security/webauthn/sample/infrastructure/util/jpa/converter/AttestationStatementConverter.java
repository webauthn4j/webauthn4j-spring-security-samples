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

package com.webauthn4j.springframework.security.webauthn.sample.infrastructure.util.jpa.converter;

import com.webauthn4j.converter.util.CborConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.util.Base64UrlUtil;

import jakarta.persistence.AttributeConverter;

/**
 * AttestationStatementConverter
 */
public class AttestationStatementConverter implements AttributeConverter<AttestationStatement, String> {

    private final CborConverter cborConverter;

    public AttestationStatementConverter(ObjectConverter objectConverter) {
        this.cborConverter = objectConverter.getCborConverter();
    }

    @Override
    public String convertToDatabaseColumn(AttestationStatement attribute) {
        AttestationStatementSerializationContainer container = new AttestationStatementSerializationContainer(attribute);
        return Base64UrlUtil.encodeToString(cborConverter.writeValueAsBytes(container));
    }

    @Override
    public AttestationStatement convertToEntityAttribute(String dbData) {
        byte[] data = Base64UrlUtil.decode(dbData);
        AttestationStatementSerializationContainer container = cborConverter.readValue(data, AttestationStatementSerializationContainer.class);
        return container.getAttestationStatement();
    }
}
