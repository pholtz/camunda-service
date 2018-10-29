package com.scottstots.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class ProcessEngineConfig
{
	private final ResourceLoader resourceLoader;
	private final DataSource dataSource;
	private final DataSourceTransactionManager transactionManager;
	
	public ProcessEngineConfig(ResourceLoader resourceLoader,
			DataSource dataSource,
			DataSourceTransactionManager transactionManager) {
		this.resourceLoader = resourceLoader;
		this.dataSource = dataSource;
		this.transactionManager = transactionManager;
	}
	
	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration() throws IOException {
		SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
		configuration.setProcessEngineName("resq");
		configuration.setDeploymentResources(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath*:bpmn/**/*.bpmn"));
		configuration.setDataSource(dataSource);
		configuration.setTransactionManager(transactionManager);
//		configuration.setDatabaseSchemaUpdate("true");
//		configuration.setJobExecutorActivate(false);
		return configuration;
	}
	
	@Bean
	public ProcessEngineFactoryBean processEngineFactoryBean() throws IOException {
		ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
		processEngineFactoryBean.setProcessEngineConfiguration(this.springProcessEngineConfiguration());
		return processEngineFactoryBean;
	}
}
