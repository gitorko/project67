package com.demo.project67.workflow;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.demo.project67.domain.BookingEvent;
import com.demo.project67.repository.BookingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
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
                .tasklet(confirmFlightTickets(), transactionManager)
                .build();
    }

    @Bean(name = "creditFlightRewardPointsStep")
    public Step creditFlightRewardPointsStep() {
        return new StepBuilder("creditFlightRewardPointsStep", jobRepository)
                .tasklet(creditFlightRewardPoints(), transactionManager)
                .build();
    }

    public Tasklet confirmFlightTickets() {
        return (contribution, chunkContext) -> {
            log.info("Running confirmFlightTickets");
            Long bookingId = (Long) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("bookingId");
            String customer = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("customer");
            bookingEventRepository.save(
                    BookingEvent.builder()
                            .event("Flight Booked for customer " + customer)
                            .bookingId(bookingId)
                            .createdOn(LocalDateTime.now())
                            .build()
            );
            log.info("Booking Id in confirmFlightTickets: " + bookingId);
            HelperUtil.delay();
            log.info("Completed confirmFlightTickets");
            return RepeatStatus.FINISHED;
        };
    }

    public Tasklet creditFlightRewardPoints() {
        return (contribution, chunkContext) -> {
            log.info("Running creditRewardPoints");
            TimeUnit.SECONDS.sleep(15);
            Long bookingId = (Long) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("bookingId");
            String customer = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("customer");
            bookingEventRepository.save(
                    BookingEvent.builder()
                            .event("Flight Reward Points added for customer " + customer)
                            .bookingId(bookingId)
                            .createdOn(LocalDateTime.now())
                            .build()
            );
            log.info("Booking Id in creditFlightRewardPoints: " + bookingId);
            HelperUtil.delay();
            log.info("Completed creditRewardPoints");
            return RepeatStatus.FINISHED;
        };
    }
}
