package ru.bisoft.collector.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import lombok.RequiredArgsConstructor;
import ru.bisoft.collector.domain.SZBDData;
import ru.pfr.szbd_1_0_0.ЭДПФР;

@Configuration
@RequiredArgsConstructor
public class Config {

    @Bean
    Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .build();
    }

    @Bean
    Step step(JobRepository jobRepository,
            DataSourceTransactionManager dataSourceTransactionManager,
            ItemReader<SZBDData> reader,
            ItemProcessor<SZBDData, ЭДПФР.СЗБД> processor,
            ItemWriter<ЭДПФР.СЗБД> writer,
            TaskExecutor taskExecutor) {
        return new StepBuilder("step", jobRepository)
                .<SZBDData, ЭДПФР.СЗБД>chunk(3, dataSourceTransactionManager)
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
    JdbcCursorItemReader<SZBDData> reader(@Qualifier("secondDataSource") DataSource dataSource) throws Exception {
        return new JdbcCursorItemReaderBuilder<SZBDData>()
                .saveState(false)
                .dataSource(dataSource)
                .sql("SELECT * FROM Person")
                .rowMapper(new BeanPropertyRowMapper<>(SZBDData.class))
                .build();
    }

    @Bean
    ItemProcessor<SZBDData, ЭДПФР.СЗБД> processor() {
        return new ItemProcessor<SZBDData, ЭДПФР.СЗБД>() {
            @Override
            public ЭДПФР.СЗБД process(SZBDData item) throws Exception {
                return new ЭДПФР.СЗБД();
            }
        };
    }

    @Bean
    ItemWriter<ЭДПФР.СЗБД> writer(Jaxb2Marshaller jaxb2Marshaller,
            @Value("person.xml") FileSystemResource xmlFile) {
        return new StaxEventItemWriterBuilder<ЭДПФР.СЗБД>()
                .name("xmlWriter")
                .marshaller(jaxb2Marshaller)
                .resource(xmlFile)
                .rootTagName("root")
                .overwriteOutput(true)
                .build();

        // return new ItemWriter<SZBDData>() {
        // @Override
        // public void write(Chunk<? extends SZBDData> chunk) throws Exception {
        // System.out.println(Thread.currentThread().getName());
        // }
        // };
    }
}
