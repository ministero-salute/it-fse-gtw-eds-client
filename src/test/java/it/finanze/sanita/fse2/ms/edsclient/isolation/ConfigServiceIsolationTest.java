package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static it.finanze.sanita.fse2.ms.edsclient.client.routes.base.ClientRoutes.Config.PROPS_NAME_REMOVE_METADATA_ENABLE;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import it.finanze.sanita.fse2.ms.edsclient.client.IConfigClient;
import it.finanze.sanita.fse2.ms.edsclient.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.edsclient.utility.ProfileUtility;

@Tag("ISOLATION")
class ConfigServiceIsolationTest {
    private ConfigSRV service;
    private IConfigClient client;
    private ProfileUtility profiles;

    @BeforeEach
    void setup() {
        service = new ConfigSRV();
        client = mock(IConfigClient.class);
        profiles = mock(ProfileUtility.class);
        ReflectionTestUtils.setField(service, "client", client);
        ReflectionTestUtils.setField(service, "profiles", profiles);
    }

    private ConfigItemDTO items(Map<String, String> values) {
        ConfigItemDTO.ConfigDataItemDTO data = new ConfigItemDTO.ConfigDataItemDTO();
        data.setItems(values);
        ConfigItemDTO result = new ConfigItemDTO();
        result.setConfigurationItems(values.isEmpty() ? List.of() : List.of(data));
        return result;
    }

    @Test
    void initializesInPriorityOrderAndCachesLatestValue() {
        for (ConfigItemTypeEnum type : ConfigItemTypeEnum.priority()) {
            when(client.getConfigurationItems(type)).thenReturn(items(Map.of(PROPS_NAME_REMOVE_METADATA_ENABLE,
                    type == ConfigItemTypeEnum.EDS_CLIENT ? "true" : "false")));
        }
        service.postConstruct();
        assertTrue(service.isRemoveMetadataEnable());
        var order = inOrder(client);
        for (ConfigItemTypeEnum type : ConfigItemTypeEnum.priority()) order.verify(client).getConfigurationItems(type);
        verify(client, never()).getProps(anyString(), anyString(), any());
    }

    @Test
    void rejectsMissingRequiredConfiguration() {
        for (ConfigItemTypeEnum type : ConfigItemTypeEnum.priority()) {
            when(client.getConfigurationItems(type)).thenReturn(items(Map.of()));
        }
        assertTrue(assertThrows(IllegalStateException.class, service::postConstruct).getMessage()
                .contains(PROPS_NAME_REMOVE_METADATA_ENABLE));
    }

    @Test
    void skipsInitializationForTestProfile() {
        when(profiles.isTestProfile()).thenReturn(true);
        service.postConstruct();
        verifyNoInteractions(client);
    }

    @SuppressWarnings("unchecked")
    @Test
    void refreshesExpiredValuesAndKeepsCachedValuesOnFailure() {
        Map<String, Pair<Long, String>> props = (Map<String, Pair<Long, String>>) ReflectionTestUtils.getField(service, "props");
        props.put(PROPS_NAME_REMOVE_METADATA_ENABLE, Pair.of(0L, "false"));
        when(client.getProps(PROPS_NAME_REMOVE_METADATA_ENABLE, "false", ConfigItemTypeEnum.EDS_CLIENT)).thenReturn("true");
        assertTrue(service.isRemoveMetadataEnable());
        assertTrue(service.isRemoveMetadataEnable());
        verify(client, times(1)).getProps(PROPS_NAME_REMOVE_METADATA_ENABLE, "false", ConfigItemTypeEnum.EDS_CLIENT);
        props.put(PROPS_NAME_REMOVE_METADATA_ENABLE, Pair.of(0L, "false"));
        RuntimeException failure = new RuntimeException("config unavailable");
        when(client.getProps(PROPS_NAME_REMOVE_METADATA_ENABLE, "false", ConfigItemTypeEnum.EDS_CLIENT)).thenThrow(failure);
        assertSame(failure, assertThrows(RuntimeException.class, service::isRemoveMetadataEnable));
        assertEquals("false", props.get(PROPS_NAME_REMOVE_METADATA_ENABLE).getValue());
    }
}
