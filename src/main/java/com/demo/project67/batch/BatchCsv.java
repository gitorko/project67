package com.demo.project67.batch;

import java.util.Arrays;

import com.demo.project67.domain.Employee;
import com.demo.project67.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchCsv {
    final JobRepository jobRepository;
    final PlatformTransactionManager transactionManager;
    final EntityManager entityManager;
    final EmployeeRepository employeeRepository;

    @Bean(name = "reader")
    public FlatFileItemReader<Employee> reader() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setNames("id", "name", "department", "salary", "employmentType");

        return new FlatFileItemReaderBuilder<Employee>()
                .name("employeeItemReader")
                .resource(new ClassPathResource("data/employees.csv"))
                .linesToSkip(1)
                .lineTokenizer(lineTokenizer)
                .fieldSetMapper(new EmployeeFieldSetMapper())
                .build();
    }

    @Bean(name = "processor")
    public EmployeeProcessor processor() {
        return new EmployeeProcessor();
    }

    @Bean(name = "csvWriter")
    public FlatFileItemWriter<Employee> csvWriter() {
        return new FlatFileItemWriterBuilder<Employee>()
                .name("employeeItemWriter")
                .resource(new FileSystemResource("/tmp/processed_employees.csv"))
                .delimited()
                .delimiter(",")
                .names(new String[]{"id", "name", "department", "salary", "employmentType"})
                .build();
    }

    @Bean(name = "jpaWriter")
    public JpaItemWriter<Employee> jpaWriter() {
        JpaItemWriter<Employee> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        return jpaItemWriter;
    }

    /**
     * Writes to both filesystem & db
     */
    @Bean(name = "compositeWriter")
    public CompositeItemWriter<Employee> compositeWriter(JpaItemWriter<Employee> jpaWriter, FlatFileItemWriter<Employee> csvWriter) {
        return new CompositeItemWriterBuilder<Employee>()
                .delegates(Arrays.asList(jpaWriter, csvWriter))
                .build();
    }

    @Bean(name = "employeeJob")
    public Job employeeJob(Step employeeStep) {
        return new JobBuilder("employeeJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(employeeStep)
                .build();
    }

    @Bean(name = "employeeStep")
    public Step employeeStep() {
        return new StepBuilder("employeeStep", jobRepository)
                .<Employee, Employee>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(compositeWriter(jpaWriter(), csvWriter()))
                .build();
    }

}
