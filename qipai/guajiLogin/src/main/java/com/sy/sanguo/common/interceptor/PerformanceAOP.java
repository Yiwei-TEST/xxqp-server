package com.sy.sanguo.common.interceptor;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.Constants;
import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceAOP {

    protected static final Logger logger = LoggerFactory.getLogger("db");

    protected Object aroundDb(ProceedingJoinPoint point) {
        Object target = point.getTarget();
        String className = target.getClass().getName();
        String methodName = point.getSignature().getName();
        long startTime = System.currentTimeMillis();
        Object[] objs = point.getArgs();
        Object obj = objs != null && objs.length > 0 ? objs[0] : "";
        String mark = PropertiesCacheUtil.getValueOrDefault("performance_db", "a", Constants.GAME_FILE);
        Object args = objs != null && objs.length > 1 ? objs[1] : "";
        Object ret;
        try {
            ret = point.proceed();
        } catch (Throwable var12) {
            logger.error("Throwable Message:" + var12.getMessage(), var12);
            return null;
        } finally {
            long timeUse = System.currentTimeMillis() - startTime;
            StringBuilder sb = new StringBuilder("xnlog|db");
            sb.append("|").append(timeUse);
            sb.append("|").append(startTime);
            sb.append("|").append(obj);
            sb.append("|").append(args);
            sb.append("|").append(methodName);
            sb.append("|").append(Thread.currentThread().toString());
            sb.append("|").append(className);
            if (NumberUtils.isDigits(mark)) {
                if (timeUse >= Long.parseLong(mark)) {
                    logger.info(sb.toString());
                }
            } else {
                logger.info(sb.toString());
            }
        }

        return ret;
    }

//    protected Object aroundService(ProceedingJoinPoint point) {
//        Object target = point.getTarget();
//        String className = target.getClass().getName();
//        String methodName = point.getSignature().getName();
//        long time = System.currentTimeMillis();
//        boolean saveLog = "1".equals(PropertiesCacheUtil.getValueOrDefault("performance_service","1",Constants.GAME_FILE));
//
//        Object ret;
//        try {
//            ret = point.proceed();
//        } catch (Throwable var12) {
//            logger.error("Throwable Message:" + var12.getMessage(), var12);
//            return null;
//        } finally {
//            if (saveLog) {
//                logger.info((new StringBuilder(256)).append("currentThread:").append(Thread.currentThread().toString()).append(" class:").append(className).append(" method:").append(methodName).append(" time(ms):").append(System.currentTimeMillis() - time).toString());
//            }
//        }
//
//        return ret;
//    }

}
