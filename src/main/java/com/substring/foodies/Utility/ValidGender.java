package com.substring.foodies.Utility;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Where to apply?? For that we write Target.
// We want to apply it on the fields of classes.

@Target(ElementType.FIELD)

// Should run at run time.
// Retention defines till what we can retain our annotation in program lifecycle.
// The annotation is retained at runtime, meaning it can be accessed during execution via reflection.
@Retention(RetentionPolicy.RUNTIME)

//The @Constraint annotation is used to link this
//annotation (@ValidGender) with a validator class
//that contains the validation logic.
//This ensures that the annotation is used for validation purposes.
//In this it is linked to GenderValidator.class
@Constraint(validatedBy = {GenderValidator.class})
public @interface ValidGender {

    String message() default "Not a valid gender.";

    // Group property is basically used to group multiple validation constraints.
    Class<?>[] groups() default {};

    // Payload property is used for adding any extra metadata.
    Class<? extends Payload>[] payload() default {};
}
