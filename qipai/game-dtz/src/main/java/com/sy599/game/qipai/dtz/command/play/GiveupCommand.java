package com.sy599.game.qipai.dtz.command.play;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.dtz.bean.DtzPlayer;
import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.qipai.dtz.tool.CardTypeToolDtz;
import com.sy599.game.qipai.dtz.tool.DtzSendLog;

/**
 * 玩家不要
 * @author zhouhj
 */
public class GiveupCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		BaseTable basetable = player.getPlayingTable(); 
		//判断实体对象
		if (basetable == null)return;
		if (basetable.getClass() != DtzTable.class)return;
		DtzTable dtztable = (DtzTable)basetable;
		DtzPlayer pdkplayer = (DtzPlayer) player;

		//同步处理，防止客户端连点
		synchronized (dtztable) {
			if (dtztable.getNowDisCardSeat() != 0 && dtztable.getNowDisCardSeat() != player.getSeat()) {
				DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要-->出牌位置错误-->","nowDisCardSeat:"+dtztable.getNextDisCardSeat()+",seat:"+player.getSeat());
				return;
			}	
		}

        //获取客户端参数列表并打印
        int param[] = new int[req.getParamsCount()];
        for (int i = 0; i <= param.length - 1; i ++) {
            param[i] = req.getParams(i);
        }
        StringBuffer params = new StringBuffer();
        params.append("客户端参数：[");
        for(int i:param) params.append(i).append(",");
        params.append("]");
