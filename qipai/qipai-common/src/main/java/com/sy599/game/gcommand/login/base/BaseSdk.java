package com.sy599.game.gcommand.login.base;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.gcommand.login.base.pfs.configs.PfSdkConfig;
import com.sy599.game.gcommand.login.base.pfs.configs.PfUtil;

import java.util.Map;

public class BaseSdk{

    protected String pf;
    private String opt;
    private String sdkId;
    private String ext;
    protected JSONObject params;

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public void setSdkId(String sdkId) {
        this.sdkId = sdkId;
    }

    public String getSdkId() {
        return sdkId;
    }

    public String loginExecute(){
        return null;
    }

    public Map<String, Object> refreshRole(RegInfo regInfo, String info) throws Exception {
        return null;
    }

    public void createRole(RegInfo regInfo, String info) throws Exception {

    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public String getOpt() {
        return opt;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public String getPf() {
        return this.pf;
    }

    public PfSdkConfig getPfSdkConfig() {
        return getPfSdkConfig(pf);
    }

    public PfSdkConfig getPfSdkConfig(String pf) {
        return PfUtil.getConfig(pf);
    }

}
