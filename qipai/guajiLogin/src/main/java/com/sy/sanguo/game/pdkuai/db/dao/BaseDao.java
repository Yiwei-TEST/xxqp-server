package com.sy.sanguo.game.pdkuai.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;

public class BaseDao{
	public SqlMapClient getSql() {
		return SharedConstants.sqlClient;
	}
	
	public SqlMapClient getFuncSql() {
		return SharedConstants.sqlFuncClient;
	}
}
