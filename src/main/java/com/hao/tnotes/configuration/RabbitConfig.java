package com.hao.tnotes.configuration;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("103.218.240.252");
        connectionFactory.setUsername("tmusic"); // RabbitMQ 默认的用户名
        connectionFactory.setPassword("tmusic"); // RabbitMQ 默认的密码
        connectionFactory.setVirtualHost("/t-music"); // 虚拟主机，通常是“/”
        return connectionFactory;
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory()); // 将 ConnectionFactory 传给 RabbitTemplate
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter()); // 可以根据需要设置消息转换器
        return rabbitTemplate;
    }



}
