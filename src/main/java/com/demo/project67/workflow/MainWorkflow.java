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
    final JobParameterValidator jobParameterValidator;
    final PlatformTransactionManager transactionManager;

    @Bean(name = "mainJob")
    public Job mainWorkflow(Job flightStartJob, Job hotelStartJob, Job cabStartJob, Job notificationStartJob) {
        return new JobBuilder("mainJob", jobRepository)
                .validator(jobParameterValidator)
                .start(flightJob(flightStartJob))
                .next(hotelJob(hotelStartJob))
                .next(cabJob(cabStartJob))
                .next(notificationJob(notificationStartJob))
                .build();
    }

    public Step flightJob(Job flightStartJob) {
        return new JobStepBuilder(new StepBuilder("flightJob", jobRepository))
                .job(flightStartJob)
                .build();
    }

    public Step hotelJob(Job hotelStartJob) {
        return new JobStepBuilder(new StepBuilder("hotelJob", jobRepository))
                .job(hotelStartJob)
                .build();
    }

    public Step cabJob(Job cabStartJob) {
        return new JobStepBuilder(new StepBuilder("cabJob", jobRepository))
                .job(cabStartJob)
                .build();
    }

    public Step notificationJob(Job notificationStartJob) {
        return new JobStepBuilder(new StepBuilder("notificationJob", jobRepository))
                .job(notificationStartJob)
                .build();
    }

}
