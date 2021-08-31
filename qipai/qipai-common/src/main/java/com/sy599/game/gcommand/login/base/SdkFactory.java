package com.sy599.game.gcommand.login.base;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.gcommand.login.base.pfs.qq.QQ;
import com.sy599.game.gcommand.login.base.pfs.weixin.Weixin;
import com.sy599.game.gcommand.login.base.pfs.xianliao.Xianliao;
import com.sy599.game.util.LogUtil;

public final class SdkFactory {

    public static BaseSdk getInst(String pf, JSONObject params) {
        BaseSdk inst = null;

        if (pf.startsWith("weixin")) {
            inst = new Weixin();
        } else if (pf.startsWith("xianliao")) {
            inst = new Xianliao();
        } else if (pf.startsWith("qq")) {
            inst = new QQ();
        }

        if (inst == null) {
            LogUtil.msgLog.error("inst not found::" + pf);
        } else {
            inst.setPf(pf);
            inst.setParams(params);
        }
        return inst;
    }
}
