package it.finanze.sanita.fse2.ms.edsclient.service.impl;


import it.finanze.sanita.fse2.ms.edsclient.client.IConfigClient;
import it.finanze.sanita.fse2.ms.edsclient.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.service.IConfigSRV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.edsclient.client.routes.base.ClientRoutes.Config.PROPS_NAME_REMOVE_METADATA_ENABLE;
import static it.finanze.sanita.fse2.ms.edsclient.enums.ConfigItemTypeEnum.EDS_CLIENT;

@Service
@Slf4j
public class ConfigSRV implements IConfigSRV {

    private static final long DELTA_MS = 300_000L;

    @Autowired
    private IConfigClient client;

    private final Map<String, Pair<Long, String>> props;

    public ConfigSRV() {
        this.props = new HashMap<>();
    }

    @PostConstruct
    public void postConstruct() {
        for(ConfigItemTypeEnum en : ConfigItemTypeEnum.priority()) {
            log.info("[GTW-CFG] Retrieving {} properties ...", en.name());
            ConfigItemDTO items = client.getConfigurationItems(en);
            List<ConfigItemDTO.ConfigDataItemDTO> opts = items.getConfigurationItems();
            for(ConfigItemDTO.ConfigDataItemDTO opt : opts) {
                opt.getItems().forEach((key, value) -> {
                    log.info("[GTW-CFG] Property {} is set as {}", key, value);
                    props.put(key, Pair.of(new Date().getTime(), value));
                });
            }
            if(opts.isEmpty()) log.info("[GTW-CFG] No props were found");
        }
        integrity();
    }

    @Override
    public Boolean isRemoveMetadataEnable() {
        long lastUpdate = props.get(PROPS_NAME_REMOVE_METADATA_ENABLE).getKey();
        if (new Date().getTime() - lastUpdate >= DELTA_MS) {
            synchronized(ConfigSRV.class) {
                if (new Date().getTime() - lastUpdate >= DELTA_MS) {
                    refresh(PROPS_NAME_REMOVE_METADATA_ENABLE);
                }
            }
        }
        return Boolean.parseBoolean(
                props.get(PROPS_NAME_REMOVE_METADATA_ENABLE).getValue()
        );
    }

    private void refresh(String name) {
        String previous = props.getOrDefault(name, Pair.of(0L, null)).getValue();
        String prop = client.getProps(name, previous, EDS_CLIENT);
        props.put(name, Pair.of(new Date().getTime(), prop));
    }

    private void integrity() {
        String err = "Missing props {} from eds-client";
        String[] out = new String[]{
            PROPS_NAME_REMOVE_METADATA_ENABLE
        };
        for (String prop : out) {
            if(!props.containsKey(prop)) throw new IllegalStateException(err.replace("{}", prop));
        }
    }

}
