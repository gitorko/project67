package com.demo.project67.batch;

import com.demo.project67.domain.Employee;
import com.demo.project67.service.HelperUtil;
import org.springframework.batch.item.ItemProcessor;

public class EmployeeProcessor implements ItemProcessor<Employee, Employee> {

    @Override
    public Employee process(Employee employee) throws Exception {
        // Example processing: Increase salary by 10%
        employee.setSalary(employee.getSalary() * 1.1);
        HelperUtil.delay(1);
        return employee;
    }
}
