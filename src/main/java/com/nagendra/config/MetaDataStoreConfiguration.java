package com.nagendra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.jdbc.metadata.JdbcMetadataStore;
import org.springframework.integration.metadata.MetadataStore;

import javax.sql.DataSource;


/**
 * Created by nagendra on 18/05/2018.
 */
public class MetaDataStoreConfiguration {

    @Bean
    public MetadataStore metadataStore(DataSource dataSource){
        JdbcMetadataStore jdbcMetadataStore = new JdbcMetadataStore(dataSource);
        jdbcMetadataStore.setTablePrefix("INT_");
        return jdbcMetadataStore;
    }
}
