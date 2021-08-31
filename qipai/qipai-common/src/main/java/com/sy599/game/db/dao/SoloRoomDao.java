package com.sy599.game.db.dao;

import com.sy599.game.db.bean.SoloRoomConfig;
import com.sy599.game.db.bean.gold.GoldRoomConfig;

import java.util.List;

public class SoloRoomDao extends BaseDao {

    private static SoloRoomDao groupDao = new SoloRoomDao();

    public static SoloRoomDao getInstance() {
        return groupDao;
    }

    public List<SoloRoomConfig> loadAllGoldRoomConfig() throws Exception {
        return (List<SoloRoomConfig>) this.getSqlLoginClient().queryForList("soloRoom.load_all_solo_room_config");
    }


}
