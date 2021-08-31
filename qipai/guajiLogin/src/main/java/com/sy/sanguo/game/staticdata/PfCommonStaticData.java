package com.sy.sanguo.game.staticdata;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.game.service.SysInfManager;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.PfSdkConfig;

public final class PfCommonStaticData {
    private static Map<String, PfSdkConfig> pfMap = new ConcurrentHashMap<>();
    /*** 公众号参数 */
    private static Map<String, PfSdkConfig> pfGZHMap = new ConcurrentHashMap<>();

    static {
        // 游航公众号
        PfSdkConfig yhGZH = new PfSdkConfig();
        yhGZH.setAppId("wx28e21058087f8abd");
        yhGZH.setAppKey("c615ee024a20466a9464f4bef418e934");
        yhGZH.setPf("yhGZH");
        pfGZHMap.put(yhGZH.getPf(), yhGZH);

        // 迅游公众号
        PfSdkConfig xyGZH = new PfSdkConfig();
        xyGZH.setAppId("wx518349f40dc68bcf");
        xyGZH.setAppKey("a6e8aa12f519af1f252c7f5eb33ae29c");
        xyGZH.setPf("xyGZH");
        pfGZHMap.put(xyGZH.getPf(), xyGZH);

        //登陆、支付参数配置（PfSdkConfig）
        Properties pfConfigs=loadFromFile(SysInfManager.baseDir + "/WEB-INF/config/pf.properties");
        if (pfConfigs!=null){
            for (Map.Entry<Object,Object> kv:pfConfigs.entrySet()){
                try{
                    JSONObject json=JSONObject.parseObject(kv.getValue().toString());
                    String pf=json.getString("pf");
                    String appId=json.getString("appId");
                    String appKey=json.getString("appKey");
                    String mchId=json.getString("mchId");
                    String payKey=json.getString("payKey");
                    String extStr=json.getString("extStr");

                    PfSdkConfig config=new PfSdkConfig();
                    if (StringUtils.isNotEmpty(pf)){
                        config.setPf(pf);
                    }
                    if (StringUtils.isNotEmpty(appId)){
                        config.setAppId(appId);
                    }
                    if (StringUtils.isNotEmpty(appKey)){
                        config.setAppKey(appKey);
                    }
                    if (StringUtils.isNotEmpty(mchId)){
                        config.setMch_id(mchId);
                    }
                    if (StringUtils.isNotEmpty(payKey)){
                        config.setPayKey(payKey);
                    }
                    if (StringUtils.isNotEmpty(extStr)){
                        config.setExtStr(extStr);
                    }

                    PfSdkConfig pre=pfMap.put(pf,config);
                    if (pre==null){
                        GameBackLogger.SYS_LOG.info("load pf config success:"+kv.toString());
                    }else{
                        GameBackLogger.SYS_LOG.error("load pf config override:"+kv.toString());
                    }
                }catch (Exception e){
                    GameBackLogger.SYS_LOG.error("pf.properties:key="+kv.getKey()+",value="+kv.getValue()+",Exception:"+e.getMessage(),e);
                }
            }
        }else{
            GameBackLogger.SYS_LOG.error("file not exists:"+SysInfManager.baseDir + "/WEB-INF/config/pf.properties");
        }

    }

    public static int countPfConfig(){
        return pfMap.size();
    }

    private static Properties loadFromFile(String dir){
        if (!new File(dir).exists()){
            GameBackLogger.SYS_LOG.warn("file not exists:"+dir);
            return null;
        }
        Properties properties=new Properties();
        try {
            FileInputStream fis = new FileInputStream(dir);
            properties.load(fis);
            fis.close();
            GameBackLogger.SYS_LOG.info("load file success:"+dir);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("Exception:"+e.getMessage(),e);
            return null;
        }
        return properties;
    }

    /**
     * 获取静态数据
     *
     * @param pf
     * @return
     */
    public static PfSdkConfig getConfig(String pf) {
        PfSdkConfig ret = pfMap.get(pf);
        if (ret==null){
            GameBackLogger.SYS_LOG.info("pfsdk is null:" + pf);
        }
        return ret;
    }

    /**
     * 获取静态数据
     *
     * @param pf
     * @return
     */
    public static PfSdkConfig getGzhConfig(String pf) {
        if (StringUtils.isBlank(pf)) {
            //默认迅游公众号
            pf = "xyGZH";
        }
        PfSdkConfig ret = pfGZHMap.get(pf);
        if (ret==null){
            GameBackLogger.SYS_LOG.info("pfGZH is null:" + pf);
        }
        return null;
    }

    /**
     * Pf是否在静态数据Map中
     *
     * @param pf
     * @return
     */
    public static boolean isHasPf(String pf) {
        return pfMap.containsKey(pf);
    }
}
