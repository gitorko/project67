package com.demo.project67.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookCabStepListener {

    @BeforeStep
    public ExitStatus beforeStep(StepExecution stepExecution) {
        log.info("[LISTENER] Book Cab Step beforeStep!");
        return stepExecution.getExitStatus();
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("[LISTENER] Book Cab Step afterStep!");
        if (ExitStatus.COMPLETED.equals(stepExecution.getExitStatus())) {
            log.info("[LISTENER] Book Cab Step completed!");
        }
        return stepExecution.getExitStatus();
    }
}
