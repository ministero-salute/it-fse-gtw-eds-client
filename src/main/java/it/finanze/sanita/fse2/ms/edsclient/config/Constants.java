package it.finanze.sanita.fse2.ms.edsclient.config;

/**
 * 
 * @author Riccardo Bonesi
 *
 * Constants application.
 */
public final class Constants {

	/**
	 *	Path scan.
	 */
	public static final class ComponentScan {

		/**
		 * Base path.
		 */
		public static final String BASE = "it.finanze.sanita.fse2.ms.edsclient";

		/**
		 * Controller path.
		 */
		public static final String CONTROLLER = "it.finanze.sanita.fse2.ms.edsclient.controller";

		/**
		 * Service path.
		 */
		public static final String SERVICE = "it.finanze.sanita.fse2.ms.edsclient.service";

		/**
		 * Configuration path.
		 */
		public static final String CONFIG = "it.finanze.sanita.fse2.ms.edsclient.config";
		
		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.finanze.sanita.fse2.ms.edsclient.config.mongo";
		
		/**
		 * Configuration mongo repository path.
		 */
		public static final String REPOSITORY_MONGO = "it.finanze.sanita.fse2.ms.edsclient.repository";

		public static final class Collections {

			public static final String INI_EDS_INVOCATION = "ini_eds_invocation";

			private Collections() {

			}
		}
		
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
 
	public static final class Profile {
		public static final String TEST = "test";
		public static final String TEST_PREFIX = "test_";

		/**
		 * Dev profile.
		 */
		public static final String DEV = "dev";

		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class IniClientConstants {

        private IniClientConstants(){}

        public static final String HEADER_ACTION = "urn:ihe:iti:2007:RegisterDocumentSet-b";
        public static final String HEADER_AUTH_CONTEXT = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";
        public static final String HEADER_ATTRNAME_URI = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";
		public static final String GENERIC_SUBJECT_SSN_OID = "^^^&2.16.840.1.113883.2.9.4.3.2&ISO";

		public static final String GENERIC_SSN_OID = "^^^^^^^^&2.16.840.1.113883.2.9.4.3.2&ISO";
		public static final String AUTHOR_IVA_OID = "^^^^^^^^&2.16.840.1.113883.2.9.6.3.2%ISO";
		public static final String AUTHOR_INSTITUTION_OID = "^^^^^&2.16.840.1.113883.2.9.4.1.3&ISO^^^^";
		public static final String VALID_SSN_OID = "^^^&2.16.840.1.113883.2.9.4.3.2&ISO";
	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
