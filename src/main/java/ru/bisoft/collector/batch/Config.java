package ru.bisoft.collector.batch;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;

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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import ru.bisoft.collector.CollectorProperties;
import ru.bisoft.collector.domain.SZBDData;
import ru.pfr.szbd_1_0_0.СЗБД;
import ru.pfr.szbd_1_0_0.ТипДолжностноеЛицо;
import ru.pfr.szbd_1_0_0.ФИО;
import ru.pfr.szbd_1_0_0.СЗБД.БлокВключенияЗаписиВБанкДанных;
import ru.pfr.szbd_1_0_0.СЗБД.БлокВключенияЗаписиВБанкДанных.СведенияОВетеранахТруда;
import ru.pfr.szbd_1_0_0.СЗБД.БлокВключенияЗаписиВБанкДанных.СведенияОВетеранахТруда.ИнформацияОФЛ;
import ru.pfr.szbd_1_0_0.СЗБД.БлокВключенияЗаписиВБанкДанных.СведенияОВетеранахТруда.РешениеОПрисвоенииКатегорииВетеранаТруда;
import ru.pfr.szbd_1_0_0.СЗБД.БлокВключенияЗаписиВБанкДанных.СведенияОВетеранахТруда.ТрудовойСтаж;
import ru.pfr.szbd_1_0_0.СЗБД.БлокВключенияЗаписиВБанкДанных.СведенияОВетеранахТруда.УдостоверениеВетеранаТруда;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CollectorProperties.class)
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

    // @Bean
    // @StepScope
    // DataSource secondDataSource(@Value("#{jobParameters['url']}") String url) {
    // return DataSourceBuilder.create()
    // .driverClassName("org.postgresql.Driver")
    // .username("postgres")
    // .password("!Q12345")
    // .url(url)
    // .build();
    // }

    @Bean
    MultiJdbcItemReader<SZBDData> reader(CollectorProperties properties) {
        List<DataSource> dataSources = properties.getUrls().stream().map(url -> (DataSource) DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .username("postgres")
                .password("!Q12345")
                .url(url)
                .build()).toList();

        MultiJdbcItemReader<SZBDData> r = new MultiJdbcItemReader<>();
        // r.setDelegate(reader);
        r.setDataSources(dataSources.toArray(new DataSource[dataSources.size()]));
        return r;
    }

    // @Bean
    // @StepScope
    // JdbcCursorItemReader<SZBDData> singleReader() throws Exception {
    // return new JdbcCursorItemReaderBuilder<SZBDData>()
    // .saveState(false)
    // //.dataSource(dataSource)
    // .sql("SELECT key_person as id FROM Person limit 2")
    // .rowMapper(new BeanPropertyRowMapper<>(SZBDData.class))
    // .build();
    // }

    @Bean
    ItemProcessor<SZBDData, СЗБД> processor() {
        return new ItemProcessor<SZBDData, СЗБД>() {
            @Override
            public СЗБД process(SZBDData item) throws Exception {
                СЗБД szbd = new СЗБД();
                szbd.setИдЗаписиБанкаДанных(UUID.randomUUID().toString());
                szbd.setКодДействия(1);
                ТипДолжностноеЛицо сотрудник = new ТипДолжностноеЛицо();
                сотрудник.setДолжность("Сотрудник министерства");
                ФИО фиоСотрудник = new ФИО();
                фиоСотрудник.setИмя("Имя");
                фиоСотрудник.setФамилия("Фамилия");
                фиоСотрудник.setОтчество("Отчество");
                сотрудник.setФИО(фиоСотрудник);
                szbd.setСведенияОСотруднике(сотрудник);

                БлокВключенияЗаписиВБанкДанных блок = new БлокВключенияЗаписиВБанкДанных();
                СведенияОВетеранахТруда ветеран = new СведенияОВетеранахТруда();
                ИнформацияОФЛ информацияОФЛ = new ИнформацияОФЛ();
                информацияОФЛ.setДатаРождения(item.getBirthday());
                информацияОФЛ.setСНИЛС(item.getSnils());
                ФИО фио = new ФИО();
                фио.setФамилия(item.getSurname());
                фио.setИмя(item.getName());
                фио.setОтчество(item.getPatronymic());
                информацияОФЛ.setФИО(фио);
                ветеран.setИнформацияОФЛ(информацияОФЛ);

                РешениеОПрисвоенииКатегорииВетеранаТруда решение = new РешениеОПрисвоенииКатегорииВетеранаТруда();
                решение.setКатегория("009");
                решение.setНомер(defaultIfEmpty(item.getPaperNumber(), "0"));
                решение.setСерия(defaultIfEmpty(item.getPaperSeries(), "0"));
                решение.setСубъектРФ("09");
                решение.setДатаНачалаДействия(LocalDate.now());
                решение.setДатаОкончанияДействия(LocalDate.now());
                решение.setДатаРешения(defaultIfEmpty(item.getPaperIssueDate(), LocalDate.now()));
                решение.setОрганПринявшийРешение("Орган принявший решения");
                решение.setНормаПрава("Норма права");
                ветеран.setРешениеОПрисвоенииКатегорииВетеранаТруда(решение);

                УдостоверениеВетеранаТруда удостоверение = new УдостоверениеВетеранаТруда();
                удостоверение.setДатаВыдачи(defaultIfEmpty(item.getPaperIssueDate(), LocalDate.now()));
                удостоверение.setНомер(defaultIfEmpty(item.getPaperNumber(), "0"));
                удостоверение.setСерия(defaultIfEmpty(item.getPaperSeries(), "0"));
                удостоверение.setКодВидаУдостоверения(BigInteger.valueOf(1184L)); // https://esnsi.gosuslugi.ru/classifiers/12489/view/348
                удостоверение.setОрганВыдавшийУдостоверение(defaultIfEmpty(item.getPaperIssuer(), "Орган, выдавший удостоверение"));
                ветеран.setУдостоверениеВетеранаТруда(удостоверение);

                ТрудовойСтаж стаж = new ТрудовойСтаж();
                стаж.setКоличествоЛет(BigInteger.ZERO);
                ветеран.setТрудовойСтаж(стаж);
                
                блок.setСведенияОВетеранахТруда(ветеран);
                szbd.setБлокВключенияЗаписиВБанкДанных(блок);

                return szbd;
            }
        };
    }

    private String defaultIfEmpty (String value, String def) {
        return (value == null || value.isEmpty()) ? def : value;
    }

    private LocalDate defaultIfEmpty (LocalDate value, LocalDate def) {
        return value == null ? def : value;
    }

    @Bean
    ItemWriter<СЗБД> writer(DataSource dataSource,
            @Value("1.xml") FileSystemResource resource,
            Jaxb2Marshaller singleElementMarshaller) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("xmlns", "http://iis.ecp.ru/szbd/types/1.0.2");
        attributes.put("xmlns:ns2", "http://iis.ecp.ru/szbd/1.0.2");
        attributes.put("xmlns:ns3", "http://пф.рф/УТ/2023-04-03");
        attributes.put("xmlns:ns4", "http://пф.рф/СЗБД/2023-04-03");
        return new StaxEventItemWriterBuilder<СЗБД>()
                .name("xmlWriter")
                .resource(resource)
                .marshaller(singleElementMarshaller)
                // .rootElementAttributes(attributes)
                .rootTagName("!-- --")
                .footerCallback(writer -> {
                    XMLEventFactory eventFactory = XMLEventFactory.newInstance();
                    XMLEvent event = eventFactory.createEndElement(
                            "ns4",
                            "http://пф.рф/СЗБД/2023-04-03",
                            "ЭДСФР");
                    try {
                        writer.add(event);
                    } catch (XMLStreamException e) {
                    }
                })
                .headerCallback(writer -> {
                    // <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    // <ns4:ЭДСФР xmlns="http://iis.ecp.ru/szbd/types/1.0.2"
                    // xmlns:ns2="http://iis.ecp.ru/szbd/1.0.2"
                    // xmlns:ns3="http://пф.рф/УТ/2023-04-03"
                    // xmlns:ns4="http://пф.рф/СЗБД/2023-04-03">
                    XMLEventFactory eventFactory = XMLEventFactory.newInstance();
                    List<Namespace> namespaces = new ArrayList<>();
                    namespaces.add(eventFactory.createNamespace("xmlns", "http://iis.ecp.ru/szbd/types/1.0.2"));
                    namespaces.add(eventFactory.createNamespace("ns2", "http://iis.ecp.ru/szbd/1.0.2"));
                    namespaces.add(eventFactory.createNamespace("ns3", "http://пф.рф/УТ/2023-04-03"));
                    namespaces.add(eventFactory.createNamespace("ns4", "http://пф.рф/СЗБД/2023-04-03"));
                    XMLEvent event = eventFactory.createStartElement(
                            "ns4",
                            "http://пф.рф/СЗБД/2023-04-03",
                            "ЭДСФР", null, namespaces.iterator());
                    try {
                        writer.add(event);
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                })
                .build();
    }
}
