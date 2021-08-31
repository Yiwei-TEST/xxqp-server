package com.sy599.game.util.helper;

import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.util.GameConfigUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.util.PayConfigUtil;

import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 资源热加载
 * @author liuping
 */
public class ResourceWatchHandler {

    private static ResourceWatchHandler watchInstance;

    public static ResourceWatchHandler getWatchInstance(String propertiesFilePath, String csvFilePath) {
        if (watchInstance == null) {
            ResourceWatchHandler.propertiesFilePath = propertiesFilePath;
            ResourceWatchHandler.csvFilePath = csvFilePath;
            watchInstance = new ResourceWatchHandler(propertiesFilePath, csvFilePath);
        }
        return watchInstance;
    }

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * properties资源监听加载绝对路径
     */
    private static String propertiesFilePath;

    /**
     * csv资源监听加载绝对路径
     */
    private static String csvFilePath;

    private WatchService fileWatcher;



    private Map<String, Class<? extends ResourceHandler>> resourceHandlers = new HashMap<>();

    /**
     * 初始化资源加载类  手动配置
     */
    public void initResourceHandler() {
        resourceHandlers.put("pay.properties", PayConfigUtil.class);
        resourceHandlers.put("gameConfig.properties", GameConfigUtil.class);
        resourceHandlers.put("activityConfig.csv", StaticDataManager.class);
        resourceHandlers.put("gameRebate.csv", StaticDataManager.class);
        resourceHandlers.put("taskConfig.csv", StaticDataManager.class);
        resourceHandlers.put("lang.csv", LangMsg.class);
        startListenerEvent();
    }

    /**
     * 是否开放
     */
    private boolean openListen = true;

    public ResourceWatchHandler(String propertiesFilePath, String csvFilePath) {
        try {
            fileWatcher = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(propertiesFilePath);
            path.register(fileWatcher, StandardWatchEventKinds.ENTRY_MODIFY);

            path = Paths.get(csvFilePath);
            path.register(fileWatcher, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (Exception e) {
            LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 开启配置表资源文件监听 并加载到内存数据
     */
    public void startListenerEvent() {
        if (!openListen) {
            return;
        }
        LogUtil.msgLog.info("start listen resource event...");
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    WatchKey key = fileWatcher.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;// 事件可能lost or discarded
                        }
                        WatchEvent<Path> e = (WatchEvent<Path>) event;
                        Path fileName = e.context();
                        try {
                            Class handlerClass = resourceHandlers.get(fileName.toString());
                            if (handlerClass == null)// 未配置加载处理
                                continue;
                            ResourceHandler handler = (ResourceHandler) ObjectUtil.newInstance(handlerClass);
                            String pathName = "";
                            if(fileName.toString().contains(".properties")) {
                                pathName = propertiesFilePath;
                            } else if(fileName.toString().contains(".csv")) {
                                pathName = csvFilePath;
                            }
                            handler.reload(pathName + fileName.toString());
                            LogUtil.msgLog.info("reload file success: " + fileName);
                        } catch (Exception ex) {
                            LogUtil.msgLog.error(fileName + "配置重新加载异常" + ex.getMessage(), ex);
                        }
                    }
                    if (!key.reset()) {
                    }
                } catch (Exception e) {
                    LogUtil.msgLog.error("配置热加载异常" + e.getMessage(), e);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public static final void shutDown() {
        if (!executor.isShutdown())
            executor.shutdown();
    }
}
