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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
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
}
