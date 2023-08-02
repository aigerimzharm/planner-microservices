package ru.javabegin.micro.planner.utils.aop;

import lombok.extern.java.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect // выполнять логгирование, кэширование и др.методы в отдельном классе (не изменяя самих объектов)
@Component //Spring считает эту аннтотацию и добавит этот класс в качестве spring-bean в свой контейнер
@Log // автоматически создаст переменную и выведет инфо в консоль

public class LoggingAspect {

    // аспект будет выполняться для всех методов из пакета контроллеров
    @Around("execution(* ru.javabegin.micro.planner.todo.controller..*(..)))")
    public Object profileControllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

        // получить информацию о том, какой класс и метод выполняется
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        log.info("----- Executing " + className + "." + methodName + "-----");

        StopWatch countDown = new StopWatch();

        countDown.start();
        Object result = proceedingJoinPoint.proceed(); // выполняем сам метод
        countDown.stop();

        log.info("-----Executing time of " + className + "." + methodName + " :: " + countDown.getTotalTimeMillis() + " ms");

        return result;

    }

}
