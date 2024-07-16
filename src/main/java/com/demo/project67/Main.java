package com.demo.project67;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableBatchProcessing
@EnableAsync
@Slf4j
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner onStart(JobOperator jobOperator, JobExplorer jobExplorer, JobRepository jobRepository) {
        return (args) -> {
            log.info("Identifying all stuck jobs & restarting!");
            identifyStuckJobs(jobOperator, jobExplorer, jobRepository, "bookFlight");
        };
    }

    private void identifyStuckJobs(JobOperator jobOperator, JobExplorer jobExplorer, JobRepository jobRepository, String jobName) {
        Set<JobExecution> allRunningJobExecutions = jobExplorer.findRunningJobExecutions(jobName);
        for (JobExecution jobExecution : allRunningJobExecutions) {
            if (isStuck(jobExecution)) {
                log.info("Stuck job found: {}, {}", jobExecution.getId(), jobExecution.getJobInstance().getJobName());
                restartJob(jobExecution, jobOperator);
            }
        }
    }

    private void markJobAsFailed(JobExecution jobExecution, JobRepository jobRepository) {
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setEndTime(LocalDateTime.now());
        jobExecution.setExitStatus(ExitStatus.FAILED);
        jobRepository.update(jobExecution);
    }

    @SneakyThrows
    private void restartJob(JobExecution jobExecution, JobOperator jobOperator) {
        long newJobExecutionId = jobOperator.restart(jobExecution.getId());
        log.info("Restarted stuck job: {}", jobExecution.getId());
    }

    @SneakyThrows
    private void stopJob(JobExecution jobExecution, JobOperator jobOperator) {
        boolean status = jobOperator.stop(jobExecution.getId());
        log.info("Stopped stuck job: {}, status: {}", jobExecution.getId(), status);
    }

    private boolean isStuck(JobExecution jobExecution) {
        //Assuming all jobs complete within 30 seconds. we use this to determine if a job is stuck
        return jobExecution.getStatus() == BatchStatus.STARTED &&
                Duration.between(jobExecution.getStartTime(), LocalDateTime.now()).toSeconds() > 30;
    }

}
