package com.mw.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import com.mw.plugins.jdbc.JdbcDao;

@Configuration
public class DataSourceConfig {

	// ======================================hraux======================================
	@Bean
	@Primary
	@ConfigurationProperties("hraux.datasource")
	public DataSourceProperties hrauxDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	@ConfigurationProperties("hraux.datasource")
	@Qualifier("hrauxDataSource")
	public DataSource hrauxDataSource() {
		return hrauxDataSourceProperties().initializeDataSourceBuilder().build();
	}

	@Bean(name = "hrauxJdbcTemplate")
	@Qualifier("hrauxJdbcTemplate")
	public NamedParameterJdbcTemplate hrauxJdbcTemplate(@Qualifier("hrauxDataSource") DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	@Bean(name = "jdbcDao")
	public JdbcDao hrauxJdbcDao(@Qualifier("hrauxJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		return new JdbcDao(namedParameterJdbcTemplate);
	}

	@Bean
	@Scope("prototype")
	public SimpleJdbcCall hrauxJdbcCall() {
		return new SimpleJdbcCall(hrauxDataSource());
	}
	
}
