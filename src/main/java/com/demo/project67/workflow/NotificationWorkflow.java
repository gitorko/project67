package com.demo.project67.workflow;

import com.demo.project67.exception.NotificationExceptionHandler;
import com.demo.project67.task.FlightNotificationTask;
import com.demo.project67.task.HotelNotificationTask;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

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
    final HotelNotificationTask hotelNotificationTask;
    final FlightNotificationTask flightNotificationTask;
    final NotificationExceptionHandler notificationExceptionHandler;

    @Bean(name = "notificationStartJob")
    public Job notificationStartJob(Job sendFlightNotificationJob, Job sendHotelNotificationJob) {
        return new JobBuilder("notificationStartJob", jobRepository)
                .start(sendFlightNotificationJobStep(sendFlightNotificationJob))
                .next(sendHotelNotificationJobStep(sendHotelNotificationJob))
                .build();
    }

    private Step sendFlightNotificationJobStep(Job sendFlightNotificationJob) {
        return new JobStepBuilder(new StepBuilder("sendFlightNotificationJobStep", jobRepository))
                .job(sendFlightNotificationJob)
                .build();
    }

    private Step sendHotelNotificationJobStep(Job sendHotelNotificationJob) {
        return new JobStepBuilder(new StepBuilder("sendHotelNotificationJobStep", jobRepository))
                .job(sendHotelNotificationJob)
                .build();
    }

    @Bean(name = "sendFlightNotificationJob")
    public Job sendFlightNotificationJob(Step sendFlightNotificationStep) {
        return new JobBuilder("sendFlightNotificationJob", jobRepository)
                .start(sendFlightNotificationStep)
                .next(retryDecider())
                .from(retryDecider()).on("RETRY").to(sendFlightNotificationStep)
                .from(retryDecider()).on("COMPLETED").end()
                .end()
                .build();
    }

    @Bean(name = "sendHotelNotificationJob")
    public Job sendHotelNotificationJob(Step sendHotelNotificationStep) {
        return new JobBuilder("sendHotelNotificationJob", jobRepository)
                .start(sendHotelNotificationStep)
                .build();
    }

    @Bean(name = "sendFlightNotificationStep")
    public Step sendNotificationStep() {
        return new StepBuilder("sendFlightNotificationStep", jobRepository)
                .tasklet(flightNotificationTask.sendingFlightNotificationTask(), transactionManager)
                .exceptionHandler(notificationExceptionHandler)
                .build();
    }

    @Bean(name = "sendHotelNotificationStep")
    public Step sendHotelNotificationStep() {
        return new StepBuilder("sendHotelNotificationStep", jobRepository)
                .tasklet(hotelNotificationTask.sendHotelNotificationTask(), transactionManager)
                .exceptionHandler(notificationExceptionHandler)
                .build();
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
