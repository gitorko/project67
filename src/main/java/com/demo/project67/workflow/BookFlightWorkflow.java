package com.demo.project67.workflow;

import com.demo.project67.repository.BookingEventRepository;
import com.demo.project67.task.BookFlightTask;
import com.demo.project67.task.FlightRewardPointsTask;
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
 * Shows how a single job can have many steps
 *
 * 1 Job with 2 Steps
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class BookFlightWorkflow {

    final JobRepository jobRepository;
    final PlatformTransactionManager transactionManager;
    final BookingEventRepository bookingEventRepository;
    final BookFlightTask bookFlightTask;
    final FlightRewardPointsTask flightRewardPointsTask;

    @Bean(name = "flightStartJob")
    public Job flightStartJob(Step bookFlightTicketStep, Step creditFlightRewardPointsStep) {
        return new JobBuilder("flightStartJob", jobRepository)
                .start(bookFlightTicketStep)
                .incrementer(new RunIdIncrementer())
                .next(creditFlightRewardPointsStep)
                .build();
    }

    @Bean(name = "bookFlightTicketStep")
    public Step bookFlightTicketStep() {
        return new StepBuilder("bookFlightTicketStep", jobRepository)
                .tasklet(bookFlightTask.bookFlight(), transactionManager)
                .build();
    }

    @Bean(name = "creditFlightRewardPointsStep")
    public Step creditFlightRewardPointsStep() {
        return new StepBuilder("creditFlightRewardPointsStep", jobRepository)
                .tasklet(flightRewardPointsTask.creditFlightRewardPoints(), transactionManager)
                .build();
    }

}
