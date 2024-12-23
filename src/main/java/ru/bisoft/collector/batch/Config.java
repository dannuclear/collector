package ru.bisoft.collector.batch;

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import ru.bisoft.collector.Url;
import ru.bisoft.collector.domain.SZBDData;
import ru.pfr.szbd_1_0_0.СЗБД;

@Configuration
@RequiredArgsConstructor
public class Config {
    static final Logger log = LoggerFactory.getLogger(Config.class);

    @Bean
    Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .listener(jobExecutionListener())
                .build();
    }

    @Bean
    JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
                for (StepExecution stepExecution : stepExecutions) {
                    log.info("\nКоличество прочитаных записей: {}\n", stepExecution.getReadCount());
                }

            }
        };
    }

    @Bean
    Step step(JobRepository jobRepository,
            DataSourceTransactionManager dataSourceTransactionManager,
            ItemReader<SZBDData> reader,
            ItemProcessor<SZBDData, СЗБД> processor,
            ItemWriter<СЗБД> writer,
            TaskExecutor taskExecutor) {
        return new StepBuilder("step", jobRepository)
                .<SZBDData, СЗБД>chunk(500, dataSourceTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                // .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    @StepScope
    DataSource secondDataSource(@Value("#{jobParameters['url']}") String url) {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .username("postgres")
                .password("!Q12345")
                .url(url)
                .build();
    }

    @Bean
    @StepScope
    MultiJdbcItemReader<SZBDData> reader(JdbcCursorItemReader<SZBDData> reader, Url urls) throws Exception {
        MultiJdbcItemReader<SZBDData> r = new MultiJdbcItemReader<>();
        r.setDelegate(reader);
        return r;
    }

    @Bean
    @StepScope
    JdbcCursorItemReader<SZBDData> singleReader(@Qualifier("secondDataSource") DataSource dataSource) throws Exception {
        return new JdbcCursorItemReaderBuilder<SZBDData>()
                .saveState(false)
                .dataSource(dataSource)
                .sql("SELECT key_person as id FROM Person limit 2")
                .rowMapper(new BeanPropertyRowMapper<>(SZBDData.class))
                .build();
    }

    @Bean
    ItemProcessor<SZBDData, СЗБД> processor() {
        return new ItemProcessor<SZBDData, СЗБД>() {
            @Override
            public СЗБД process(SZBDData item) throws Exception {
                СЗБД szbd = new СЗБД();
                return szbd;
            }
        };
    }

    @Bean
    ItemWriter<СЗБД> writer(DataSource dataSource,
            @Value("1.xml") FileSystemResource resource,
            Jaxb2Marshaller singleElementMarshaller) {
        return new StaxEventItemWriterBuilder<СЗБД>()
                .name("xmlWriter")
                .resource(resource)
                .marshaller(singleElementMarshaller)
                .rootTagName("root")
                .build();

        // return new JdbcBatchItemWriterBuilder<SZBDData>()
        // .dataSource(dataSource)
        // .sql("INSERT INTO SZBDData (id) VALUES (:id)")
        // .itemSqlParameterSourceProvider(new
        // BeanPropertyItemSqlParameterSourceProvider<>())
        // .build();
    }
}
