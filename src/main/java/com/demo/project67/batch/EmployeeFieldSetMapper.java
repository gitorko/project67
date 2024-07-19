package com.demo.project67.batch;

import com.demo.project67.domain.Employee;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class EmployeeFieldSetMapper implements FieldSetMapper<Employee> {

    @Override
    public Employee mapFieldSet(FieldSet fieldSet) throws BindException {
        Employee employee = new Employee();
        employee.setId(fieldSet.readLong("id"));
        employee.setName(fieldSet.readString("name"));
        employee.setDepartment(fieldSet.readString("department"));
        employee.setSalary(fieldSet.readDouble("salary"));

        String employmentType;
        switch (fieldSet.readInt("employmentType")) {
            case 0:
                employmentType = "terminated";
                break;
            case 1:
                employmentType = "permanent";
                break;
            case 2:
                employmentType = "contract";
                break;
            default:
                throw new IllegalArgumentException("Invalid employment type: " + fieldSet.readInt("employmentType"));
        }
        employee.setEmploymentType(employmentType);

        return employee;
    }
}
