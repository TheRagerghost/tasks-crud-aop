package com.avragerghost.tasks_crud_aop.aspects;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.avragerghost.tasks_crud_aop.aspects.annotations.RequireRole;
import com.avragerghost.tasks_crud_aop.enums.UserRole;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.models.User;
import com.avragerghost.tasks_crud_aop.services.TaskService;

@Aspect
@Component
public class TaskAspect {
    private static final Logger logger = LoggerFactory.getLogger(TaskAspect.class);

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_DIM = "\u001B[2m";
    private static final String ANSI_RESET = "\u001B[0m";

    @Autowired
    private TaskService taskService;

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

    @Before("@annotation(requireRole)")
    public void checkRolePermission(JoinPoint joinPoint, RequireRole requireRole) {
        User user = taskService.getCurrentUser();
        UserRole[] reqRoles = requireRole.value();
        boolean hasRequiredRole = false;
        for (UserRole reqRole : reqRoles) {
            if (user.getRole().equals(reqRole)) {
                hasRequiredRole = true;
                break;
            }
        }

        if (!hasRequiredRole) {
            logger.warn("\nДля выполнения метода {} нужны права: {}", joinPoint.getSignature().getName(), reqRoles);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "У текущего пользователя недостаточно прав для выполнения данного запроса.");
        }
    }

    @AfterReturning(pointcut = "taskServiceMethods()", returning = "result")
    public void enrichTaskLog(JoinPoint joinPoint, Object result) {
        if (result instanceof Task) {
            Task task = (Task) result;

            String title = task.getTitle();
            String desc = task.getDescription();
            String richLog;

            switch (task.getState()) {
                case VISIBLE:
                    richLog = ANSI_GREEN + title + ANSI_RESET + ": " + desc;
                    break;

                case HIDDEN:
                    richLog = ANSI_DIM + title + ANSI_RESET + ": " + ANSI_DIM + "Задача скрыта" + ANSI_RESET;
                    break;

                case DELETED:
                    richLog = ANSI_RED + title + ANSI_RESET + ": " + ANSI_RED + "Задача удалена" + ANSI_RESET;
                    break;

                default:
                    richLog = title + ": " + desc;
            }

            logger.info("\n====================\n" +
                    "[ID: {} | User: {}]\t{}" +
                    "\n====================\n",
                    task.getId(), task.getUser().getName(), richLog);

        }
    }

    @AfterThrowing(pointcut = "taskServiceMethods()", throwing = "e")
    public void handleTaskServiceException(JoinPoint joinPoint, Exception e) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        logger.error("\nОшибка в методе {} с аргументами: {}\nСообщение: {}", methodName, args, e);
    }

}
