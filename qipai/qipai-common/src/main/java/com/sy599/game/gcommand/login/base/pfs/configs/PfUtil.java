package com.sy599.game.gcommand.login.base.pfs.configs;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PfUtil {
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
        Map<String,String> pfConfigs = ResourcesConfigsUtil.loadStringValues("PfConfig");
        if (pfConfigs!=null){
            for (Map.Entry<String,String> kv:pfConfigs.entrySet()){
                try{
                    JSONObject json=JSONObject.parseObject(kv.getValue());
                    String pf=json.getString("pf");
                    String appId=json.getString("appId");
                    String appKey=json.getString("appKey");
                    String mchId=json.getString("mchId");
                    String payKey=json.getString("payKey");

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

                    PfSdkConfig pre=pfMap.put(pf,config);
                    if (pre==null){
                        LogUtil.msgLog.info("load pf config success:"+kv.toString());
                    }else{
                        LogUtil.msgLog.warn("load pf config override:"+kv.toString());
                    }
                }catch (Exception e){
                    LogUtil.msgLog.error("pf.properties:key="+kv.getKey()+",value="+kv.getValue()+",Exception:"+e.getMessage(),e);
                }
            }
        }

        LogUtil.msgLog.error("load pfConfig count="+(pfConfigs==null?0:pfConfigs.size()));

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
            LogUtil.errorLog.info("pfsdk is null:" + pf);
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
            LogUtil.errorLog.info("pfGZH is null:" + pf);
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
