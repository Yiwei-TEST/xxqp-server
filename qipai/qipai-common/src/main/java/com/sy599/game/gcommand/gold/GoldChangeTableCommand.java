package com.sy599.game.gcommand.gold;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.Constants;
import com.sy599.game.util.GoldRoomUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.Map;

/**
 * 金币场换桌
 */
public class GoldChangeTableCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		if (GoldRoomUtil.isGoldRoom(player)){
			synchronized (Constants.GOLD_LOCK){

				BaseTable table = player.getPlayingTable();
				if (table!=null){
					synchronized (table){
						if(table.getState()==SharedConstants.table_state.play){
							player.writeErrMsg("正在游戏，不能换桌");
							return;
						}else{
							Map<Long,Player> map = table.getPlayerMap();
							if (map.size()==1&&map.containsKey(player.getUserId())){
								player.writeErrMsg("房间里只有你一人，不需要换桌");
								return;
							}
						}
					}
				}

				GoldRoomUser goldRoomUser = GoldRoomDao.getInstance().loadGoldRoomUser(player.getPlayingTableId(),player.getUserId());
				if (goldRoomUser!=null) {
					GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(goldRoomUser.getRoomId());
					if (goldRoom != null) {

					} else {

					}
				}else{

				}
			}
		}
	}

	@Override
	public void setMsgTypeMap() {
	}

}
