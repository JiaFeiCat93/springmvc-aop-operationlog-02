package com.qetch.springmvc.aspect;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.qetch.springmvc.annotation.MethodLog;
import com.qetch.springmvc.domain.LogModel;
import com.qetch.springmvc.domain.User;
import com.qetch.springmvc.service.LogModelService;

@Component
@Aspect
public class SystemLogAspect {
	private static final Logger logger = LoggerFactory.getLogger(SystemLogAspect.class);
	private static final ThreadLocal<Date> beginTimeThreadLocal = new NamedThreadLocal<>("ThreadLocal begin time");
	private static final ThreadLocal<LogModel> logThreadLocal = new NamedThreadLocal<>("ThreadLocal log");
	private static final ThreadLocal<User> currentUser = new NamedThreadLocal<>("ThreadLocal user");
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Autowired
	private LogModelService logModelService;
	
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
		// 读取session中的用户
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		System.out.println(user);
		currentUser.set(user);
	}
	
	/**
	 * 用于拦截Controller层记录用户的操作
	 * @Title: doAfter
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param: @param joinPoint
	 * @param: @throws Exception
	 * @return: void
	 * @throws
	 */
	@After("controllerAspect()")
	public void doAfter(JoinPoint joinPoint) throws Exception {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		User user = currentUser.get();
		if (user != null) {
			String title = "";
			String type = "info";// 日志类型（info：入库，error：错误）
			String remoteAddr = SystemLogAspect.getIP();// 请求的IP
			String requestUri = request.getRequestURI();// 请求的URI
			title = SystemLogAspect.getControllerMethodDescription(joinPoint);
			
			LogModel logModel = new LogModel();
			logModel.setTitle(title);
			logModel.setType(type);
			logModel.setRemoteAddr(remoteAddr);
			logModel.setRequestUri(requestUri);
			logModel.setException("无异常");
			logModel.setUser(user);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logModel.setAddTime(sdf.format(new Date()));
			threadPoolTaskExecutor.execute(new SaveLogThread(logModel, logModelService));
			logThreadLocal.set(logModel);
		}
	}
	
	/**
	 * 异常通知 记录操作报错的日志
	 * @Title: doAfterThrowing
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param: @param joinPoint
	 * @param: @param e
	 * @return: void
	 * @throws
	 */
	public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
		LogModel logModel = logThreadLocal.get();
		logModel.setType("error");
		logModel.setException(e.toString());
		new UpdateLogThread(logModel, logModelService).start();
	}
	
	/**
	 * 获取IP
	 * @Title: getIP
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param: @return
	 * @param: @throws Exception
	 * @return: String
	 * @throws
	 */
	public static String getIP() throws Exception {
		InetAddress ia = InetAddress.getLocalHost();
		String localIP = ia.getHostAddress();
		return localIP;
	}
	
	/**
	 * 获取注解中对方法的描述信息 用于controller层注解
	 * @Title: getControllerMethodDescription
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param: @param joinPoint
	 * @param: @return
	 * @return: String
	 * @throws
	 */
	public static String getControllerMethodDescription(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		MethodLog methodLog = method.getAnnotation(MethodLog.class);
		String description = methodLog.remarks();
		return description;
	}
	
	/**
	 * 保存日志线程
	 * @ClassName: SaveLogThread
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author zcw
	 * @date: 2019年3月7日 上午12:20:31
	 */
	private static class SaveLogThread implements Runnable {
		private LogModel logModel;
		private LogModelService logModelService;
		
		public SaveLogThread(LogModel logModel, LogModelService logModelService) {
			this.logModel = logModel;
			this.logModelService = logModelService;
		}
		
		@Override
		public void run() {
			logModelService.saveLogModel(logModel);
		}
	}
	
	/**
	 * 更新日志线程
	 * @ClassName: UpdateLogThread
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author zcw
	 * @date: 2019年3月7日 上午12:23:34
	 */
	private static class UpdateLogThread extends Thread {
		private LogModel logModel;
		private LogModelService logModelService;
		
		public UpdateLogThread(LogModel logModel, LogModelService logModelService) {
			super(UpdateLogThread.class.getSimpleName());
			this.logModel = logModel;
			this.logModelService = logModelService;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
}
