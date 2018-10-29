package com.scottstots.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatasourceConfig
{
	@Bean("camundaDatasource")
	@ConfigurationProperties(prefix = "datasource.camunda")
	public DataSource camundaDatasource() {
		return new HikariDataSource();
	}
	
	@Bean
	public DataSourceTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(this.camundaDatasource());
		return transactionManager;
	}
}
