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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import jakarta.xml.bind.Marshaller;
import ru.pfr.szbd_1_0_0.СЗБД;

@SpringBootApplication
public class CollectorApplication {

	public static void main(String[] args) throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		ApplicationContext context = SpringApplication.run(CollectorApplication.class, args);
		JobLauncher launcher = context.getBean(JobLauncher.class);
		Job job = context.getBean("job", Job.class);
		// Job exportToXmlJob = context.getBean("exportToXmlJob", Job.class);

		JobParameters parameters = new JobParametersBuilder()
				.addLong("id", 1L)
				.addString("url", "jdbc:postgresql://192.168.23.227:5432/kchrr")
				// .addString("url", "jdbc:postgresql://bi-soft.ru:25432/kchrr")
				.toJobParameters();
		launcher.run(job, parameters);

		parameters = new JobParametersBuilder()
		.addLong("id", 2L)
		.addString("url", "jdbc:postgresql://192.168.23.227:5432/kchrr")
		// .addString("url", "jdbc:postgresql://bi-soft.ru:25432/kchrr")
		.toJobParameters();

		launcher.run(job, parameters);

		// parameters = new JobParametersBuilder()
		// .addLong("id", 100L)
		// .toJobParameters();
		// launcher.run(exportToXmlJob, parameters);

		// parameters = new JobParametersBuilder()
		// .addLong("id", 2L)
		// .addString("url", "jdbc:postgresql://bi-soft.ru:25432/kchrr")
		// .toJobParameters();
		// launcher.run(job, parameters);
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
		//jaxb2Marshaller.setMarshallerProperties(props);
		return jaxb2Marshaller;
	}

	// @Bean
	// @ConfigurationProperties("spring.datasource.db1")
	// DataSource db1DataSource(){
	// return DataSourceBuilder.create().build();
	// }

	// @Bean
	// @ConfigurationProperties("spring.datasource.db2")
	// DataSource db2DataSource(){
	// return DataSourceBuilder.create().build();
	// }
}
