package com.demo.project67.workflow;

import java.time.LocalDateTime;

import com.demo.project67.domain.BookingEvent;
import com.demo.project67.listener.BookCabJobListener;
import com.demo.project67.listener.BookCabStepListener;
import com.demo.project67.repository.BookingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

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
    final BookingEventRepository bookingEventRepository;

    final BookCabStepListener bookCabTicketStepListener;
    final BookCabJobListener bookCabJobListener;

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
                .tasklet(bookCab(), transactionManager)
                .listener(bookCabTicketStepListener)
                .build();
    }

    @Transactional
    public Tasklet bookCab() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                log.info("Running bookCab");
                Long bookingId = (Long) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("bookingId");
                String customer = (String) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("customer");
                bookingEventRepository.save(
                        BookingEvent.builder()
                                .event("Cab Booked for customer " + customer)
                                .bookingId(bookingId)
                                .createdOn(LocalDateTime.now())
                                .build()
                );
                log.info("Booking Id in bookCab: " + bookingId);
                HelperUtil.delay();
                log.info("Completed bookCab");
                return RepeatStatus.FINISHED;
            }
        };
    }

}
