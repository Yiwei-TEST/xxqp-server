package com.sy599.game.udplog;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.util.LogUtil;
import com.sy599.guaji.log.codec.LogMessageProtocalCodecFactory;
import com.sy599.guaji.log.message.AbstractMessage;
import com.sy599.guaji.log.message.ActionLogMessage;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 日志记录器，作为与日志服务器交互的接口
 *
 * @author taohuiliang
 * @date 2012-07-16
 */
public class UdpLogger implements IPlayerLogger {

    private static final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
    private volatile static IoSession clientSession;
    /**
     * 重新发起连接的次数
     **/
    private static int reConnectTimes;
    // private static final int MAX_RECONNECT_TIMES = 5;
    private static UdpLogger _inst = new UdpLogger();

    private UdpLogger() {
    }

    public static UdpLogger getInstance() {
        return _inst;
    }
//

    /**
     * 初始化与日志服务器之间的连接
     */
    public void connectToServer() {
        if(true){ // 20200331
            return;
        }
        if (reConnectTimes > 5) {
            LogUtil.errorLog.error("connect log server out times:" + reConnectTimes);
        }

        while (reConnectTimes <= 5) {
            if (atomicBoolean.getAndSet(false)) {
                if (!isConnected()) {
                    reConnectTimes++;

                    if (clientSession != null) {
                        clientSession.close(true);
                    }

                    LogUtil.monitorLog.info("start==times:" + reConnectTimes + ",LOGSERVER_IP:" + GameServerConfig.LOGSERVER_IP + ",LOGSERVER_PORT:" + GameServerConfig.LOGSERVER_PORT);

                    IoConnector connector = new NioDatagramConnector();
                    connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new LogMessageProtocalCodecFactory()));
                    connector.setHandler(new LogClientHandler());
                    ConnectFuture future = connector.connect(new InetSocketAddress(GameServerConfig.LOGSERVER_IP, GameServerConfig.LOGSERVER_PORT));
//			future.awaitUninterruptibly();
                    future.awaitUninterruptibly(10, TimeUnit.SECONDS);
                    clientSession = future.getSession();

                    if (isConnected()) {
                        LogUtil.monitorLog.info("connect success==times:" + reConnectTimes + ",LOGSERVER_IP:" + GameServerConfig.LOGSERVER_IP + ",LOGSERVER_PORT:" + GameServerConfig.LOGSERVER_PORT);
                    } else {
                        LogUtil.monitorLog.info("connect fail==times:" + reConnectTimes + ",LOGSERVER_IP:" + GameServerConfig.LOGSERVER_IP + ",LOGSERVER_PORT:" + GameServerConfig.LOGSERVER_PORT);
                    }
                }
                atomicBoolean.compareAndSet(false, true);
                return;
            }
        }
    }

    /**
     * 发送行为日志
     */
    public void sendActionLog(Player player, LogConstants reason, String properties/**
     *
     *
     *
     * 属性增减
     */
    ) {
        if(true){ // 20200331
            return;
        }

        LogUtil.monitorLog.info("action log:Reason={},date={},UserId={},cards={},FreeCards={},properties={},RecMsg={}"
                , reason, CommonUtil.dateTimeToString(), player.getUserId(), player.getCards()
                , player.getFreeCards(), properties, player.getRecMsg());

        ActionLogMessage actionMsg = new ActionLogMessage();
        // actionMsg.setCoin((int) player.getCoin());
        // actionMsg.setExp(player.getExp());
        // actionMsg.setLevel(player.getLevel());
        actionMsg.setReason(reason.val());
        actionMsg.setTime(new Date());
        actionMsg.setUserId(player.getUserId());
        actionMsg.setYuanbao((int) player.getCards());
        actionMsg.setBdyuanbao((int) player.getFreeCards());
        actionMsg.setProperties(properties == null ? "" : properties);
        String recMsg = "";
        if (player.getRecMsg() != null) {
            recMsg = player.getRecMsg().toString();
        }
        actionMsg.setRelatedItem(recMsg);
        writeLog(actionMsg);
    }

    /**
     * 发送行为日志
     *
     * @param player
     * @param reason
     * @param properties
     * @param logRes
     */
    public void sendActionLog(Player player, LogConstants reason, String properties, String logRes) {

        LogUtil.monitorLog.info("action log:Reason={},date={},UserId={},cards={},FreeCards={},properties={},RecMsg={}"
                , reason, CommonUtil.dateTimeToString(), player.getUserId(), player.getCards()
                , player.getFreeCards(), properties, player.getRecMsg());

        if(true){ // 20200331
            return;
        }
        ActionLogMessage actionMsg = new ActionLogMessage();
        actionMsg.setReason(reason.val());
        actionMsg.setTime(new Date());
        actionMsg.setUserId(player.getUserId());
        actionMsg.setYuanbao((int) player.getCards());
        actionMsg.setBdyuanbao((int) player.getFreeCards());
        actionMsg.setProperties(properties == null ? "" : properties);
        String recMsg = "";
        if (player.getRecMsg() != null) {
            recMsg = player.getRecMsg().toString();
        }
        actionMsg.setRelatedItem(recMsg);
        writeLog(actionMsg);
    }

    //
////	/**
////	 * 发送跨服行为日志
////	 *
////	 * @param player
////	 * @param reason
////	 * @param attach
////	 * @param extend
////	 *            void
////	 */
//	/*
//	 * public void sendCrossActionLog(CPlayer player, int reason, String attach,
//	 * String extend){ CrossActionLogMessage actionMsg = new
//	 * CrossActionLogMessage(); actionMsg.setUserId(player.getUserId());
//	 * actionMsg.setReason(reason); actionMsg.setAttach(attach ==
//	 * null?"":attach); actionMsg.setExtend(extend == null?"":extend);
//	 * actionMsg.setTime(new Date()); writeLog(actionMsg); }
//	 */
    private void writeLog(AbstractMessage message) {
        try {
            if (!isConnected()) {
                connectToServer();
            }
            if (isConnected()) {
                clientSession.write(message);
            }
        } catch (Exception e) {
            LogUtil.e("请检查日志工程-->writeLog err", e);
        }

    }
//
//	 public void sendSnapshotLog(long userId, String userInfo, String
//	 userItem) {
    // SnapshotLogMessage message=new SnapshotLogMessage(userId,
    // TimeUtil.now(),userInfo,userItem);
    // writeLog(message);
//	 }
//

    /**
     * 发送玩家快照
     */
    @Override
    public void sendSnapshotLog(Player player, int sign) {
//		Map<String, Object> infoMap = new HashMap<String, Object>();
//		infoMap.put("sign", sign);
//
//		Map<String, Object> bagMap = new HashMap<String, Object>();
//		String info = JacksonUtil.writeValueAsString(infoMap);
//		String bag = JacksonUtil.writeValueAsString(bagMap);
//		SnapshotLogMessage message = new SnapshotLogMessage(player.getUserId(), TimeUtil.now(), info, bag);
//		writeLog(message);
    }
//
//	// }
//

    /**
     * 发送玩家快照
     */
    public void sendSnapshotLog(long masterId, String info, String logres) {
//		SnapshotLogMessage message = new SnapshotLogMessage(masterId, TimeUtil.now(), info, logres);
//		writeLog(message);
    }

    //
    public boolean isConnected() {
        return clientSession != null && clientSession.isConnected();
    }

}
