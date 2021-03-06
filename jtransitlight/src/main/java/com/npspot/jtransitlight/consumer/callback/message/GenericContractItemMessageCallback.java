package com.npspot.jtransitlight.consumer.callback.message;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npspot.jtransitlight.MessageProcessingException;
import com.npspot.jtransitlight.contract.GenericContractItem;
import com.npspot.jtransitlight.publisher.model.BusMessage;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenericContractItemMessageCallback<T extends GenericContractItem> implements MessageCallback {

    private static final Logger LOGGER = LogManager.getLogger(GenericContractItemMessageCallback.class);

    private Class tClass;
    private GenericItemMessageCallback<T> receiverGenericMessageArrayCallback;
    private final ObjectMapper objectMapper;
    private final JavaType deltaType;

    public GenericContractItemMessageCallback(Class tClass, GenericItemMessageCallback<T> receiverGenericMessageArrayCallback) {
        this.receiverGenericMessageArrayCallback = receiverGenericMessageArrayCallback;
        this.tClass = tClass;
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaType type = objectMapper.getTypeFactory().constructType(tClass);
        deltaType = objectMapper.getTypeFactory().constructParametricType(BusMessage.class,type);

    }

    @Override
    public void onMessage(byte[] bytes) throws MessageProcessingException {
        try {
            BusMessage<T> busMessage = objectMapper.readValue(bytes, deltaType);
            receiverGenericMessageArrayCallback.onMessage(busMessage.getMessage());
        } catch (IOException e) {
            LOGGER.error("Error parsing message to " + tClass.getName() + " " + e.getMessage() + ". Message text: \n" + new String(bytes));
            throw new MessageProcessingException("Can't parse message to provided type " + tClass.getName(), e);
        }
    }

    @Override
    public boolean isAcknowledged() {
        return receiverGenericMessageArrayCallback.isAcknowledged();
    }
}
