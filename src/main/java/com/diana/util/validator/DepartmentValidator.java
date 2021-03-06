package com.diana.util.validator;

import com.diana.model.Department;
import com.diana.service.DepartmentService;
import com.diana.util.error.EntityAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@ComponentScan(basePackages = {"com.diana.service"})
public class DepartmentValidator implements Validator {

    @Autowired
    private DepartmentService departmentService;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(Department.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Department department = (Department) o;
        Department d = departmentService.findByName(department.getName());
        if (d != null){
            throw new EntityAlreadyExistsException("Department with such name already exists");
        }
    }
}