//        params.append(",玩家位置：").append(player.getSeat());
//        params.append(",牌局结算状态：").append(dtztable.jiesuan);
//        params.append(",当前小局 : ").append(dtztable.dtzRound);
//        params.append(",是否不是当局第一人出不要 : ").append(dtztable.masterFP);
//        params.append(",当局结束状态：").append(dtztable.isOver());
//        params.append(",双方已经都出完牌的组号：").append(dtztable.getWinGroup());
//        params.append(",结算玩家位置：").append(dtztable.settSeat);
//        DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要",params.toString());

		//判断牌局是否结算，防止多次点击
		if (dtztable.jiesuan)return;
		//判断是否不是当局第一人出不要,如果是第一个人出不要,不处理
		if (!dtztable.masterFP)return;
		//判断三人要的起必压
		if(dtztable.isThreePlayer() && CardTypeToolDtz.isCanPlay(player.getHandPais(), dtztable.getNowDisCardIds(), dtztable) == 1){ //三人玩法添加牌必压
			return;
        }
		dtztable.giveup(pdkplayer, req.getParamsCount());
		/*
		//判断是否该局结束
		if (dtztable.isOver()){
			//四人玩法才有
			if(dtztable.isFourPlayer()){
				//得到两人都出完牌的一组,需要进行结算
				int wingroup = dtztable.getWinGroup();
				//设置最终结算的玩家位置
				if (dtztable.settSeat==0){
					if(wingroup==1){
						//A组结束，设置B组某位玩家为结算位置
						if (player.getSeat()==2)dtztable.settSeat = 4;
						else if(player.getSeat()==4)dtztable.settSeat = 2;
						else return;//发生错误,角色的位置不正确，前面有打印，直接返回
					}else if(wingroup==2) {
						//B组结束，设置A组某位玩家为结算位置
						if(player.getSeat()==1)dtztable.settSeat = 3;
						else if(player.getSeat()==3)dtztable.settSeat = 1;
						else return;//发生错误,角色的位置不正确，前面有打印，直接返回
					}
				}
				//疑问代码，当牌桌设置的结算位置没有手牌时，切到同组的对面玩家
				if (dtztable.getSeatMap().get(dtztable.settSeat).getHandPais().size() == 0){
					if (wingroup == 1) {
						if (dtztable.settSeat == 2)dtztable.settSeat = 4;
						else if (dtztable.settSeat == 4)dtztable.settSeat = 2;
						else return;//发生错误,角色的位置不正确，前面有打印，直接返回
					}else if (wingroup == 2) {
						if (dtztable.settSeat == 1)dtztable.settSeat = 3;
						else if (dtztable.settSeat == 3)dtztable.settSeat = 1;
						else return;//发生错误,角色的位置不正确，前面有打印，直接返回
					}
		    		DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要===异常===切到同组的对面玩家","计算位置："+dtztable.settSeat);
				}
			}
			params = new StringBuffer();
			params.append("客户端参数：[");
			for(int i:param) params.append(i).append(",");
			params.append("]");
			params.append(",玩家位置：").append(player.getSeat());
			params.append(",牌局结算状态：").append(dtztable.jiesuan);
			params.append(",当前小局 : ").append(dtztable.dtzRound);
			params.append(",是否不是当局第一人出不要 : ").append(dtztable.masterFP);
			params.append(",当局结束状态：").append(dtztable.isOver());
			//params.append(",双方已经都出完牌的组号：").append(dtztable.getWinGroup());
			params.append(",结算玩家位置：").append(dtztable.settSeat);
			DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，定位结算位置后",params.toString());
			//发送玩家不要的消息到客户端
			if (dtztable.settSeat != 0) {
				//如果结算位置是玩家所在位置，进行结算
				if (player.getSeat() == dtztable.settSeat) {
					//给客户端各个玩家发送不要的消息
					dtztable.sendNotletInfo(pdkplayer);
					//设置本轮赢家
					DtzPlayer winPlayer = pdkplayer;
					if (dtztable.scorePlayerTemp != null) {
						//此处scorePlayerTemp肯定不会为空，结束时一定有积分赢家
						winPlayer = dtztable.scorePlayerTemp;
						dtztable.scorePlayerTemp = null;
						DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，双关后设置本轮积分赢家","积分赢家："+winPlayer.getUserId()+"["+winPlayer.getName()+"]");
					}else{
						//scorePlayerTemp为空，发生了异常
						DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，双关后设置本轮积分赢家异常","积分赢家："+winPlayer.getUserId()+"["+winPlayer.getName()+"]");
					}
					//增加分数到本轮赢者
					winPlayer.setPoint(winPlayer.getPoint() + dtztable.getScore());
					//增加地炸筒子分到本轮赢者
					winPlayer.setRoundScore(winPlayer.getRoundScore() + dtztable.getTzScore());
					LogUtil.msg("双关后玩家:" + winPlayer + " score : " + winPlayer.getPoint() + " - " + winPlayer.getRoundScore());
					dtztable.changeDisCardRound(1); //增加轮数
					int group = dtztable.findGroupByPlayer(winPlayer);//
					dtztable.addScoreBuGroup(group, dtztable.getScore()); //为这一组加分
					dtztable.clearRoundScore(); //清理掉一局的分数
					dtztable.recordCardAll(dtztable.getCardTemp(), winPlayer);  //筒子放起来
					List<Integer> groupCount = dtztable.getAllTzOrXiGroupScore();
					int score_c = dtztable.getRoundFragmentCardSocre();
					for (Player  pp : dtztable.getPlayerMap().values()) {
						pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int)winPlayer.getUserId(), groupCount, score_c, 0);
					}
					dtztable.clearCardTemp(); //清掉牌的缓存
					LogUtil.msg("table" + dtztable.getId() + " 双关后在 要不起的状态， 而且  打完之后 将进入 结算.");
					dtztable.delayedSettlement(player.getSeat());
					return ;
				}
			}else{
				dtztable.sendNotletInfo(pdkplayer);
				//得到要结算加积分的玩家
				DtzPlayer winPlayer = pdkplayer;
				if (dtztable.scorePlayerTemp != null) {
					DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，计算加分玩家","原加分玩家是 "+winPlayer.getUserId()+"["+winPlayer.getName()+"],现加分玩家是："+dtztable.scorePlayerTemp.getUserId()+"["+dtztable.scorePlayerTemp.getName()+"]");
					winPlayer = dtztable.scorePlayerTemp;
					dtztable.scorePlayerTemp = null;
				}
				//设置玩家当前轮加的牌面分，不包含筒子、地炸、囍的分
				winPlayer.setPoint(winPlayer.getPoint() + dtztable.getScore());
				//设置玩家当前轮加的筒子、地炸、囍的分
				winPlayer.setRoundScore(winPlayer.getRoundScore() + dtztable.getTzScore());
				DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，进入本局结算中的本轮结算胜出的玩家","胜出玩家是:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",牌面分："+winPlayer.getPoint()+",筒子分："+winPlayer.getRoundScore());
				//增加轮数
				dtztable.changeDisCardRound(1);
				//得到赢的那组
				int group = dtztable.findGroupByPlayer(winPlayer);//
				//为这一组加该轮积分
				dtztable.addScoreBuGroup(group, dtztable.getScore());
				//清理牌桌该轮的分数
				dtztable.clearRoundScore();
				//筒子牌放起来
				dtztable.recordCardAll(dtztable.getCardTemp(), winPlayer);
				//得到全局筒子的总分 A组， B组
				List<Integer> groupCount = dtztable.getAllTzOrXiGroupScore();
				//得到该轮5,10，k的所有积分和
				int score_c = dtztable.getRoundFragmentCardSocre();
				//推送给玩家
				for (Player  pp : dtztable.getPlayerMap().values()) {
					pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int)winPlayer.getUserId(), groupCount, score_c, 0);
				}
				//清掉牌的缓存
				dtztable.clearCardTemp();
				//本轮结算结束，进入本局结算
				DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，进入本局结算","胜出玩家是:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",牌面分："+winPlayer.getPoint()+",筒子分："+winPlayer.getRoundScore());
				//开始本局结算
				dtztable.delayedSettlement(player.getSeat());
				return;
			}
		}
		//牌局未结束，继续运行
		
		//得到此轮其他人是否都不要
		Pair<Boolean, DtzPlayer> pair = dtztable.isDoneRound1();
		//其他人都不要，这段有问题，不会出现这种情况
		if(pair.getValue0()){
			DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要===异常===其他三家都不要，最后出牌人是自己的",params.toString());
			//清空当前牌桌上此轮出的牌
			dtztable.getNowDisCardIds().clear();
			if(pair.getValue1().equals(player)){
				//如果此时最后出牌的人是自己，这是一个异常情况，打印后返回
				DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要===异常二次===其他三家都不要，最后出牌人是自己的",params.toString());
				return;
			}
		}
		//给自己客户端推送不要的消息及传过来的参数
		pdkplayer.writeComMessage(WebSocketMsgType.REQ_COM_GIVEUP, param);
        //计算下一个出牌的玩家
        synchronized(dtztable){
            //设置当前存储玩家要不起玩家数量对象
            dtztable.changePlayTimes(false, pdkplayer);
            //要不起逻辑处理
            dtztable.notLet(pdkplayer);
        }
        DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，计算下一个出牌的玩家","玩家要不起 下一个玩家的位置是 " + dtztable.getNowDisCardSeat());
//			LogUtil.msg("table:" + dtztable.getId() + "位置:" + player.getSeat() + "的玩家要不起 下一个玩家的位置是 " + dtztable.getNowDisCardSeat());
        //判定逻辑,看是否本轮已经结束
        pair = dtztable.isDoneRound1();
        if (pair.getValue0()) {
            //没有人要的起了，本轮结束，开始给玩家加分
            //清空本轮牌面的牌
            dtztable.getNowDisCardIds().clear();
            //得到加分玩家
            DtzPlayer winPlayer = pair.getValue1();
            if (dtztable.scorePlayerTemp != null) {
                DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，计算加分玩家","原加分玩家是 "+winPlayer.getUserId()+"["+winPlayer.getName()+"],现加分玩家是："+dtztable.scorePlayerTemp.getUserId()+"["+dtztable.scorePlayerTemp.getName()+"]");
                winPlayer = dtztable.scorePlayerTemp;
                dtztable.scorePlayerTemp = null;
            }
            //给玩家加分
            //设置玩家当前轮加的牌面分，不包含筒子、地炸、囍的分
            winPlayer.setPoint(winPlayer.getPoint() + dtztable.getScore()); //加分数
            //设置玩家当前轮加的筒子、地炸、囍的分
            winPlayer.setRoundScore(winPlayer.getRoundScore() + dtztable.getTzScore());
            DtzSendLog.sendCardLog(dtztable.getId(), player.getUserId(), player.getName(), "玩家不要，计算本轮胜出的玩家","胜出玩家是:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",牌面分："+winPlayer.getPoint()+",筒子分："+winPlayer.getRoundScore());
//				LogUtil.msg("玩家:" + winPlayer + " score : " + winPlayer.getPoint() + " - " + winPlayer.getRoundScore());
            //给本轮胜出组进行加分
            int group = dtztable.findGroupByPlayer(winPlayer);
            //为这一组加牌面分
            dtztable.addScoreBuGroup(group, dtztable.getScore());
            //清理掉该轮牌桌的牌面分和筒子分
            dtztable.clearRoundScore();
            //记录本轮出牌的情况
            dtztable.recordCardAll(dtztable.getCardTemp(), winPlayer);  //筒子放起来
            //得到全局各组的筒子分总和
            List<Integer> groupCount = dtztable.getAllTzOrXiGroupScore();
            //得到该轮5,10，k的所有积分和
            int score_c = dtztable.getRoundFragmentCardSocre();
            //推送给玩家
            for (Player  pp : dtztable.getPlayerMap().values()) {
                pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int)winPlayer.getUserId(), groupCount, score_c);
            }
            //清理本轮牌的缓存
            dtztable.clearCardTemp(); //清掉牌的缓存
        }*/
    }
	

	@Override
	public void setMsgTypeMap() {
	}
	
}
