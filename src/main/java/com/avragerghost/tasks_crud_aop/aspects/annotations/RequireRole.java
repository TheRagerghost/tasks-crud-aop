package com.avragerghost.tasks_crud_aop.aspects.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avragerghost.tasks_crud_aop.enums.UserRole;

/**
 * Аннотация для назначения разрешенных ролей пользователя на метод.
 * <p>
 * Default: {@code STAFF} и {@code ADMIN}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireRole {
    UserRole[] value() default { UserRole.STAFF, UserRole.ADMIN };
}
