package setxpro.email.infra;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import setxpro.email.dtos.EmailRequestDto;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public Map<String, Object> consumerConfig() {
        final HashMap<String, Object> result = new HashMap<>();
        result.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        result.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        result.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return result;
    }

    @Bean
    public ConsumerFactory<String, EmailRequestDto> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig(), null, new JsonDeserializer<>(EmailRequestDto.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmailRequestDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmailRequestDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
}
