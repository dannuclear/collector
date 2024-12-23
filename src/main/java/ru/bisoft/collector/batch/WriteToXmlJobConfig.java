package ru.bisoft.collector.batch;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WriteToXmlJobConfig {

    // @Bean
    // Job exportToXmlJob(JobRepository jobRepository, Step exportToXmlStep) {
    //     return new JobBuilder("exportToXmlJob", jobRepository)
    //             .start(exportToXmlStep)
    //             .build();
    // }

    // Step exportToXmlStep(JobRepository jobRepository,
    //         DataSourceTransactionManager dataSourceTransactionManager,
    //         ItemReader<SZBDData> h2Reader) {
    //     return new StepBuilder("exportToXmlStep", jobRepository)
    //             .chunk(Integer.MAX_VALUE, dataSourceTransactionManager)
    //             .reader(h2Reader)
    //             .writer(null)
    //             .build();
    // }

    // @Bean
    // JdbcCursorItemReader<SZBDData> h2Reader(DataSource dataSource) {
    //     return new JdbcCursorItemReaderBuilder<SZBDData>()
    //             .dataSource(dataSource)
    //             .beanRowMapper(SZBDData.class)
    //             .build();
    // }

    // @Bean
    // ItemProcessor<SZBDData, СЗБД> h2ItemProcessor() {
    //     return new ItemProcessor<SZBDData, СЗБД>() {

    //         @Override
    //         @Nullable
    //         public СЗБД process(@NonNull SZBDData item) throws Exception {
    //             СЗБД szbd = new СЗБД();
    //             szbd.setИдЗаписиБанкаДанных(UUID.randomUUID().toString());
    //             СЗБД.БлокВключенияЗаписиВБанкДанных block = new СЗБД.БлокВключенияЗаписиВБанкДанных();

    //             СведенияОВетеранахТруда labor = new СведенияОВетеранахТруда();
    //             ИнформацияОФЛ ifoOFL = new ИнформацияОФЛ();
    //             labor.setИнформацияОФЛ(ifoOFL);
    //             block.setСведенияОВетеранахТруда(labor);

    //             szbd.setБлокВключенияЗаписиВБанкДанных(block);
    //             return szbd;
    //         }
    //     };
    // }

    // @Bean
    // ItemWriter<СЗБД> xmlItemWriter(Jaxb2Marshaller marshaller) {
    //     return new ItemWriter<СЗБД>() {
    //         @Override
    //         public void write(@NonNull Chunk<? extends СЗБД> chunk) throws Exception {
    //             ЭДПФР edpfr = new ЭДПФР();

    //             // edpfr.getСЗБД().add(szbd);
    //         }
    //     };
    // }
}
