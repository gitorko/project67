package com.demo.project67.workflow;

import com.demo.project67.repository.BookingEventRepository;
import com.demo.project67.task.BookHotelTask;
import com.demo.project67.task.HotelRewardPointsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

    final PlatformTransactionManager transactionManager;
    final BookingEventRepository bookingEventRepository;
    final JobRepository jobRepository;
    final BookHotelTask bookHotelTask;
    final HotelRewardPointsTask hotelRewardPointsTask;

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
                .tasklet(bookHotelTask.bookHotel(), transactionManager)
                .build();
    }

    @Bean(name = "creditHotelRewardPointsStep")
    public Step creditHotelRewardPointsStep() {
        return new StepBuilder("creditHotelRewardPointsStep", jobRepository)
                .tasklet(hotelRewardPointsTask.creditHotelRewardPoints(), transactionManager)
                .build();
    }

}
