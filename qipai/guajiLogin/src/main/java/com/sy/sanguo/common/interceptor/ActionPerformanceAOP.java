package com.sy.sanguo.common.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionPerformanceAOP {

    protected static final Logger LOG = LoggerFactory.getLogger("monitor");

    protected Object actionProcess(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        Object target = point.getTarget();
        String className = target.getClass().getName();
        String methodName = point.getSignature().getName();
        Object ret;
        try {
            ret = point.proceed();
        } catch (Throwable var12) {
            LOG.error("Throwable Message:" + var12.getMessage(), var12);
			throw var12;
//			return null;
        } finally {
            long timeUse = System.currentTimeMillis() - start;
            if (timeUse > 200) {
                StringBuilder sb = new StringBuilder("xnlog|action");
                sb.append("|").append(timeUse);
                sb.append("|").append(start);
                sb.append("|").append(Thread.currentThread());
                sb.append("|").append(className);
                sb.append("|").append(methodName);
                LOG.info(sb.toString());
            }
        }
        return ret;
    }
}
