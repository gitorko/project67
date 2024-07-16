package com.demo.project67.workflow;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import com.demo.project67.domain.BookingEvent;
import com.demo.project67.exception.NotificationExceptionHandler;
import com.demo.project67.repository.BookingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.JobStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shows 2 different way of doing retry
 *
 * 2 Jobs with 1 Step each.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class NotificationWorkflow {

    final JobRepository jobRepository;
    final PlatformTransactionManager transactionManager;
    final BookingEventRepository bookingEventRepository;
    final NotificationExceptionHandler notificationExceptionHandler;
    AtomicInteger notificationAttemptCounter = new AtomicInteger();
    AtomicInteger invoiceAttemptCounter = new AtomicInteger();

    @Bean(name = "notificationStartJob")
    public Job notificationStartJob(Job generateInvoiceJob, Job sendNotificationJob) {
        return new JobBuilder("notificationStartJob", jobRepository)
                .start(generateInvoiceJob(generateInvoiceJob))
                .next(sendNotificationJob(sendNotificationJob))
                .build();
    }

    public Step generateInvoiceJob(Job generateInvoiceJob) {
        return new JobStepBuilder(new StepBuilder("generateInvoiceJob", jobRepository))
                .job(generateInvoiceJob)
                .build();
    }

    public Step sendNotificationJob(Job sendNotificationJob) {
        return new JobStepBuilder(new StepBuilder("sendNotificationJob", jobRepository))
                .job(sendNotificationJob)
                .build();
    }

    @Bean(name = "generateInvoiceJob")
    public Job generateInvoiceJob(Step generateInvoiceStep) {
        return new JobBuilder("generateInvoiceJob", jobRepository)
                .start(generateInvoiceStep)
                .build();
    }

    @Bean(name = "generateInvoiceStep")
    public Step generateInvoiceStep() {
        return new StepBuilder("generateInvoiceStep", jobRepository)
                .tasklet(generateInvoice(), transactionManager)
                .exceptionHandler(notificationExceptionHandler)
                .build();
    }

    @Bean(name = "sendNotificationJob")
    public Job sendNotificationJob(Step sendNotificationStep) {
        return new JobBuilder("sendNotificationJob", jobRepository)
                .start(sendNotificationStep)
                .next(retryDecider())
                .from(retryDecider()).on("RETRY").to(sendNotificationStep)
                .from(retryDecider()).on("COMPLETED").end()
                .end()
                .build();
    }

    @Bean(name = "sendNotificationStep")
    public Step sendNotificationStep() {
        return new StepBuilder("sendNotificationStep", jobRepository)
                .tasklet(sendingNotification(), transactionManager)
                .exceptionHandler(notificationExceptionHandler)
                .build();
    }

    /**
     * Retry in flow
     */
    @Transactional
    public Tasklet sendingNotification() {
        return (contribution, chunkContext) -> {
            log.info("Running sendingNotification, Attempt: {}", notificationAttemptCounter.get());
            Long bookingId = (Long) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("bookingId");
            String customer = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("customer");
            bookingEventRepository.save(
                    BookingEvent.builder()
                            .event("Notification Sent to customer " + customer + ", Attempt: " + notificationAttemptCounter.get())
                            .bookingId(bookingId)
                            .createdOn(LocalDateTime.now())
                            .build()
            );
            log.info("Booking Id: {}, In sendingNotification, Attempt: {} ", bookingId, notificationAttemptCounter.get());
            HelperUtil.delay();
            //Simulate error for first 2 attempts
            if (notificationAttemptCounter.incrementAndGet() < 3) {
                log.error("Failed sendingNotification!");
                throw new RuntimeException("Notification Job Failure!");
            }
            log.info("Completed sendingNotification");
            return RepeatStatus.FINISHED;
        };
    }

    /**
     * Retry in code
     */
    @Transactional
    public Tasklet generateInvoice() {
        RetryTemplate retryTemplate = new RetryTemplate();
        // Configure the retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // Number of retry attempts
        retryTemplate.setRetryPolicy(retryPolicy);
        // Configure the backoff policy (optional)
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000); // Backoff period in milliseconds
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return (contribution, chunkContext) -> {
            return retryTemplate.execute(context -> {
                log.info("Running generateInvoice, Attempt: {}", invoiceAttemptCounter.get());
                Long bookingId = (Long) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("bookingId");
                String customer = (String) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("customer");
                bookingEventRepository.save(
                        BookingEvent.builder()
                                .event("Invoice generated for customer " + customer + ", Attempt: " + invoiceAttemptCounter.get())
                                .bookingId(bookingId)
                                .createdOn(LocalDateTime.now())
                                .build()
                );
                log.info("Booking Id: {}, In generateInvoice, Attempt: {} ", bookingId, invoiceAttemptCounter.get());
                HelperUtil.delay();
                //Simulate error for first 2 attempts
                if (invoiceAttemptCounter.incrementAndGet() < 3) {
                    log.error("Failed generateInvoice!");
                    throw new RuntimeException("Invoice Job Failure!");
                }
                log.info("Completed generateInvoice");
                return RepeatStatus.FINISHED;
            });
        };
    }

    private JobExecutionDecider retryDecider() {
        return (jobExecution, stepExecution) -> {
            if (stepExecution.getStatus() == BatchStatus.FAILED) {
                return new FlowExecutionStatus("RETRY");
            }
            return new FlowExecutionStatus("COMPLETED");
        };
    }
}
