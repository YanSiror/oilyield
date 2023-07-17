package com.example.demo.services.impl;

import com.example.demo.bean.Mail;
import com.example.demo.config.RabbitConfig;
import com.example.demo.services.ConsumerService;
import com.example.demo.utils.SendMailUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;

@Component
@Slf4j
public class ConsumerServiceImpl implements ConsumerService {
    @Autowired
    private SendMailUtil sendMailUtil;

    @Override
    @RabbitListener(queues = RabbitConfig.MAIL_QUEUE_NAME)
    public void consume(Message message, Channel channel) throws IOException {
        //将消息转化为对象
        String str = new String(message.getBody());
        //Mail mail = JsonUtil.strToObj(str, Mail.class);
        ObjectMapper objectMapper = new ObjectMapper();
        Mail mail  = objectMapper.readValue(str, Mail.class);
        log.info("收到消息: {}", mail.toString());
        MessageProperties properties = message.getMessageProperties();
        long tag = properties.getDeliveryTag();
        boolean success = false;
        try {
            success = sendMailUtil.send(mail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        if (success) {
            channel.basicAck(tag, false);// 消费确认
        } else {
            channel.basicNack(tag, false, true);
        }
    }
}
