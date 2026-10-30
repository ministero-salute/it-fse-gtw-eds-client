package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.repository.impl.EdsInvocationRepo;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(EdsInvocationRepo.class)
class EdsRepositoryIsolationTest {
    @Autowired private EdsInvocationRepo repository;
    @MockitoBean private MongoTemplate mongo;

    @Test
    void findsAndRemovesUsingWorkflowCriteria() {
        IniEdsInvocationETY document = new IniEdsInvocationETY();
        when(mongo.findOne(any(Query.class), eq(IniEdsInvocationETY.class))).thenReturn(document);
        when(mongo.findAndRemove(any(Query.class), eq(IniEdsInvocationETY.class))).thenReturn(document);
        assertSame(document, repository.find("workflow"));
        assertSame(document, repository.remove("workflow"));
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongo).findOne(query.capture(), eq(IniEdsInvocationETY.class));
        assertEquals("workflow", query.getValue().getQueryObject().getString(IniEdsInvocationETY.FIELD_WIF));
        verify(mongo).findAndRemove(query.capture(), eq(IniEdsInvocationETY.class));
        assertEquals("workflow", query.getValue().getQueryObject().getString(IniEdsInvocationETY.FIELD_WIF));
    }

    @Test
    void returnsNullWhenDocumentDoesNotExist() {
        assertNull(repository.find("missing"));
        assertNull(repository.remove("missing"));
    }

    @Test
    void wrapsMongoFailuresWithOriginalCause() {
        RuntimeException failure = new RuntimeException("mongo unavailable");
        when(mongo.findOne(any(Query.class), eq(IniEdsInvocationETY.class))).thenThrow(failure);
        when(mongo.findAndRemove(any(Query.class), eq(IniEdsInvocationETY.class))).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class, () -> repository.find("workflow")).getCause());
        assertSame(failure, assertThrows(BusinessException.class, () -> repository.remove("workflow")).getCause());
    }
}
