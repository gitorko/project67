package com.demo.project67.service;

import java.time.LocalDateTime;

import com.demo.project67.domain.Booking;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {

    final JobLauncher jobLauncher;
    final Job mainJob;
    final Job employeeJob;

    @SneakyThrows
    @Async
    public void startAsyncTravelJob(Booking booking) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("bookingId", booking.getId())
                .addString("customer", booking.getCustomer())
                .addLocalDateTime("time", LocalDateTime.now())
                .toJobParameters();
        JobExecution run = jobLauncher.run(mainJob, jobParameters);
        log.info("Job Id: {}", run.getJobInstance().getInstanceId());
    }

    @SneakyThrows
    @Async
    public void startAsyncEmployeeJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("time", LocalDateTime.now())
                .toJobParameters();
        JobExecution run = jobLauncher.run(employeeJob, jobParameters);
        log.info("Job Id: {}", run.getJobInstance().getInstanceId());
    }
}
