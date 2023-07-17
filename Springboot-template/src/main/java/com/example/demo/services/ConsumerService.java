package com.example.demo.services;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.io.IOException;

public interface ConsumerService {
    public void consume(Message message, Channel channel) throws IOException;
}
