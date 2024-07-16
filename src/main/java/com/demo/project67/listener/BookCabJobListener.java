package com.demo.project67.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookCabJobListener {

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        log.info("[LISTENER] Book Cab Job beforeJob!");
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
            log.info("[LISTENER] Book Cab Job afterJob!");
        }
    }
}
