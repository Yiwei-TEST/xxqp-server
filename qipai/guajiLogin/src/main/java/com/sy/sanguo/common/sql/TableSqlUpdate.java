package com.sy.sanguo.common.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sy.sanguo.game.dao.SqlDaoImpl;

public class TableSqlUpdate {
	private static Map<String, Map<String, String>> sqlMap = new HashMap<String, Map<String, String>>();
	private static Map<String, String> orderValidateMap = new HashMap<String, String>();

	static {
		sqlMap.put("order_validate", orderValidateMap);
		orderValidateMap.put("sdk_order_id", "ALTER TABLE order_validate ADD COLUMN `sdk_order_id` varchar(100)  DEFAULT '' COMMENT 'SDK订单号';");
	}

	public static void check(SqlDaoImpl sqlDao) {
		if (!sqlMap.isEmpty()) {
			for (Entry<String, Map<String, String>> entry : sqlMap.entrySet()) {
				checkTable(sqlDao, entry.getKey(), entry.getValue());
			}
		}
	}

	private static void checkTable(SqlDaoImpl sqlDao, String tableName, Map<String, String> sqlMap) {
		List<Map<String, Object>> list = sqlDao.showcolumns(tableName);
		if (list != null) {
			for (Entry<String, String> entry : sqlMap.entrySet()) {
				boolean find = false;
				for (Map<String, Object> columns : list) {
					String field = columns.get("Field").toString();
					if (field.equals(entry.getKey())) {
						find = true;
						break;
					}
				}
				if (!find) {
					sqlDao.update(entry.getValue());
				}
			}

		}
	}
}
