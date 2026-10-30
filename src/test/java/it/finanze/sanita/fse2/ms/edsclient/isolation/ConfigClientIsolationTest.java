package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import it.finanze.sanita.fse2.ms.edsclient.client.impl.ConfigClient;
import it.finanze.sanita.fse2.ms.edsclient.client.routes.ConfigClientRoutes;
import it.finanze.sanita.fse2.ms.edsclient.client.response.WhoIsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ConfigItemTypeEnum;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({ConfigClient.class, ConfigClientRoutes.class})
@TestPropertySource(properties = "gtw-config.url.host=http://config.invalid")
class ConfigClientIsolationTest {
    @Autowired private ConfigClient client;
    @Autowired private ConfigClientRoutes routes;
    @MockitoBean private RestTemplate http;

    @Test
    void readsConfigurationAndGatewayName() {
        ConfigItemDTO items = new ConfigItemDTO();
        when(http.getForObject(routes.getConfigItems(ConfigItemTypeEnum.EDS_CLIENT), ConfigItemDTO.class)).thenReturn(items);
        assertSame(items, client.getConfigurationItems(ConfigItemTypeEnum.EDS_CLIENT));
        assertNull(client.getGatewayName());
        WhoIsResponseDTO name = new WhoIsResponseDTO("gateway");
        when(http.getForObject(routes.whois(), WhoIsResponseDTO.class)).thenReturn(name);
        assertEquals("gateway", client.getGatewayName());
        assertEquals("[CFG]", routes.identifier());
        assertEquals("cfg", routes.microservice());
        assertEquals("http://config.invalid/v1/config-items?type=EDS_CLIENT", routes.getConfigItems(ConfigItemTypeEnum.EDS_CLIENT));
    }

    @Test
    void prefersSpecificPropertyAndFallsBackToGeneric() {
        String specific = routes.getConfigItem(ConfigItemTypeEnum.EDS_CLIENT, "key");
        String generic = routes.getConfigItem(ConfigItemTypeEnum.GENERIC, "key");
        when(http.getForObject(specific, String.class)).thenReturn("specific");
        assertEquals("specific", client.getProps("key", "old", ConfigItemTypeEnum.EDS_CLIENT));
        verify(http, never()).getForObject(generic, String.class);
        when(http.getForObject(specific, String.class)).thenReturn(null);
        when(http.getForObject(generic, String.class)).thenReturn("generic");
        assertEquals("generic", client.getProps("key", "old", ConfigItemTypeEnum.EDS_CLIENT));
        when(http.getForObject(generic, String.class)).thenReturn(null);
        assertNull(client.getProps("key", "old", ConfigItemTypeEnum.EDS_CLIENT));
    }

    @Test
    void preservesPreviousValueWhenUnreachable() {
        when(http.getForEntity(routes.status(), String.class)).thenThrow(new ResourceAccessException("offline"));
        assertEquals("old", client.getProps("key", "old", ConfigItemTypeEnum.EDS_CLIENT));
        verify(http, never()).getForObject(anyString(), eq(String.class));
    }
}
