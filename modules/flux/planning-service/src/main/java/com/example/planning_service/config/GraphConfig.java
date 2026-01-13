package com.example.planning_service.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * Configuration for Neo4j graph database with connection pool limits
 * to prevent SSH connection issues
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "com.example.planning_service.repository.graph", transactionManagerRef = "neo4jTransactionManager")
public class GraphConfig {

    @Value("${spring.neo4j.uri}")
    private String neo4jUri;

    @Value("${spring.neo4j.authentication.username}")
    private String username;

    @Value("${spring.neo4j.authentication.password}")
    private String password;

    @Bean
    public Driver neo4jDriver() {
        Config config = Config.builder()
                .withMaxConnectionLifetime(30, java.util.concurrent.TimeUnit.MINUTES)
                .withMaxConnectionPoolSize(5)   // Further reduced to prevent SSH overload during build
                .withConnectionAcquisitionTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .withConnectionTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        return GraphDatabase.driver(neo4jUri, AuthTokens.basic(username, password), config);
    }

    @Bean("neo4jTransactionManager")
    public Neo4jTransactionManager neo4jTransactionManager(Driver driver,
            DatabaseSelectionProvider databaseNameProvider) {
        return new Neo4jTransactionManager(driver, databaseNameProvider);
    }
}
