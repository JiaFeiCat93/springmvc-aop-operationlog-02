package com.qetch.springmvc.domain;

public class LogModel {
	private User user;			//用户
	private String remoteAddr;	//IP
	private String exception;	//异常
	private String title;		//日志标题
	private String requestUri;	//请求地址
	private String type;		//日志类型
	private String description;	//日志记录描述
	private String addTime;		//日志添加时间
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getRemoteAddr() {
		return remoteAddr;
	}
	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRequestUri() {
		return requestUri;
	}
	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	@Override
	public String toString() {
		return "LogModel [user=" + user + ", remoteAddr=" + remoteAddr + ", exception=" + exception + ", title=" + title
				+ ", requestUri=" + requestUri + ", type=" + type + ", description=" + description + ", addTime="
				+ addTime + "]";
	}
}
