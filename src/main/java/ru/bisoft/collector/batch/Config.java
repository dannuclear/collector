package ru.bisoft.collector.batch;

import java.util.Collection;

import javax.sql.DataSource;

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
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import lombok.RequiredArgsConstructor;
import ru.bisoft.collector.domain.SZBDData;

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
    JobExecutionListener jobExecutionListener (){
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
            // ItemProcessor<SZBDData, SZBDData> processor,
            ItemWriter<SZBDData> writer,
            TaskExecutor taskExecutor) {
        return new StepBuilder("step", jobRepository)
                .<SZBDData, SZBDData>chunk(500, dataSourceTransactionManager)
                .reader(reader)
                // .processor(processor)
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
    JdbcCursorItemReader<SZBDData> reader(@Qualifier("secondDataSource") DataSource dataSource) throws Exception {
        return new JdbcCursorItemReaderBuilder<SZBDData>()
                .saveState(false)
                .dataSource(dataSource)
                .sql("SELECT key_person as id FROM Person limit 1000")
                .rowMapper(new BeanPropertyRowMapper<>(SZBDData.class))
                .build();
    }

    // @Bean
    // ItemProcessor<SZBDData, SZBDData> processor() {
    //     return new ItemProcessor<SZBDData, SZBDData>() {
    //         @Override
    //         public SZBDData process(SZBDData item) throws Exception {
    //             return item;
    //         }
    //     };
    // }

    @Bean
    ItemWriter<SZBDData> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<SZBDData>()
                .dataSource(dataSource)
                .sql("INSERT INTO SZBDData (id) VALUES (:id)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
}
