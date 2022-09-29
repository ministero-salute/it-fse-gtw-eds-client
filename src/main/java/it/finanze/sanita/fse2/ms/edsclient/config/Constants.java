package it.finanze.sanita.fse2.ms.edsclient.config;

import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
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

		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class AppConstants {

        private AppConstants(){}

		public static Map<ProcessorOperationEnum, HttpMethod> methodMap = new HashMap<ProcessorOperationEnum, HttpMethod>() {

			/**
			 * Serial Version UID
			 */
			private static final long serialVersionUID = 5813388826870382105L;

			{
				put(ProcessorOperationEnum.PUBLISH, HttpMethod.POST);
				put(ProcessorOperationEnum.REPLACE, HttpMethod.PUT);
				put(ProcessorOperationEnum.UPDATE, HttpMethod.PUT);
				put(ProcessorOperationEnum.DELETE, HttpMethod.DELETE);
			}};
	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
