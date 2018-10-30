package com.scottstots.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
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
	
	@Bean("bpmnResources")
	public Resource[] bpmnResources() throws IOException {
		return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath*:bpmn/**/*.bpmn");
	}
	
	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration() throws IOException {
		SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
		configuration.setProcessEngineName("resq");
		configuration.setDeploymentResources(this.bpmnResources());
		configuration.setDataSource(dataSource);
		configuration.setTransactionManager(transactionManager);
		return configuration;
	}
	
	@Bean
	public ProcessEngineFactoryBean processEngineFactoryBean() throws IOException {
		ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
		processEngineFactoryBean.setProcessEngineConfiguration(this.springProcessEngineConfiguration());
		return processEngineFactoryBean;
	}
	
	@Bean("bpmnResq")
	public BpmnModelInstance bpmnResq() throws IOException {
		Resource[] resources = this.bpmnResources();
		for(int idx = 0; idx < resources.length; idx++) {
			if(resources[idx].getFilename().contains("resq.bpmn")) {
				return Bpmn.readModelFromStream(resources[idx].getInputStream());	
			}
		}
		return null;
	}
}
