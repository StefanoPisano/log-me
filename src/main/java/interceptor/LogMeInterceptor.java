package interceptor;

import annotation.LogMe;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Aspect
@Component
public class LogMeInterceptor {

    private Logger log;

    @Around("@annotation(annotation.LogMe)")
    public Object LogMe(ProceedingJoinPoint p) throws Throwable {
        final Method m = ((MethodSignature) p.getSignature()).getMethod();
        final LogMe l = m.getAnnotation(LogMe.class);

        initLogger(m);

        StringBuilder sb = new StringBuilder();

        setForKind(l, m, sb);
        logMethod(m, sb);
        logParams(Arrays.asList(p.getArgs()).stream().map(v -> v.toString()).collect(Collectors.toList()), sb);

        logIt(l.level(), sb);

        return p.proceed();
    }

    private void initLogger(Method m) {
        log = LoggerFactory.getLogger(m.getDeclaringClass());
    }

    private void setForKind(LogMe l, Method m, StringBuilder sb) {
        switch(l.kind()) {
            case DEVSTYLE:
                checkForClassAnnotations(m, sb);
                break;
            case SIMPLE:
                getLogClass(m, sb);
                break;
            default:
                throw new RuntimeException("INVALID KIND" + l.kind());
        }
    }

    private void checkForClassAnnotations(Method m, StringBuilder sb) {
        final Class<?> clazz = m.getDeclaringClass();
        if(clazz.isAnnotationPresent(RestController.class)) {
            sb.append("[@RestController]");
            getLogClass(m, sb);
            checkForControllerMapping(m, sb);
        } else if(clazz.isAnnotationPresent(Controller.class)) {
            sb.append("[@Controller]");
            getLogClass(m, sb);
            checkForControllerMapping(m, sb);
        } else if(clazz.isAnnotationPresent(Service.class)) {
            sb.append("[@Service]");
            getLogClass(m, sb);
            checkForServiceOrRepositoryMapping(m, sb);
        } else if(clazz.isAnnotationPresent(Repository.class)) {
            sb.append("[@Repository]");
            getLogClass(m, sb);
            checkForServiceOrRepositoryMapping(m, sb);
        } else {
            getLogClass(m, sb);
        }
    }

    private void getLogClass(Method m, StringBuilder sb) {
        sb.append("[").append(m.getDeclaringClass().getSimpleName()).append("]");
    }

    private void logMethod(Method m, StringBuilder sb) {
        sb.append("[").append(m.getName()).append("]");
    }

    private void logParams(Collection<String> coll, StringBuilder sb) {
        sb.append("[").append(coll.toString()).append("]");
    }

    private void checkForControllerMapping(Method m, StringBuilder sb) {
        if(m.isAnnotationPresent(RequestMapping.class)) {
            sb.append("[")
                    .append(Arrays.asList(m.getAnnotation(RequestMapping.class).method()).toString())
                    .append(Arrays.asList(m.getAnnotation(RequestMapping.class).value()).toString())
                    .append("] ");
        } else if (m.isAnnotationPresent(PostMapping.class)) {
            sb.append("[POST] [")
                    .append(Arrays.asList(m.getAnnotation(PostMapping.class).value()).toString())
                    .append("] ");
        } else if (m.isAnnotationPresent(GetMapping.class)) {
            sb.append("[GET] [")
                    .append(Arrays.asList(m.getAnnotation(GetMapping.class).value()).toString())
                    .append("] ");
        } else if(m.isAnnotationPresent(PatchMapping.class)) {
            sb.append("[PATCH][")
                    .append(Arrays.asList(m.getAnnotation(PatchMapping.class).value()).toString())
                    .append("] ");
        } else if(m.isAnnotationPresent(PutMapping.class)) {
            sb.append("[PUT] [")
                    .append(Arrays.asList(m.getAnnotation(PutMapping.class).value()).toString())
                    .append("] ");
        } else if(m.isAnnotationPresent(DeleteMapping.class)) {
            sb.append("[DELETE] [")
                    .append(Arrays.asList(m.getAnnotation(DeleteMapping.class).value()).toString())
                    .append("] ");
        }
    }

    private void checkForServiceOrRepositoryMapping(Method m, StringBuilder sb) {
        if (m.isAnnotationPresent(Transactional.class)) {
            sb.append("[@Transactional] ");
        }
    }

    private void logIt(LogMe.LogMeLevel level, StringBuilder sb){
        switch (level) {
            case ERROR:
                log.error(sb.toString());
                break;
            case WARN:
                log.warn(sb.toString());
                break;
            case TRACE:
                log.trace(sb.toString());
            case DEBUG:
                log.debug(sb.toString());
                break;
            case INFO:
                log.info(sb.toString());
                break;
            default:
                throw new RuntimeException("INVALID LEVEL" + level);
        }
    }
}
