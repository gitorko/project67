package com.demo.project67.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.stereotype.Component;

@Component
public class JobParameterValidator implements JobParametersValidator {
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        var customerName = (String) parameters.getParameters().get("customer").getValue();
        if (customerName.isBlank()) {
            throw new JobParametersInvalidException("Invalid Parameters");
        }
    }
}
