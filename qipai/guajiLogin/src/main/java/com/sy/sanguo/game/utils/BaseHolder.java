package com.sy.sanguo.game.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BaseHolder implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		BaseHolder.applicationContext = arg0;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static <T> T getBean(String beanName) {
		return (T) applicationContext.getBean(beanName);
	}

	public static <T> T getBean(Class<?> beanClass) {
		return (T) applicationContext.getBean(beanClass);
	}
}