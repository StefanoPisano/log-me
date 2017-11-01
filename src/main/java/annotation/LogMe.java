package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will log the method which is being executed
 * with different informations depending on the chosen Kind.
 *
 * SIMPLE will log the Class and the Method name and the arguments
 *
 * DEVSTYLE will log all information of SIMPLE Kind and it will tell you
 * if the method is being executed in a:
 * @Controller
 * @RestController
 * @Service
 * @Repository
 *
 * It will tell you also if:
 * @Controller / @RestController : all informations about kind of
 * url mappings (GET, POST, PUT, DELETE, PATCH) with their value.
 *
 * @Service / @Repository : if the method is annotated with @Transactional
 *
 * Level will allow you to choose the debug level (INFO, WARN, DEBUG, TACE, ERROR)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogMe {

     enum LogMeLevel {
        INFO,
        DEBUG,
        TRACE,
        WARN,
        ERROR
    }

    enum LogKind {
        SIMPLE,
        DEVSTYLE
    }

    LogMeLevel level() default LogMeLevel.INFO;
    LogKind kind() default LogKind.DEVSTYLE;

}
