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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.utility.RequestUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class RequestUtilityTest {

	@Test
	@DisplayName("Undefined subject role test")
	void testExtractSubjectRoleFromTokenUndefined() {
		List<Document> listMetadata = new ArrayList<Document>();
		String undefinedSubjectRole = Constants.AppConstants.JWT_MISSING_SUBJECT_ROLE;
		assertEquals(undefinedSubjectRole, RequestUtility.extractSubjectRoleFromToken(listMetadata));
	}
	
	@Test
	@DisplayName("field test undefined")
	void testExtractFieldFromMetadataUndefined() {
		List<Document> listMetadata = new ArrayList<Document>();
		String undefinedField = Constants.AppConstants.UNKNOWN_DOCUMENT_TYPE;
		assertEquals(undefinedField, RequestUtility.extractFieldFromMetadata(listMetadata, undefinedField));
	}
	
	@Test
	@DisplayName("issuer test undefined")
	void testExtractIssuerFromMetadataUndefined() {
		List<Document> listMetadata = new ArrayList<Document>();
		String undefinedIssuer = Constants.AppConstants.UNKNOWN_ISSUER;
		assertEquals(undefinedIssuer, RequestUtility.extractIssuerFromToken(listMetadata, undefinedIssuer));
	}
}
