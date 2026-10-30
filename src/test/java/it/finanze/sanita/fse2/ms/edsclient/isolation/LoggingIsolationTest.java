package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.*;
import org.apache.kafka.clients.producer.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.read.ListAppender;
import it.finanze.sanita.fse2.ms.edsclient.client.IConfigClient;
import it.finanze.sanita.fse2.ms.edsclient.dto.OptionalLogDataDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.*;
import it.finanze.sanita.fse2.ms.edsclient.logging.*;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;

@Tag("ISOLATION")
class LoggingIsolationTest {
    @Test
    @SuppressWarnings("unchecked")
    void asynchronousDeliveryHandlesSuccessCallbackFailureAndTimeout() {
        Producer<String, String> producer = mock(Producer.class);
        ProducerRecord<String, String> record = new ProducerRecord<>("topic", "value");
        FailedDeliveryCallback<String> failure = mock(FailedDeliveryCallback.class);
        AsynchronousDeliveryStrategy strategy = new AsynchronousDeliveryStrategy();
        assertTrue(strategy.send(producer, record, "event", failure));
        ArgumentCaptor<Callback> callback = ArgumentCaptor.forClass(Callback.class);
        verify(producer).send(eq(record), callback.capture());
        callback.getValue().onCompletion(null, null);
        verifyNoInteractions(failure);
        RuntimeException exception = new RuntimeException("delivery failed");
        callback.getValue().onCompletion(null, exception);
        verify(failure).onFailedDelivery("event", exception);
        org.apache.kafka.common.errors.TimeoutException timeout = new org.apache.kafka.common.errors.TimeoutException("timeout");
        when(producer.send(eq(record), any(Callback.class))).thenThrow(timeout);
        assertFalse(strategy.send(producer, record, "event", failure));
        verify(failure).onFailedDelivery("event", timeout);
    }

    @Test
    @SuppressWarnings("unchecked")
    void blockingDeliveryCoversWaitingTimeoutFailuresAndInterrupts() throws Exception {
        Producer<String, String> producer = mock(Producer.class);
        Future<RecordMetadata> future = mock(Future.class);
        ProducerRecord<String, String> record = new ProducerRecord<>("topic", "value");
        FailedDeliveryCallback<String> failure = mock(FailedDeliveryCallback.class);
        when(producer.send(record)).thenReturn(future);
        BlockingDeliveryStrategy strategy = new BlockingDeliveryStrategy();
        assertEquals(0L, strategy.getTimeout());
        assertTrue(strategy.send(producer, record, "event", failure));
        verify(future).get();
        strategy.setTimeout(15L);
        assertEquals(15L, strategy.getTimeout());
        assertTrue(strategy.send(producer, record, "event", failure));
        verify(future).get(15L, TimeUnit.MILLISECONDS);
        TimeoutException timeout = new TimeoutException("timeout");
        when(future.get(15L, TimeUnit.MILLISECONDS)).thenThrow(timeout);
        assertFalse(strategy.send(producer, record, "event", failure));
        verify(failure).onFailedDelivery("event", timeout);
        doThrow(new InterruptedException()).when(future).get(15L, TimeUnit.MILLISECONDS);
        assertFalse(strategy.send(producer, record, "event", failure));
        strategy.setTimeout(-1L);
        assertTrue(strategy.send(producer, record, "event", failure));
        when(producer.send(record)).thenThrow(new BufferExhaustedException("full"));
        assertFalse(strategy.send(producer, record, "event", failure));
    }

    static class TestAppender extends KafkaAppender<ILoggingEvent> {
        Producer<byte[], byte[]> producer;
        RuntimeException creationFailure;
        @Override protected Producer<byte[], byte[]> createProducer() {
            if (creationFailure != null) throw creationFailure;
            return producer;
        }
        long timestamp(Object event) { return super.getTimestamp((ILoggingEvent) event); }
    }

    @SuppressWarnings("unchecked")
    private TestAppender appender() {
        TestAppender appender = new TestAppender();
        appender.setContext(new LoggerContext());
        appender.setName("isolation");
        appender.addProducerConfig("bootstrap.servers=unused.invalid:9092");
        appender.addProducerConfig("invalid-entry");
        appender.setTopic("logs");
        Encoder<ILoggingEvent> encoder = mock(Encoder.class);
        when(encoder.encode(any())).thenReturn("payload".getBytes(StandardCharsets.UTF_8));
        appender.setEncoder(encoder);
        appender.producer = mock(Producer.class);
        return appender;
    }

    private LoggingEvent event(String logger) {
        LoggingEvent event = new LoggingEvent();
        event.setLoggerName(logger);
        event.setTimeStamp(123L);
        return event;
    }

