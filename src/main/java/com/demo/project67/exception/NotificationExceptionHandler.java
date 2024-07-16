package com.demo.project67.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationExceptionHandler implements ExceptionHandler {
    @Override
    public void handleException(RepeatContext context, Throwable throwable) throws Throwable {
        log.error("Error sending notification!");
    }

}
