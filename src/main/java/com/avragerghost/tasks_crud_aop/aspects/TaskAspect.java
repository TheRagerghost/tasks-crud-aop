package com.avragerghost.tasks_crud_aop.aspects;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.avragerghost.tasks_crud_aop.models.Task;

@Aspect
@Component
public class TaskAspect {
    private static final Logger logger = LoggerFactory.getLogger(TaskAspect.class);

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_DIM = "\u001B[2m";
    private static final String ANSI_RESET = "\u001B[0m";

    @Pointcut("execution(* com.avragerghost.tasks_crud_aop.services.TaskService.*(..))")
    public void taskServiceMethods() {
    };

    @Around("@annotation(com.avragerghost.tasks_crud_aop.aspects.annotations.LogExecTime)")
    public Object logExecTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        Long endTime = System.currentTimeMillis();
        logger.info(
                "\nМетод " + ANSI_GREEN + "{}" + ANSI_RESET + " был выполнен за " + ANSI_YELLOW + "{}ms" + ANSI_RESET,
                joinPoint.getSignature().getName(), endTime - startTime);
        return result;
    }

    @Before("@annotation(com.avragerghost.tasks_crud_aop.aspects.annotations.ForbidForPublicAPI)")
    public void blockForPublicAPI(JoinPoint joinPoint) {
        logger.warn("\nМетод {} запрещен к выполнению через открытый API", joinPoint.getSignature().getName());
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action is blocked for public API");
    }

    @AfterReturning(pointcut = "taskServiceMethods()", returning = "result")
    public void enrichTaskLog(JoinPoint joinPoint, Object result) {
        System.out.println("====================");
        if (result instanceof Task) {
            Task task = (Task) result;
            logTaskRich(task);
        } else if (result instanceof List<?> tasks && !tasks.isEmpty() && tasks.get(0) instanceof Task) {
            @SuppressWarnings("unchecked")
            List<Task> tasksList = (List<Task>) tasks;
            tasksList.forEach(this::logTaskRich);
        }
        System.out.println("====================\n");
    }

    public void logTaskRich(Task task) {
        String title = task.getTitle();
        String desc = task.getDescription();
        String richLog;

        switch (task.getState()) {
            case VISIBLE:
                richLog = ANSI_GREEN + title + ANSI_RESET + ": " + desc;
                break;

            case HIDDEN:
                richLog = ANSI_DIM + title + ANSI_RESET + ": " + ANSI_DIM + "this task is HIDDEN." + ANSI_RESET;
                break;

            case DELETED:
                richLog = ANSI_RED + title + ANSI_RESET + ": " + ANSI_RED + "this task is DELETED." + ANSI_RESET;
                break;

            default:
                richLog = title + ": " + desc;
        }

        System.out.println("[ID: " + task.getId() + " | User ID: " + task.getUserId() + "]\t" + richLog);
    }

    @AfterThrowing(pointcut = "taskServiceMethods()", throwing = "e")
    public void handleTaskServiceException(JoinPoint joinPoint, Exception e) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        logger.error("\nОшибка в методе {} с аргументами: {}\nСообщение: {}", methodName, args, e);
    }

}
