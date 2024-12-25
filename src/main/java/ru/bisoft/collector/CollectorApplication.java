package ru.bisoft.collector;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import jakarta.xml.bind.Marshaller;
import ru.pfr.szbd_1_0_0.СЗБД;

@SpringBootApplication
public class CollectorApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(CollectorApplication.class, args);
		// JobLauncher launcher = context.getBean(JobLauncher.class);
		// Job job = context.getBean("job", Job.class);
		// launcher.run(job, new JobParametersBuilder().toJobParameters());

	}

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource")
	DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	Jaxb2Marshaller jaxb2Marshaller() {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setClassesToBeBound(СЗБД.class);
		Resource schema0 = new ClassPathResource("smev/szbd/1.0.0/commons/УнифТипы_2023-04-03.xsd");
		Resource schema1 = new ClassPathResource("smev/szbd/1.0.0/SINGLE_2023-04-03.xsd");
		jaxb2Marshaller.setSchemas(schema0, schema1);
		// jaxb2Marshaller.setMarshallerProperties(props);
		return jaxb2Marshaller;
	}
}
