/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
