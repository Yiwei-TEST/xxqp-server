package com.sy.sanguo.common.executor;

//import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.mainland.util.db.DBDataSource;
import com.sy.mainland.util.db.MultiReadWriteSwitch;
import com.sy.mainland.util.db.SingleReadWriteSwitch;
import com.sy.sanguo.common.datasource.DruidDataSourceFactory;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.db.dao.FirstMythDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.SysInfManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MinuteTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MinuteTask.class);

	/** 分钟数量 **/
	private int mineCount = 0;

	@Override
	public void run() {
		mineCount++;
//		if (mineCount % 3 == 0) {
//			// 每3分钟
////			StaticDataManager.getMonitor();
//		}

		try {
			SysInfManager.initServer();
		}catch (Throwable t){
			LogUtil.e("Exception:"+t.getMessage(),t);
		}

		if (mineCount % 60 == 0) {
			mineCount = 0;
		}

		if ("1".equals(PropertiesCacheUtil.getValue("db_monitor",Constants.GAME_FILE))){
			try{
				if (SharedConstants.sqlClient!=null){
					Set<DruidDataSource> list0 = new HashSet<>();
					DataSource d = SharedConstants.sqlClient.getDataSource();
					if (d instanceof DruidDataSource){
						list0.add((DruidDataSource)d);
					} else if (d instanceof DelegatingDataSource){
						d=((DelegatingDataSource) d).getTargetDataSource();
						if (d instanceof DBDataSource){
							SingleReadWriteSwitch srws=((DBDataSource)d).getDefaultReadWriteSwitch();
							addDataSource(list0,srws);
							Object o = ((DBDataSource)d).getReadWriteSwitch();
							if (o!=null&&o!=srws){
								if (o instanceof SingleReadWriteSwitch) {
									srws = (SingleReadWriteSwitch) o;
									addDataSource(list0,srws);
								} else if (o instanceof MultiReadWriteSwitch) {
									MultiReadWriteSwitch mrws=(MultiReadWriteSwitch)o;
									for(SingleReadWriteSwitch s:mrws.getMultiReadWriteSwitchMap().values()){
										addDataSource(list0,s);
									}
								}
							}
						}
					}

					for (DruidDataSource dataSource:list0){
						LOGGER.info("database connections:url={},count={},active={},open={},close={}"
								,dataSource.getUrl(),dataSource.getPoolingCount()
								,dataSource.getActiveCount(),dataSource.getConnectCount(),dataSource.getCloseCount());
					}
				}

				DruidDataSourceFactory.msg();
			}catch (Throwable t){
				LogUtil.e("Exception:"+t.getMessage(),t);
			}
		}

		// 清理代开房间
		// DaikaiManager.getInstance().clearDaikaiTable();

		//检查排行榜更新
		try {
			FirstMythDao.getInstance().checkRefresh();
		}catch (Throwable t){
			LogUtil.e("Exception:"+t.getMessage(),t);
		}
	}

	private static void addDataSource(Set<DruidDataSource> list,SingleReadWriteSwitch srws){
		if (srws!=null){
			if (srws.getWrite() instanceof DruidDataSource){
				list.add((DruidDataSource)srws.getWrite());
			}
			Object read=srws.getRead();
			if (read!=null&&read!=srws.getWrite()){
				if (read instanceof DruidDataSource) {
					list.add((DruidDataSource)read);
				} else if (read instanceof List) {
					List<Object> temp = (List<Object>) read;
					for (Object ob:temp){
						if (ob instanceof DruidDataSource)
							list.add((DruidDataSource)ob);
					}
				} else if (read instanceof Map) {
					Object[] temp = ((Map) read).values().toArray();
					for (Object ob:temp){
						if (ob instanceof DruidDataSource)
							list.add((DruidDataSource)ob);
					}
				} else if (read instanceof Set) {
					Object[] temp = ((Set) read).toArray();
					for (Object ob:temp){
						if (ob instanceof DruidDataSource)
							list.add((DruidDataSource)ob);
					}
				} else if (read instanceof DataSource[]) {
					DataSource[] temp = (DataSource[]) read;
					for (Object ob:temp){
						if (ob instanceof DruidDataSource)
							list.add((DruidDataSource)ob);
					}
				}
			}
		}
	}

}
