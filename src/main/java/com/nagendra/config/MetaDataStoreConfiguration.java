package com.nagendra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.jdbc.metadata.JdbcMetadataStore;
import org.springframework.integration.metadata.MetadataStore;

import javax.sql.DataSource;


/**
 * Created by nagendra on 18/05/2018.
 */
public class MetaDataStoreConfiguration {

    /*
    CREATE TABLE INT_METADATA_STORE  (
        METADATA_KEY VARCHAR(255) NOT NULL,
        METADATA_VALUE VARCHAR(4000),
        REGION VARCHAR(100) NOT NULL,
        constraint METADATA_STORE primary key (METADATA_KEY, REGION)
    );
     */
    @Bean
    public MetadataStore metadataStore(DataSource dataSource){
        JdbcMetadataStore jdbcMetadataStore = new JdbcMetadataStore(dataSource);
        jdbcMetadataStore.setTablePrefix("INT_");
        return jdbcMetadataStore;
    }
}
