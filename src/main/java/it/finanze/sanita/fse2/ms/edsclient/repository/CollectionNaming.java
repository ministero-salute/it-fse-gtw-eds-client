package it.finanze.sanita.fse2.ms.edsclient.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.utility.ProfileUtility;

@Configuration
public class CollectionNaming {
    
    @Autowired
    private ProfileUtility profileUtility;

    @Bean("iniEdsInvocationBean")
    public String getIniEdsInvocationCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.INI_EDS_INVOCATION;
        }
        return Constants.ComponentScan.Collections.INI_EDS_INVOCATION;
    }
}
