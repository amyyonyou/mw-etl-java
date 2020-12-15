package com.mw.config;

import javax.inject.Inject;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DataScadaDataConfig {

	@Inject
	private DataSource dataSourceScada;

	@Bean
	public NamedParameterJdbcTemplate scada() {
		return new NamedParameterJdbcTemplate(dataSourceScada);
	}

	@Bean
	public PlatformTransactionManager transactionManagerScada() {
		return new DataSourceTransactionManager(dataSourceScada);
	}

	@Bean
	@Scope("prototype")
	public SimpleJdbcCall simpleJdbcCallScada() {
		return new SimpleJdbcCall(dataSourceScada);
	}

	@Configuration
	static class Standard {

		@Inject
		private Environment environment;

		@Bean(destroyMethod = "close")
		public DataSource dataSourceYonYou() {
			DataSource dataSource = new DataSource();
			dataSource.setDriverClassName(environment.getProperty("scada.datasource.driver-class-name"));
			dataSource.setUrl(environment.getProperty("scada.datasource.url"));
			dataSource.setUsername(environment.getProperty("scada.datasource.username"));
			dataSource.setPassword(environment.getProperty("scada.datasource.password"));
			dataSource.setMaxActive(Integer.parseInt(environment.getProperty("scada.pool.maxIdle")));
			dataSource.setMaxIdle(Integer.parseInt(environment.getProperty("scada.pool.maxActive")));

			dataSource.setTestWhileIdle(true);
			dataSource.setTestOnBorrow(true);
			dataSource.setValidationQuery("select 1 from dual");//select 1
			dataSource.setTestOnReturn(false);
			dataSource.setValidationInterval(30000);
			dataSource.setTimeBetweenEvictionRunsMillis(30000);
			dataSource.setRemoveAbandonedTimeout(60);
			dataSource.setMinEvictableIdleTimeMillis(30000);
			dataSource.setLogAbandoned(true);
			dataSource.setRemoveAbandoned(true);

			return dataSource;
		}
	}
}
