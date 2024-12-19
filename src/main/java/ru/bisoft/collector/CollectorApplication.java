package ru.bisoft.collector;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

import com.zaxxer.hikari.HikariDataSource;

@SpringBootApplication
public class CollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollectorApplication.class, args);
	}

	@Bean
	@ConfigurationProperties("spring.datasource.db1") 
	DataSource db1DataSource(){
		return DataSourceBuilder.create().build();
	}

	//@Bean
	//@ConfigurationProperties("spring.datasource.db2") 
	//DataSource db2DataSource(){
	//	return new HikariDataSource(); //DataSourceBuilder.create().build();
	//}
}
