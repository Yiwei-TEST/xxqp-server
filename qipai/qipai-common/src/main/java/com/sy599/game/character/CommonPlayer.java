package com.sy599.game.character;

import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.msg.serverPacket.TableRes;

import java.util.List;

public class CommonPlayer extends Player {

    @Override
    public void initPlayInfo(String data) {

    }

    @Override
    public void clearTableInfo() {

    }

    @Override
    public SharedConstants.player_state getState() {
        return null;
    }

    @Override
    public int getIsEntryTable() {
        return 0;
    }

    @Override
    public void setIsEntryTable(int tableOnline) {

    }

    @Override
    public int getSeat() {
        return 0;
    }

    @Override
    public void setSeat(int randomSeat) {

    }

    @Override
    public void changeState(SharedConstants.player_state entry) {

    }

    @Override
    public void initNext() {

    }

    @Override
    public List<Integer> getHandPais() {
        return null;
    }

    @Override
    public String toInfoStr() {
        return null;
    }

    @Override
    public TableRes.PlayerInTableRes.Builder buildPlayInTableInfo() {
        return null;
    }

    @Override
    public void initPais(String handPai, String outPai) {

    }

    @Override
    public void endCompetition1() {

    }
}
