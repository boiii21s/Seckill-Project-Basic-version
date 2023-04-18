package com.miaoshaoproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


@Component
public class ValidatorImpl implements InitializingBean {
    private Validator validator;

    //实现较验方法并返回较验结果
    public ValidationResult validate(Object bean){
        final ValidationResult result = new ValidationResult();
        //这个set中就是放的错误信息
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size()>0){
            //有错误
            result.setHasError(true);
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        return result;

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //通过hiberna validator工厂的初始化方式使其validator实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();

    }
}
