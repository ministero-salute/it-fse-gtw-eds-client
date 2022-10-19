/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.config;

import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import org.springframework.http.HttpMethod;

import java.util.EnumMap;
import java.util.Map;

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
		
		public static final String DOCKER = "docker";

		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class AppConstants {

        private AppConstants() {}

		public static final String UNKNOWN_ISSUER = "UNKNOWN_ISSUER";

		public static final String UNKNOWN_DOCUMENT_TYPE = "UNKNOWN_DOCUMENT_TYPE";

		public static final String JWT_MISSING_SUBJECT_ROLE = "UNDEFINED_SUBJECT_ROLE";

		public static final Map<ProcessorOperationEnum, HttpMethod> methodMap = new EnumMap<>(ProcessorOperationEnum.class);
		static {
			methodMap.put(ProcessorOperationEnum.PUBLISH, HttpMethod.POST);
			methodMap.put(ProcessorOperationEnum.REPLACE, HttpMethod.PUT);
			methodMap.put(ProcessorOperationEnum.UPDATE, HttpMethod.PUT);
			methodMap.put(ProcessorOperationEnum.DELETE, HttpMethod.DELETE);
		}

	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
