//package com.sy.sanguo.game.utils;
//
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.TransactionDefinition;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.support.DefaultTransactionDefinition;
//import org.springframework.transaction.support.TransactionTemplate;
//
//@Component
//public class TransactionTemplateUtils implements ApplicationContextAware {
//
//	/** 事务模板 */
//	private static TransactionTemplate transactionTemplate; // set方法
//
//	public static TransactionTemplate getTransactionTemplate() {
//		return transactionTemplate;
//	}
//
//	@Override
//	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		this.transactionTemplate = (TransactionTemplate) applicationContext.getBean("transactionTemplate");
//	}
//
//
//	public void execute(Runnable r) {
//		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//		// explicitly setting the transaction name is something that can be done only programmatically
//		def.setName("SomeTxName");
//		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//		PlatformTransactionManager txManager = transactionTemplate.getTransactionManager();
//		TransactionStatus status = txManager.getTransaction(def);
//		try {
//			// execute your business logic here
//			r.run();
//		}
//		catch (Exception ex) {
//			txManager.rollback(status);
//			throw ex;
//		}
//		txManager.commit(status);
//	}
//}