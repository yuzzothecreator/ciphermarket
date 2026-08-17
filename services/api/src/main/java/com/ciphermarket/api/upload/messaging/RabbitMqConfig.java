package com.ciphermarket.api.upload.messaging;

import com.ciphermarket.api.config.MessagingProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String UPLOAD_EXCHANGE = "ciphermarket.upload";

    @Bean
    DirectExchange uploadExchange() {
        return new DirectExchange(UPLOAD_EXCHANGE);
    }

    @Bean
    Queue uploadProcessingQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.uploadQueue())
                .deadLetterExchange("")
                .deadLetterRoutingKey(properties.uploadDlq())
                .build();
    }

    @Bean
    Queue uploadProcessingDlq(MessagingProperties properties) {
        return QueueBuilder.durable(properties.uploadDlq()).build();
    }

    @Bean
    Binding uploadProcessingBinding(Queue uploadProcessingQueue, DirectExchange uploadExchange) {
        return BindingBuilder.bind(uploadProcessingQueue).to(uploadExchange).with("processing");
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
