package com.sdu.irlab.chatlabelling.aspect;

import com.sdu.irlab.chatlabelling.datasource.domain.WebRequestLog;
import com.sdu.irlab.chatlabelling.datasource.repository.WebRequestLogDAO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.security.Principal;
import java.util.Arrays;

@Aspect
@Component
public class LogRequestAspect {

    private static final Logger log = LoggerFactory.getLogger(LogRequestAspect.class);
    @Autowired
    private WebRequestLogDAO webRequestLogDAO;

    /**
     * 定义切入点，controller下面的所有类的所有公有方法，这里需要更改成自己项目的
     */
    @Pointcut("execution(public * com.sdu.irlab.chatlabelling.controller.*.*(..))")
    public void requestLog() {
    }

    ;

//    @Before("requestLog()")
//    public void doBefore(JoinPoint joinPoint) {
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        //打印当前的请求路径
//        log.info("RequestMapping:[{}]", request.getRequestURI());
//        //打印请求参数，如果需要打印其他的信息可以到request中去拿
//        log.info("RequestParam:{}", Arrays.toString(joinPoint.getArgs()));
//        log.info("IP : " + request.getRemoteAddr());
//        log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "."
//                + joinPoint.getSignature().getName());
//        HttpSession session = request.getSession();
//        //读取session中的用户
//        log.info("User:{}", request.getUserPrincipal().getName());
//    }

    //    @AfterReturning(returning = "response", pointcut = "requestLog()")
//    public void doAfterRunning(Object response) {
//        //打印返回值信息
//        log.info("Response:[{}]", response);
//        //打印请求耗时
////        log.info("Request spend times : [{}ms]", System.currentTimeMillis() - startTime.get());
//    }
    @Around("requestLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
//        CustomHttpServletRequestWrapper wrapper=new CustomHttpServletRequestWrapper(request);
//        String body=wrapper.getBodyString();
        WebRequestLog webRequestLog = new WebRequestLog();
        webRequestLog.setRequestUrl(request.getRequestURI());
        webRequestLog.setClassMethod(joinPoint.getSignature().getDeclaringTypeName() + "."
                + joinPoint.getSignature().getName());
        webRequestLog.setIp(request.getRemoteAddr());
        webRequestLog.setParameters(Arrays.toString(joinPoint.getArgs()));
//        webRequestLog.setRequestBody(body);
        Principal principal = request.getUserPrincipal();
        webRequestLog.setRequestUser(principal == null ? null : principal.getName());

        Object response = joinPoint.proceed();
        long spend = System.currentTimeMillis() - start;
        webRequestLog.setSpendTimeMills(spend);
        webRequestLog.setResponse(response+"");
        webRequestLogDAO.save(webRequestLog);
        return response;
    }
}
