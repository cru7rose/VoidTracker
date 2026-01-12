package com.example.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class KafkaTestConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.orders-created}")
    private String ordersCreatedTopic;
    @Value("${app.kafka.topics.orders-assigned}")
    private String ordersAssignedTopic;
    @Value("${app.kafka.topics.orders-status-changed}")
    private String ordersStatusChangedTopic;
    @Value("${app.kafka.topics.route-planned}")
    private String routePlannedTopic;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // --- OSTATECZNA POPRAWKA ---
    // Jawne zdefiniowanie fabryki konsumentów z poprawnie skonfigurowanym
    // deserializatorem JSON.
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Tworzymy i konfigurujemy deserializer JSON bezpośrednio
        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(objectMapper);
        jsonDeserializer.setUseTypeHeaders(true); // Allow type headers
        jsonDeserializer.addTrustedPackages("*"); // Ufaj wszystkim pakietom (bezpieczne w teście)

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public NewTopic ordersCreatedTopicBean() {
        return TopicBuilder.name(ordersCreatedTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic ordersAssignedTopicBean() {
        return TopicBuilder.name(ordersAssignedTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic ordersStatusChangedTopicBean() {
        return TopicBuilder.name(ordersStatusChangedTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic routePlannedTopicBean() {
        return TopicBuilder.name(routePlannedTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public org.springframework.kafka.core.ProducerFactory<String, Object> testProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonSerializer.class);
        configProps.put(org.springframework.kafka.support.serializer.JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "testKafkaTemplate")
    public org.springframework.kafka.core.KafkaTemplate<String, Object> testKafkaTemplate() {
        return new org.springframework.kafka.core.KafkaTemplate<>(testProducerFactory());
    }
}
