/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.edsclient;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.utility.RequestUtility;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link RequestUtility}. Its methods are static and pure, so the test runs as
 * plain JUnit without booting a Spring context (which would require MongoDB / broker infra).
 */
public class RequestUtilityTest {

	@Test
	@DisplayName("Undefined subject role test")
	void testExtractSubjectRoleFromTokenUndefined() {
		List<Document> listMetadata = new ArrayList<>();

		String undefinedSubjectRole = Constants.AppConstants.JWT_MISSING_SUBJECT_ROLE;
		assertEquals(undefinedSubjectRole, RequestUtility.extractSubjectRoleFromToken(listMetadata));
	}
	
	@Test
	@DisplayName("field test undefined")
	void testExtractFieldFromMetadataUndefined() {
		List<Document> listMetadata = new ArrayList<>();
		String undefinedField = Constants.AppConstants.UNKNOWN_DOCUMENT_TYPE;
		assertEquals(undefinedField, RequestUtility.extractFieldFromMetadata(listMetadata, undefinedField));
	}

	@Test
	@DisplayName("issuer test undefined")
	void testExtractIssuerFromMetadataUndefined() {
		List<Document> listMetadata = new ArrayList<>();
		String undefinedIssuer = Constants.AppConstants.UNKNOWN_ISSUER;
		assertEquals(undefinedIssuer, RequestUtility.extractIssuerFromToken(listMetadata, undefinedIssuer));
	}

	@Test
	@DisplayName("Extract subject Role success")
	void testExtractSubjectRoleFromTokenSuccess(){
			List<Document> listMetadata = new ArrayList<>();
			Document doc = new Document("tokenEntry", new Document("payload", new Document("subject_role", "testRole")));
			listMetadata.add(doc);

			String expectedSubjectRole = "testRole";
			String actualSubjectRole = RequestUtility.extractSubjectRoleFromToken(listMetadata);
			assertEquals(expectedSubjectRole, actualSubjectRole);
	}

	@Test
	@DisplayName("Extract issuer success")
	void testExtractIssuerFromTokenSuccess(){
		List<Document> listMetadata = new ArrayList<>();
		Document doc = new Document("tokenEntry", new Document("payload", new Document("fieldName", "fieldName")));
		listMetadata.add(doc);

		String expectedIssuer = "fieldName";
		String actualIssuer = RequestUtility.extractIssuerFromToken(listMetadata, "fieldName");
		assertEquals(expectedIssuer, actualIssuer);
	}

	@Test
	@DisplayName("Extract field from metadata success")
	void testExtractFieldFromMetadataSuccess(){
		List<Document> listMetadata = new ArrayList<>();
		Document doc = new Document("documentEntry",  new Document("fieldName", "fieldName"));
		listMetadata.add(doc);

		String expectedField = "fieldName";
		String actualField = RequestUtility.extractFieldFromMetadata(listMetadata, "fieldName");
		assertEquals(expectedField, actualField);
	}

	@Test
	@DisplayName("Extract JWT claims maps the payload and omits absent delegation_scope")
	void testExtractJwtClaimsSuccess() {
		List<Document> listMetadata = new ArrayList<>();
		Document payload = new Document();
		payload.put("sub", "RSSMRA22A01A399Z");
		payload.put("subject_role", "AAS");
		payload.put("person_id", "BMTBTS01A01I526W");
		payload.put("purpose_of_use", "TREATMENT");
		payload.put("locality", "201123456");
		payload.put("subject_organization", "Regione Marche");
		payload.put("subject_organization_id", "110");
		payload.put("patient_consent", true); // outside JWT_PAYLOAD_CLAIMS -> must be omitted
		listMetadata.add(new Document("tokenEntry", new Document("payload", payload)));

		Map<String, Object> claims = RequestUtility.extractJwtClaims(listMetadata);

		assertEquals("RSSMRA22A01A399Z", claims.get("sub"));
		assertEquals("AAS", claims.get("subject_role"));
		assertEquals("BMTBTS01A01I526W", claims.get("person_id"));
		assertEquals("TREATMENT", claims.get("purpose_of_use"));
		assertEquals("201123456", claims.get("locality"));
		assertEquals("Regione Marche", claims.get("subject_organization"));
		assertEquals("110", claims.get("subject_organization_id"));
		assertFalse(claims.containsKey("delegation_scope"), "delegation_scope must be omitted when absent");
		assertFalse(claims.containsKey("patient_consent"), "claims outside JWT_PAYLOAD_CLAIMS must be omitted");
		assertEquals(7, claims.size());
	}

	@Test
	@DisplayName("Extract JWT claims forwards delegation_scope when present")
	void testExtractJwtClaimsWithDelegationScope() {
		List<Document> listMetadata = new ArrayList<>();
		Document payload = new Document();
		payload.put("sub", "RSSMRA22A01A399Z");
		payload.put("delegation_scope", "CAREGIVER");
		listMetadata.add(new Document("tokenEntry", new Document("payload", payload)));

		Map<String, Object> claims = RequestUtility.extractJwtClaims(listMetadata);

		assertEquals("RSSMRA22A01A399Z", claims.get("sub"));
		assertEquals("CAREGIVER", claims.get("delegation_scope"));
	}

	@Test
	@DisplayName("Extract JWT claims returns empty map for null metadata")
	void testExtractJwtClaimsNull() {
		Map<String, Object> claims = RequestUtility.extractJwtClaims(null);
		assertTrue(claims.isEmpty());
	}
}
