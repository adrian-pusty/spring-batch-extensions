package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.extensions.excel.mapping.BeanWrapperRowMapper;
import org.springframework.batch.extensions.excel.streaming.StreamingXlsxItemReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.net.URL;
import java.util.Objects;

@Configuration
@SpringBootApplication
@EnableBatchProcessing
@RequiredArgsConstructor
public class RowByIndexNotSupportedReprod
{
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobLauncher jobLauncher;
    public static void main(String[] args)
    {
        SpringApplication.run(RowByIndexNotSupportedReprod.class, args);
    }

    @Bean
    public JobExecution createJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException
    {
        Job job = jobBuilderFactory.get("a_job")
                .incrementer(new RunIdIncrementer())
                .flow(createStep())
                .end().build();
        return jobLauncher.run(job, new JobParameters());
    }

    public Step createStep()
    {
        BeanWrapperRowMapper<MyIn> mapper = new BeanWrapperRowMapper<>();
        mapper.setTargetType(MyIn.class);
        StreamingXlsxItemReader<MyIn> reader = new StreamingXlsxItemReader<>();

        FileSystemResource resource = resource("input.xlsx");

        reader.setResource(resource);
        reader.setRowMapper(mapper);
        reader.setLinesToSkip(1);

        return stepBuilderFactory.get("a_step")
                .<MyIn, MyOut>chunk(10)
                .reader(reader)
                .writer(System.out::println)
                .build();
    }

    private static FileSystemResource resource(String name)
    {
        URL url = RowByIndexNotSupportedReprod.class
                .getClassLoader()
                .getResource(name);

        String file = Objects.requireNonNull(url).getFile();

        return new FileSystemResource(file);
    }
}

class MyIn {}
class MyOut {}