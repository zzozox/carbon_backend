package com.carbon.domain.common.annotation;


import java.lang.annotation.*;

@Inherited
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperatorName {

}
