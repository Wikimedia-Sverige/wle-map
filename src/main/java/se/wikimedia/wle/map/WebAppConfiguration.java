package se.wikimedia.wle.map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.model.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.InetAddress;
import java.time.Clock;

@Configuration
@ComponentScan(basePackages = "se.wikimedia")
public class WebAppConfiguration {

    @Bean
    @Scope("singleton")
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Scope("singleton")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configOverride(InetAddress.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Bean("service.ini")
    @Scope("singleton")
    public ConstrettoConfiguration constretto() {
        return ConstrettoBuilder.empty()
                .createIniFileConfigurationStore()
                .addResource(new ClassPathResource("service.ini"))
                .done()
                .createObjectConfigurationStore()
                .addObject(new Object())
                .done()
                .createSystemPropertiesStore()
                .getConfiguration();
    }

    @Bean("prevalenceBase")
    @Scope("singleton")
    public String prevalenceBase(
            @Autowired
            @Qualifier("service.ini")
                    ConstrettoConfiguration constrettoConfiguration
    ) {
        return constrettoConfiguration.evaluateToString("prevayler.prevalenceBase");
    }

    @Bean("prevalenceBase.mkdirs")
    @Scope("singleton")
    public boolean prevalenceBaseMkDirs(
            @Autowired
            @Qualifier("service.ini")
                    ConstrettoConfiguration constrettoConfiguration
    ) {
        return constrettoConfiguration.evaluateToBoolean("prevayler.prevalenceBase.mkdirs");
    }

    @Bean("lucenePath")
    @Scope("singleton")
    public String luceneBase(
            @Autowired
            @Qualifier("service.ini")
                    ConstrettoConfiguration constrettoConfiguration
    ) {
        return constrettoConfiguration.evaluateToString("lucene.path");
    }

    @Bean("lucenePath.mkdirs")
    @Scope("singleton")
    public boolean lucenePathMkDirs(
            @Autowired
            @Qualifier("service.ini")
                    ConstrettoConfiguration constrettoConfiguration
    ) {
        return constrettoConfiguration.evaluateToBoolean("lucene.path.mkdirs");
    }


}
