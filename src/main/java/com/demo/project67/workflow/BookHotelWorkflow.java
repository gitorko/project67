package com.demo.project67.workflow;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.demo.project67.domain.BookingEvent;
import com.demo.project67.repository.BookingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Shows Flow `FlowBuilder` that can define if-else scenarios
 *
 * 1 Job with 2 Steps
 */

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BookHotelWorkflow {

    final BookingEventRepository bookingEventRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "hotelStartJob")
    public Job hotelStartJob(Flow bookHotelFlow) {
        return new JobBuilder("hotelStartJob", jobRepository)
                .start(bookHotelFlow)
                .end()
                .build();
    }

    /**
     * This provides decision logic
     */
    @Bean(name = "bookHotelFlow")
    public Flow bookHotelFlow(Step bookHotelStep, Step creditHotelRewardPointsStep) {
        return new FlowBuilder<Flow>("bookHotelFlow")
                .start(bookHotelStep)
                .on(BatchStatus.FAILED.toString()).end()
                .from(bookHotelStep).on("*").to(creditHotelRewardPointsStep)
                .end();
    }

    @Bean(name = "bookHotelStep")
    public Step bookHotelStep() {
        return new StepBuilder("bookHotelStep", jobRepository)
                .tasklet(confirmHotel(), transactionManager)
                .build();
    }

    @Bean(name = "creditHotelRewardPointsStep")
    public Step creditHotelRewardPointsStep() {
        return new StepBuilder("creditHotelRewardPointsStep", jobRepository)
                .tasklet(creditHotelRewardPoints(), transactionManager)
                .build();
    }

    public Tasklet confirmHotel() {
        return (contribution, chunkContext) -> {
            log.info("Running confirmHotel");
            TimeUnit.SECONDS.sleep(15);
            Long bookingId = (Long) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("bookingId");
            String customer = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("customer");
            bookingEventRepository.save(
                    BookingEvent.builder()
                            .event("Hotel Booked for customer " + customer)
                            .bookingId(bookingId)
                            .createdOn(LocalDateTime.now())
                            .build()
            );
            log.info("Booking Id in confirmHotel: " + bookingId);
            HelperUtil.delay();
            log.info("Completed confirmHotel");
            return RepeatStatus.FINISHED;
        };
    }

    public Tasklet creditHotelRewardPoints() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                log.info("Running creditHotelRewardPoints");
                TimeUnit.SECONDS.sleep(15);
                Long bookingId = (Long) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("bookingId");
                String customer = (String) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("customer");
                bookingEventRepository.save(
                        BookingEvent.builder()
                                .event("Hotel Reward points added for customer " + customer)
                                .bookingId(bookingId)
                                .createdOn(LocalDateTime.now())
                                .build()
                );
                log.info("Booking Id in creditHotelRewardPoints: " + bookingId);
                HelperUtil.delay();
                log.info("Completed creditHotelRewardPoints");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
