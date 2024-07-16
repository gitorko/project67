package com.demo.project67.service;

import java.time.Instant;

import com.demo.project67.domain.Booking;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobService {

    final JobLauncher jobLauncher;
    final Job mainJob;

    @SneakyThrows
    @Async
    public void startAsyncJob(Booking booking) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("bookingId", booking.getId())
                .addString("customer", booking.getCustomer())
                .addLong("time", Instant.now().toEpochMilli())
                .toJobParameters();
        jobLauncher.run(mainJob, jobParameters);
    }
}
