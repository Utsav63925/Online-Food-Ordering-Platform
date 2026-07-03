package com.substring.foodies.Utility;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class GenderValidator implements ConstraintValidator<ValidGender, String> {

    private Logger logger= LoggerFactory.getLogger(this.getClass().getName());
    @Override
    public void initialize(ValidGender constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {

        if(value==null || value.isEmpty()) {
            logger.warn("Invalid Gender String");
            return false;
        }

        if(value.toLowerCase().equals("male") || value.toLowerCase().equals("female"))
        {
            return true;
        }
        return false;
    }
}
