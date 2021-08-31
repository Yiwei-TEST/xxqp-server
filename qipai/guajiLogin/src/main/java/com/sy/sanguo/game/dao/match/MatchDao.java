package com.sy.sanguo.game.dao.match;

import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

import java.util.HashMap;

public class MatchDao extends BaseDao {
    private static MatchDao _inst = new MatchDao();

    public static MatchDao getInstance() {
        return _inst;
    }

    public HashMap<String, Object> selectOne(String matchId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("matchId", matchId);
        return (HashMap<String, Object>) getSql().queryForObject("gold.selectMatchBean", map);
    }
}
