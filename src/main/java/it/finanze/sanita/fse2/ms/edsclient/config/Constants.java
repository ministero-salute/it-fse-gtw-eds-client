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
package it.finanze.sanita.fse2.ms.edsclient.config;

import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import org.springframework.http.HttpMethod;

import java.util.EnumMap;
import java.util.Map;

/**
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

		
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
	
	public static final class Collections {

		public static final String INI_EDS_INVOCATION = "ini_eds_invocation";

		private Collections() {

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

		public static final String MOCKED_GATEWAY_NAME = "mocked-gateway";

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
