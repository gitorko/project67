package com.demo.project67.controller;

import lombok.SneakyThrows;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobExplorer jobExplorer;

    @SneakyThrows
    @PostMapping("/restart/{jobInstanceId}")
    public String restartJob(@PathVariable("jobInstanceId") Long jobInstanceId) {
        try {
            // Restart the job instance identified by jobInstanceId
            long newJobExecutionId = jobOperator.restart(jobInstanceId);
            return "Job restarted successfully. New JobExecutionId: " + newJobExecutionId;
        } catch (JobInstanceAlreadyCompleteException | JobRestartException | JobParametersInvalidException e) {
            return "Failed to restart job. Reason: " + e.getMessage();
        }
    }

    @GetMapping("/status/{jobExecutionId}")
    public BatchStatus getJobStatus(@PathVariable("jobExecutionId") Long jobExecutionId) {
        try {
            // Retrieve the JobExecution using JobExplorer
            return jobExplorer.getJobExecution(jobExecutionId).getStatus();
        } catch (Exception e) {
            return BatchStatus.UNKNOWN;
        }
    }
}
