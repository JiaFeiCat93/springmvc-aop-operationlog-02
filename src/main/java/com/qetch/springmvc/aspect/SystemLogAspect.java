package com.qetch.springmvc.aspect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.qetch.springmvc.domain.LogModel;
import com.qetch.springmvc.domain.User;

@Component
@Aspect
public class SystemLogAspect {
	private static final Logger logger = LoggerFactory.getLogger(SystemLogAspect.class);
	private static final ThreadLocal<Date> beginTimeThreadLocal = new NamedThreadLocal<>("ThreadLocal begin time");
	private static final ThreadLocal<LogModel> logThreadLocal = new NamedThreadLocal<>("ThreadLocal log");
	private static final ThreadLocal<User> currentUser = new NamedThreadLocal<>("ThreadLocal user");
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	/**
	 * Controller层切点注解拦截
	 */
	@Pointcut("@annotation(com.qetch.springmvc.annotation.MethodLog)")
	public void controllerAspect() {
		
	}
	
	/**
	 * 用于拦截Controller层记录用户的操作的开始时间
	 * @param joinPoint 切点
	 * @throws InterruptedException
	 */
	@Before("controllerAspect")
	public void doBefore(JoinPoint joinPoint) throws InterruptedException {
		Date beginTime = new Date();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		beginTimeThreadLocal.set(beginTime);
		if (logger.isDebugEnabled()) {// 这里日志级别为DEBUG
			logger.debug("开始计时:{} URI:{}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(beginTime), request.getRequestURI());
		}
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		System.out.println(user);
		currentUser.set(user);
	}
}
