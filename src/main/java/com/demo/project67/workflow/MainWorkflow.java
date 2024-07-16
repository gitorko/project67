package com.demo.project67.workflow;

import com.demo.project67.validator.JobParameterValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.JobStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Shows how jobs can be chained together
 *
 * 1 Parent Job & 4 Child Jobs
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class MainWorkflow {

    final JobRepository jobRepository;
    final Job flightStartJob;
    final Job hotelStartJob;
    final Job cabStartJob;
    final Job notificationStartJob;
    final JobParameterValidator jobParameterValidator;
    final PlatformTransactionManager transactionManager;

    @Bean(name = "mainJob")
    public Job mainWorkflow() {
        return new JobBuilder("mainJob", jobRepository)
                .validator(jobParameterValidator)
                .start(flightJob())
                .next(hotelJob())
                .next(cabJob())
                .next(notificationJob())
                .build();
    }

    public Step flightJob() {
        return new JobStepBuilder(new StepBuilder("flightJob", jobRepository))
                .job(flightStartJob)
                .build();
    }

    public Step hotelJob() {
        return new JobStepBuilder(new StepBuilder("hotelJob", jobRepository))
                .job(hotelStartJob)
                .build();
    }

    public Step cabJob() {
        return new JobStepBuilder(new StepBuilder("cabJob", jobRepository))
                .job(cabStartJob)
                .build();
    }

    public Step notificationJob() {
        return new JobStepBuilder(new StepBuilder("notificationJob", jobRepository))
                .job(notificationStartJob)
                .build();
    }

}
