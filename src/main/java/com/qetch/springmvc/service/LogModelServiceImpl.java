package com.qetch.springmvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qetch.springmvc.domain.LogModel;

@Service
public class LogModelServiceImpl implements LogModelService {
	private static final Logger logger = LoggerFactory.getLogger(LogModelServiceImpl.class);

	@Override
	public void saveLogModel(LogModel logModel) {
		logger.info("正在保存..." + logModel.toString());
	}
}
