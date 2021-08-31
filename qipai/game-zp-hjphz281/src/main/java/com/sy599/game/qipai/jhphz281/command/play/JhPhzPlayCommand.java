package com.sy599.game.qipai.jhphz281.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.jhphz281.bean.JhPhzPlayer;
import com.sy599.game.qipai.jhphz281.bean.JhPhzTable;
import com.sy599.game.qipai.jhphz281.util.JhPhzCardUtils;
import com.sy599.game.qipai.jhphz281.util.JhPhzConstants;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JhPhzPlayCommand extends BaseCommand {
	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		JhPhzTable table = player0.getPlayingTable(JhPhzTable.class);
		if (table == null) {
			return;
		}
		JhPhzPlayer player=(JhPhzPlayer)player0;

		// 该牌局是否能打牌
		int canPlay = table.isCanPlay();
		if (canPlay != 0) {
			if (canPlay == 1) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
			} else if (canPlay == 2) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_7));
			}
			return;
		}

		if (table.getState() != SharedConstants.table_state.play) {
			return;
		}
		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		int action = playCard.getCardType();
		List<Integer> cards = playCard.getCardIdsList();

//		if (action == 100){//取消托管
//			player.setAutoPlay(false,table);
//			player.setLastOperateTime(System.currentTimeMillis());
//			return;
//		}else if (action == 101){//托管
//			player.setAutoPlay(true,table);
//			return;
//		}

		if (action == JhPhzConstants.action_pao||action == JhPhzConstants.action_mo){
			return;
		}

		if (action == JhPhzConstants.action_pass) {
			int nowId=0;
			if (!cards.isEmpty()) {
				// 是否已经摸牌
				cards = new ArrayList<>(cards);
				nowId = cards.remove( 0);
			}

			synchronized (table){
				Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
				List<Integer> list;
				if (actionMap != null && (list=actionMap.get(player.getSeat()))!=null) {

					// 比较动作
					if (!compareVal(list, cards)) {
						LogUtil.errorLog.warn("actions not match:tableId="+table.getId()+",userId="+player.getUserId()+",current="+table.getPlayBureau()+",server="+list+",client="+cards+",nowId="+nowId);
						return;
					}

					if (list.get(7).intValue() == 1 || list.get(8).intValue() == 1 || list.get(9).intValue() == 1) {
						int nextCard=table.getNextLeftCard();
						LogUtil.monitorLog.info("action pass wang diao or chuang or zha:userId="+player.getUserId()+",tableId="+table.getId()+",card="+nextCard+",cardVal="+ JhPhzCardUtils.loadCardVal(nextCard)+",wang action="+list.get(7)+"_"+list.get(8)+"_"+list.get(9));
					}else{
						int lastCard=table.getLastCard();
						if(lastCard>80||nowId==0) {
							nowId= lastCard;
						}
						if (lastCard>0&&!table.isTianHu()) {
							
							if (lastCard != nowId) {
								LogUtil.errorLog.warn("card not match:tableId="+table.getId()+",userId="+player.getUserId()+",current="+table.getPlayBureau()+",server="+lastCard+",client="+nowId);
								return;
							}
						}
						cards.clear();
					}

					if (list.get(0).intValue() == 1) {
						LogUtil.monitorLog.info("action pass hu:userId="+player.getUserId()+",tableId="+table.getId()+",card="+table.getHuCard()+",cardVal="+ JhPhzCardUtils.loadCardVal(table.getHuCard()));
					}
				}
			}
			if (!cards.isEmpty()) {
				// 是否已经摸牌
				cards.clear();
			}
		} else {
			for (int cardId : cards) {
				if (cardId >= 200) {
					player.writeErrMsg("王霸不能打");
					return;
				}
			}
		}

		player.setAutoPlay(false,table);
		player.setLastOperateTime(System.currentTimeMillis());

		table.play(player, cards, action);

	}

	@Override
	public void setMsgTypeMap() {

	}

	public static final boolean compareVal(List<Integer> list0,List<Integer> list2){
		
		List<Integer> list1 = new ArrayList<>(list0);
		if(list1.size()>5){//过滤跑
			list1.set(5, 0);
		}
		int i=0,len1=list1.size(),len2=list2.size();
		if (len1>=len2){

			for(;i<len2;i++){
				if (list1.get(i).intValue()!=list2.get(i).intValue()){
					return false;
				}
			}
			for(;i<len1;i++){
				if (list1.get(i).intValue()!=0){
					return false;
				}
			}
		}else{
			for(;i<len1;i++){
				if (list1.get(i).intValue()!=list2.get(i).intValue()){
					return false;
				}
			}
			for(;i<len2;i++){
				if (list2.get(i).intValue()!=0){
					return false;
				}
			}
		}
		return true;
	}

}
