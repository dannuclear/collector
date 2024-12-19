package ru.bisoft.collector.batch;

import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class Config {
    // private final JobBuilderFactory jobBuilderFactory;
    // private final StepBuilderFactory stepBuilderFactory;

    // @Bean
    // public Job exampleJob() {
    //     return jobBuilderFactory.get("exampleJob")
    //             .incrementer(new RunIdIncrementer())
    //             .flow(exampleStep())
    //             .end()
    //             .build();
    // }

    // @Bean
    // public Step exampleStep() {
    //     return stepBuilderFactory.get("exampleStep")
    //             .<String, String>chunk(10)
    //             .reader(exampleReader())
    //             .processor(exampleProcessor())
    //             .writer(exampleWriter())
    //             .build();
    // }

    // @Bean
    // public ItemReader<String> exampleReader() {
    //     // Implement your ItemReader here
    //     return new ExampleItemReader();
    // }

    // @Bean
    // public ItemProcessor<String, String> exampleProcessor() {
    //     // Implement your ItemProcessor here
    //     return new ExampleItemProcessor();
    // }

    // @Bean
    // public ItemWriter<String> exampleWriter() {
    //     // Implement your ItemWriter here
    //     return new ExampleItemWriter();
    // }
}
