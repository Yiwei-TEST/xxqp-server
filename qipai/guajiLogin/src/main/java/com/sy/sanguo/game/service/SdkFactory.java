package com.sy.sanguo.game.service;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;
import com.sy.sanguo.game.dao.RoomCardDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.pdkuai.db.dao.GameUserDao;
import com.sy.sanguo.game.service.channel.aibei.Aibei;
import com.sy.sanguo.game.service.pfs.*;
import com.sy.sanguo.game.service.pfs.apple.Apple;
import com.sy.sanguo.game.service.pfs.qq.QQ;
import com.sy.sanguo.game.service.pfs.weixin.Weixin;
import com.sy.sanguo.game.service.pfs.xianliao.Xianliao;

import javax.servlet.http.HttpServletRequest;

public class SdkFactory {

    public static IMobileSdk getLoginInst(String pf, HttpServletRequest request) {
        return getInst(pf, request, null, null, null, null,null);
    }

    public static IMobileSdk getLoginInst(String pf, HttpServletRequest request, UserDaoImpl userDao, GameUserDao gameUserDao) {
        return getInst(pf, request, null, null, userDao, gameUserDao,null);
    }

    public static IMobileSdk getLoginInst(String pf, HttpServletRequest request, UserDaoImpl userDao) {
        return getInst(pf, request, null, null, userDao, null,null);
    }

    public static IMobileSdk getPayInst(String pf, HttpServletRequest request, OrderDaoImpl orderDao) {
        return getInst(pf, request, orderDao, null, null, null,null);
    }

    public static IMobileSdk getInst(String pf, HttpServletRequest request, OrderDaoImpl orderDao, OrderValiDaoImpl orderValiDao, UserDaoImpl userDao) {
        return getInst(pf, request, orderDao, orderValiDao, userDao, null,null);
    }

    public static IMobileSdk getInst(String pf, HttpServletRequest request, OrderDaoImpl orderDao, OrderValiDaoImpl orderValiDao) {
        return getInst(pf, request, orderDao, orderValiDao, null, null,null);
    }

    public static IMobileSdk getInst(String pf, HttpServletRequest request, OrderDaoImpl orderDao, OrderValiDaoImpl orderValiDao, UserDaoImpl userDao, GameUserDao gameUserDao, RoomCardDaoImpl roomCardDao) {
        BaseSdk inst = null;
        if ("aibei".equals(pf)) {
            inst = new Aibei();
        } else if ("upstream".equals(pf)) {
            inst = new Upstream();
        } else if ("yypt".equals(pf)) {
            inst = new Yypt();
        } else if ("apple".equals(pf)) {
            inst = new Apple();
        } else if ("tonglian".equals(pf)) {
            inst = new Tonglian();
        } else if (pf.startsWith("weixin")) {
            inst = new Weixin();
        } else if (pf.startsWith("futong")) {
            inst = new WeiFuTong();
        } else if (pf.startsWith("webfutong")) {
            inst = new WebWeiFuTong();
        } else if (pf.startsWith("webchanyou")) {
            inst = new WebChanYou();
        } else if (pf.startsWith("webzyf")) {
            inst = new WebZyf();
        } else if (pf.startsWith("xianliao")) {
            inst = new Xianliao();
        } else if (pf.startsWith("qq")) {
            inst = new QQ();
        } else if (pf.startsWith("webunpay")) {
            inst = new WebUnPay();
        } else if (pf.startsWith("webwmpay")) {
            inst = new WebWmPay();
        } else if (pf.startsWith("xftpay")) {
            inst = new XftPay();
        } else if (pf.startsWith("h5zyf")) {
            inst = new H5Zyf();
        }

        // else if ("webyy".equals(pf)) {
        // inst = new WebYY();
        // }
        if (inst == null) {
            GameBackLogger.SYS_LOG.error("inst not found::" + pf);
        } else {
            inst.setRequest(request);
            inst.setOrderDao(orderDao);
            inst.setPf(pf);
            inst.setOrderValiDao(orderValiDao);
            inst.setUserDao(userDao);
            inst.setGameUserDao(gameUserDao);
            inst.setRoomCardDao(roomCardDao);
            inst.setSdkConfig(SysInfManager.getInstance().getSdkConfig(pf));
            inst.setPfConfig();
        }
        return inst;
    }
}
