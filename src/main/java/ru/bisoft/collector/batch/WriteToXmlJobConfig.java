package ru.bisoft.collector.batch;

import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import ru.bisoft.collector.domain.SZBDData;
import ru.pfr.szbd_1_0_0.ЭДПФР;
import ru.pfr.szbd_1_0_0.ЭДПФР.СЗБД;
import ru.pfr.szbd_1_0_0.ЭДПФР.СЗБД.БлокВключенияЗаписиВБанкДанных.СведенияОВетеранахТруда;
import ru.pfr.szbd_1_0_0.ЭДПФР.СЗБД.БлокВключенияЗаписиВБанкДанных.СведенияОВетеранахТруда.ИнформацияОФЛ;

@Configuration
public class WriteToXmlJobConfig {

    @Bean
    Job exportToXmlJob(JobRepository jobRepository, Step exportToXmlStep) {
        return new JobBuilder("exportToXmlJob", jobRepository)
                .start(exportToXmlStep)
                .build();
    }

    Step exportToXmlStep(JobRepository jobRepository,
            DataSourceTransactionManager dataSourceTransactionManager,
            ItemReader<SZBDData> h2Reader) {
        return new StepBuilder("exportToXmlStep", jobRepository)
                .chunk(Integer.MAX_VALUE, dataSourceTransactionManager)
                .reader(h2Reader)
                .writer(null)
                .build();
    }

    @Bean
    JdbcCursorItemReader<SZBDData> h2Reader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<SZBDData>()
                .dataSource(dataSource)
                .beanRowMapper(SZBDData.class)
                .build();
    }

    @Bean
    ItemProcessor<SZBDData, СЗБД> h2ItemProcessor() {
        return new ItemProcessor<SZBDData, СЗБД>() {

            @Override
            @Nullable
            public СЗБД process(@NonNull SZBDData item) throws Exception {
                СЗБД szbd = new СЗБД();
                szbd.setИдЗаписиБанкаДанных(UUID.randomUUID().toString());
                СЗБД.БлокВключенияЗаписиВБанкДанных block = new СЗБД.БлокВключенияЗаписиВБанкДанных();
                
                СведенияОВетеранахТруда labor = new СведенияОВетеранахТруда();
                ИнформацияОФЛ ifoOFL = new ИнформацияОФЛ();
                labor.setИнформацияОФЛ(ifoOFL);
                block.setСведенияОВетеранахТруда(labor);

                szbd.setБлокВключенияЗаписиВБанкДанных(block);
                return szbd;
            }
        };
    }

    @Bean
    ItemWriter<СЗБД> xmlItemWriter(Jaxb2Marshaller marshaller) {
        return new ItemWriter<СЗБД>() {
            @Override
            public void write(@NonNull Chunk<? extends СЗБД> chunk) throws Exception {
                ЭДПФР edpfr = new ЭДПФР();
                
                //edpfr.getСЗБД().add(szbd);
            }
        };
    }
}
