package com.sy599.game.character;

import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes.Builder;

import java.util.List;

public class ProtoPlayer extends Player {

	@Override
	public void initPlayInfo(String data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearTableInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	public player_state getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIsEntryTable() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setIsEntryTable(int tableOnline) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getSeat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSeat(int randomSeat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeState(player_state entry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initNext() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Integer> getHandPais() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toInfoStr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder buildPlayInTableInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initPais(String handPai, String outPai) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endCompetition1() {
		// TODO Auto-generated method stub

	}

}
