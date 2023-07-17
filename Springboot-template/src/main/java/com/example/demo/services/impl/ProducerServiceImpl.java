package com.example.demo.services.impl;

import com.example.demo.bean.Mail;
import com.example.demo.config.RabbitConfig;
import com.example.demo.services.ProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProducerServiceImpl implements ProducerService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public boolean send(Mail mail) {
        //创建uuid
        String msgId = UUID.randomUUID().toString().replaceAll("-", "");
        mail.setMsgId(msgId);

        //发送消息到rabbitMQ
        CorrelationData correlationData = new CorrelationData(msgId);
        //rabbitTemplate.convertAndSend(RabbitConfig.MAIL_EXCHANGE_NAME, RabbitConfig.MAIL_ROUTING_KEY_NAME, MessageHelper.objToMsg(mail), correlationData);
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.MAIL_EXCHANGE_NAME, RabbitConfig.MAIL_ROUTING_KEY_NAME, new ObjectMapper().writeValueAsString(mail), correlationData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return true;
    }
}
