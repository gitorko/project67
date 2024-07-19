package com.demo.project67.workflow;

import com.demo.project67.listener.BookCabJobListener;
import com.demo.project67.listener.BookCabStepListener;
import com.demo.project67.task.BookCabTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Shows how listeners can be added
 *
 * 1 Job with 1 Step
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class BookCabWorkflow {

    final JobRepository jobRepository;
    final PlatformTransactionManager transactionManager;
    final BookCabStepListener bookCabTicketStepListener;
    final BookCabJobListener bookCabJobListener;
    final BookCabTask bookCabTask;

    @Bean(name = "cabStartJob")
    public Job cabStartJob(Step bookCabStep) {
        return new JobBuilder("cabStartJob", jobRepository)
                .start(bookCabStep)
                .incrementer(new RunIdIncrementer())
                .listener(bookCabJobListener)
                .build();
    }

    @Bean(name = "bookCabStep")
    public Step bookCabStep() {
        return new StepBuilder("bookCabStep", jobRepository)
                .tasklet(bookCabTask.bookCab(), transactionManager)
                .listener(bookCabTicketStepListener)
                .build();
    }

}
