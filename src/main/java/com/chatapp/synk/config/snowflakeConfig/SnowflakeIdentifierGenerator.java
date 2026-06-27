package com.chatapp.synk.config.snowflakeConfig;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class SnowflakeIdentifierGenerator implements IdentifierGenerator, ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        // Fetch the generator bean from the application context
        SnowflakeIdGenerator generator = context.getBean(SnowflakeIdGenerator.class);
        return generator.nextId();
    }
}