    @Test
    @SuppressWarnings("unchecked")
    void appenderLazilySendsDefersKafkaLogsAndClosesProducer() {
        TestAppender appender = appender();
        appender.setPartition(-1);
        appender.start();
        assertTrue(appender.isStarted());
        verifyNoInteractions(appender.producer);
        appender.doAppend(event("org.apache.kafka.clients.NetworkClient"));
        verifyNoInteractions(appender.producer);
        appender.doAppend(event("application"));
        ArgumentCaptor<ProducerRecord<byte[], byte[]>> record = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(appender.producer, times(2)).send(record.capture(), any(Callback.class));
        assertEquals("logs", record.getValue().topic());
        assertNull(record.getValue().partition());
        assertEquals(123L, record.getValue().timestamp());
        assertArrayEquals("payload".getBytes(StandardCharsets.UTF_8), record.getValue().value());
        assertTrue(appender.isAppendTimestamp());
        appender.setAppendTimestamp(false);
        appender.setKeyingStrategy(new NoKeyKeyingStrategy());
        appender.doAppend(event("application"));
        verify(appender.producer, times(3)).send(record.capture(), any(Callback.class));
        assertNull(record.getValue().timestamp());
        appender.stop();
        verify(appender.producer).close();
        appender.stop();
    }

    @Test
    @SuppressWarnings("unchecked")
    void failedProducerCreationFallsBackToAttachedAppendersAndSupportsDetachment() {
        TestAppender appender = appender();
        appender.creationFailure = new IllegalStateException("producer unavailable");
        Appender<ILoggingEvent> fallback = mock(Appender.class);
        when(fallback.getName()).thenReturn("fallback");
        appender.addAppender(fallback);
        assertTrue(appender.isAttached(fallback));
        assertSame(fallback, appender.getAppender("fallback"));
        assertTrue(appender.iteratorForAppenders().hasNext());
        appender.start();
        LoggingEvent event = event("application");
        appender.doAppend(event);
        verify(fallback).doAppend(event);
        assertTrue(appender.detachAppender("fallback"));
        appender.addAppender(fallback);
        assertTrue(appender.detachAppender(fallback));
        appender.addAppender(fallback);
        appender.detachAndStopAllAppenders();
        verify(fallback).stop();
        appender.stop();
        verifyNoInteractions(appender.producer);
    }

    @Test
    void invalidAppenderDoesNotStartAndHostKeysAreStable() {
        TestAppender appender = new TestAppender();
        appender.setContext(new LoggerContext());
        appender.start();
        assertFalse(appender.isStarted());
        appender.stop();
        assertTrue(appender.getProducerConfig().containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        HostNameKeyingStrategy key = new HostNameKeyingStrategy();
        LoggerContext context = new LoggerContext();
        context.putProperty("HOSTNAME", "isolation-host");
        key.setContext(context);
        key.start();
        assertTrue(key.isStarted());
        assertArrayEquals(ByteBuffer.allocate(4).putInt("isolation-host".hashCode()).array(), key.createKey("event"));
        key.stop();
        assertNull(new NoKeyKeyingStrategy().createKey("event"));
    }

    @Test
    void structuredLoggingEmitsRealJsonAndCachesGatewayName() {
        IConfigClient config = mock(IConfigClient.class);
        when(config.getGatewayName()).thenThrow(new IllegalStateException("offline")).thenReturn("gateway");
        LoggerHelper helper = new LoggerHelper(config);
        Logger logger = (Logger) LoggerFactory.getLogger(LoggerHelper.class);
        ListAppender<ILoggingEvent> output = new ListAppender<>();
        output.start();
        logger.addAppender(output);
        try {
            helper.info("first", OperationLogEnum.SEND_TO_UAR, ResultLogEnum.OK, new Date(), null);
            OptionalLogDataDTO data = OptionalLogDataDTO.builder().issuer("issuer").role("doctor")
                    .fiscalCode("fiscal").documentType("type").workflowInstanceId("workflow").documentId("doc")
                    .locality("Rome").applicationId("app").applicationVendor("vendor").applicationVersion("1").build();
            helper.error("failed", OperationLogEnum.SEND_TO_UAR, ResultLogEnum.KO, new Date(), ErrorLogEnum.KO_PUB, data);
            helper.info("last", OperationLogEnum.SEND_TO_UAR, ResultLogEnum.OK, new Date(), data);
            var json = JsonUtility.jsonToObject(output.list.getLast().getFormattedMessage(), java.util.Map.class);
            assertEquals("gateway", json.get("gateway_name"));
            assertEquals("fiscal", json.get("op_fiscal_code"));
            assertEquals("doc", json.get("idDocumento"));
            assertEquals("app", json.get("op_application_id"));
            verify(config, times(2)).getGatewayName();
        } finally {
            logger.detachAppender(output);
            output.stop();
        }
    }
}
