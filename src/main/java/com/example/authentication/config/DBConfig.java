package com.example.authentication.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = {"com.example.authentication.persistence"},
        entityManagerFactoryRef = "authenticationEntityManagerFactory",
        transactionManagerRef = "authenticationTransactionManager"
)
@EnableTransactionManagement
@Slf4j
class DBConfig {

    @Autowired
    private Environment environment;

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean authenticationEntityManagerFactory(
            final EntityManagerFactoryBuilder builder,
            final @Qualifier("authenticationDatasource") DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory) {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("jdbc.authdb.hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", environment.getProperty("jdbc.authdb.hibernate.dialect"));
        // this is CRUCIAL to allow Hibernate Envers to inject dependencies into the RevisionInfoListener. DO NOT REMOVE
        properties.put("hibernate.resource.beans.container", new SpringBeanContainer(beanFactory));

        return builder
                .dataSource(dataSource)
                .packages("com.example.authentication.model")
                .persistenceUnit("authenticationDB")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager authenticationTransactionManager(
            @Qualifier("authenticationEntityManagerFactory") EntityManagerFactory authenticationEntityManagerFactory) {
        return new JpaTransactionManager(authenticationEntityManagerFactory);
    }
    @Bean(name = "authenticationDatasource")
    public DataSource authenticationDatasource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("jdbc.authdb.driverClassName"));
        dataSource.setUrl(environment.getProperty("jdbc.authdb.url"));
        dataSource.setUsername(environment.getProperty("jdbc.authdb.username"));
        dataSource.setPassword(environment.getProperty("jdbc.authdb.password"));

        log.info("DB connection: url=" + dataSource.getUrl() + ", user=" + dataSource.getUsername());
        return dataSource;
    }
}
