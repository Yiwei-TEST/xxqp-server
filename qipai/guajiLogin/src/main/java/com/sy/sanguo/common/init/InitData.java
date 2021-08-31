package com.sy.sanguo.common.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.sy.mainland.util.redis.Redis;
import com.sy.sanguo.common.datasource.DruidDataSourceFactory;
import com.sy.sanguo.common.datasource.MySqlDruidDataSource;
import com.sy.sanguo.common.executor.task.OneMinuteFixedRateTask;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.KeyWordsFilter;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.common.util.TaskExecutor;
import com.sy.sanguo.game.bean.IpGroup;
import com.sy.sanguo.game.bean.MacGroup;
import com.sy.sanguo.game.bean.Room;
import com.sy.sanguo.game.bean.SystemBlack;
import com.sy.sanguo.game.bean.SystemChatMaskWord;
import com.sy.sanguo.game.dao.BlackDaoImpl;
import com.sy.sanguo.game.dao.MaskWordDao;
import com.sy.sanguo.game.dao.RoomDaoImpl;
import com.sy.sanguo.game.dao.SqlDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.db.dao.SystemCommonInfoDao;
import com.sy.sanguo.game.pdkuai.helper.FakeTableHepler;
import com.sy.sanguo.game.pdkuai.manager.StatisticsManager;
import com.sy.sanguo.game.pdkuai.manager.SystemCommonInfoManager;
import com.sy.sanguo.game.pdkuai.manager.TaskManager;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy.sanguo.game.utils.BjdUtil;
import com.sy.sanguo.game.utils.HttpDataUtil;
import com.sy.sanguo.game.utils.LoginUtil;
import com.sy599.sanguo.util.GroupConfigUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.SysPartitionUtil;
import com.sy599.sanguo.util.TimeUtil;
import net.sy599.common.security.SecuritConstant;
import net.sy599.common.security.SecuritConstantImpl;
import org.apache.commons.lang.StringUtils;

public class InitData {
	private UserDaoImpl userDao;
	private BlackDaoImpl blackDaoImpl;
	private MaskWordDao maskWordDao;
	private SqlDaoImpl sqlDao;
	public static Map<String, IpGroup> ipGroupMap = new HashMap<String, IpGroup>();
	public static Map<String, MacGroup> macGroupMap = new HashMap<String, MacGroup>();
	public static int state;
	public static int maxIpCount;
	public static int maxMacCount;
	public static List<String> ipWhite;
	public static List<String> macWhite;
	public static List<String> groupFilterWords = new ArrayList<>();
	public static int groupToQinYouQuan = 0;
	public static long refreshblacktime;
	public static long refreshMaskWordtime;
	public static Properties game_config_properties = new Properties();
	public static Properties redbag_properties = new Properties();
	public static Properties keyValueProperties = new Properties();

	public static String db_name_1kz = "";


	public void init() {
		try {
			System.out.println("redis status:connected="+ Redis.isConnected());
			checkSql();
			initDataBase();
			if (state != 0) {
				initIpGroup();
				initMacGroup();
			}
			initBlack();
			initMaskWord();
			initRoom();
			SystemCommonInfoManager.getInstance().initData();
			TaskManager.getInstance().init();
			StatisticsManager.getInstance().init();
//			TaskManagerNew.getInstance().init();
			FakeTableHepler.refreshFreeFakeHeadImgMap();
			StaticDataManager.getInstance().init();
			ResourcesConfigsUtil.initResourcesConfigs();
            SysPartitionUtil.init();
			LogUtil.i("init resources configs finished");
			TaskExecutor.getInstance().scheduleWithFixedRate(new OneMinuteFixedRateTask(), OneMinuteFixedRateTask.loadFirstExecuteDate(), 1 * 60 * 1000);
//			StaticDataManager.getMonitor();
			// 初始化屏蔽字
			KeyWordsFilter.getInstance().initData(SysInfManager.baseDir + "/WEB-INF/classes/csv/keywords.txt");
			KeyWordsFilter.getInstance_1().initData(SysInfManager.baseDir + "/WEB-INF/classes/csv/keywords_1.txt");

            // 加载俱乐部等级配置
            GroupConfigUtil.initGroupConfig();

			LangMsg.loadLangMsg();
			BjdUtil.init();
		}catch (Exception e){
			LogUtil.e("init error:"+e.getMessage(),e);
		}
	}

	private void checkSql() {
//		TableMobileReg.check(sqlDao);
//		TableSqlUpdate.check(sqlDao);
	}

	private static Properties loadFromFile(String dir) throws Exception {
		Properties properties=new Properties();
		File file = new File(dir);
		if (!file.exists() || !file.isFile()){
			return new Properties();
		}
		try {
			FileInputStream fis = new FileInputStream(dir);
			properties.load(fis);
			fis.close();
		} catch (Exception e) {
			throw e;
		}
		return properties;
	}

	private void initDataBase() {
		Reader reader;
		try {

			game_config_properties=loadFromFile(SysInfManager.baseDir + "/WEB-INF/config/game.properties");
			keyValueProperties=loadFromFile(SysInfManager.baseDir + "/WEB-INF/config/keyV.properties");

			if(game_config_properties.containsKey("groupFilterWords")) {
				groupFilterWords = Arrays.asList(StringUtil.explodeToStringArray(((String)game_config_properties.get("groupFilterWords")), ","));
			}
			if(game_config_properties.containsKey("groupToQinYouQuan")) {
				groupToQinYouQuan = Integer.parseInt((String)game_config_properties.get("groupToQinYouQuan"));
			}
			redbag_properties=loadFromFile(SysInfManager.baseDir + "/WEB-INF/config/redbag.properties");

			Properties jdbcProperties = new Properties();
			StringUtil.loadFromFile(jdbcProperties, SysInfManager.baseDir + "/WEB-INF/config/jdbc.properties");
			reader = Resources.getResourceAsReader("config/sqlMapConfigLog.xml");
			String classDriver = jdbcProperties.getProperty("jdbc.ClassDriver");
			String pw = jdbcProperties.getProperty("jdbc.funcpassword");
			String url = jdbcProperties.getProperty("jdbc.funcurl");
			String user = jdbcProperties.getProperty("jdbc.funcuser");

//			 String pw = jdbcProperties.getProperty("jdbc.password");
//			 String url = jdbcProperties.getProperty("jdbc.url");
//			 String user = jdbcProperties.getProperty("jdbc.user");

			if (StringUtils.isBlank(pw) || StringUtils.isBlank(url)) {
				GameBackLogger.SYS_LOG.info("func url is null");
				return;
			}

			jdbcProperties = new Properties();
			SecuritConstant des = new SecuritConstantImpl();
			jdbcProperties.setProperty("jdbc.password", des.decrypt(pw));
			jdbcProperties.setProperty("jdbc.url", url);
			jdbcProperties.setProperty("jdbc.user", user);
			jdbcProperties.setProperty("jdbc.ClassDriver", classDriver);
			SharedConstants.sqlFuncClient = SqlMapClientBuilder.buildSqlMapClient(reader, jdbcProperties);
			db_name_1kz = MySqlDruidDataSource.loadDbNameFromUrl(url);

            reader.close();
			int select = SystemCommonInfoDao.getInstance().selectFuncOne();
			LogUtil.i("func select:" + select);

            if(game_config_properties.containsKey("md5_key_phoneNum")) {
                LoginUtil.setMd5KeyPhoneNum((String) game_config_properties.get("md5_key_phoneNum"));
            }

            if(game_config_properties.containsKey("aes_key_phoneNum")) {
                LoginUtil.setAESKeyPhoneNum((String) game_config_properties.get("aes_key_phoneNum"));
            }

			if(game_config_properties.containsKey("http_data_aes_switch")) {
				HttpDataUtil.setHttpDataAESSwitch((String)game_config_properties.get("http_data_aes_switch"));
			}
			if(game_config_properties.containsKey("http_data_aes_id")) {
				HttpDataUtil.setHttpDataAESId((String)game_config_properties.get("http_data_aes_id"));
			}
			if(game_config_properties.containsKey("http_data_aes_key")) {
				HttpDataUtil.setHttpDataAESKey((String)game_config_properties.get("http_data_aes_key"));
			}
		} catch (Exception e) {
			LogUtil.e("initDataBase err:", e);
		}
	}

	private void test() throws Exception {
		Reader reader;
		Properties jdbcProperties = new Properties();
		StringUtil.loadFromFile(jdbcProperties, SysInfManager.baseDir + "/WEB-INF/config/jdbc.properties");
		reader = Resources.getResourceAsReader("config/sqlMapConfigLog.xml");
		String md5Password = jdbcProperties.getProperty("jdbc.password");
		SecuritConstant des = new SecuritConstantImpl();
		jdbcProperties.setProperty("jdbc.password", des.decrypt(md5Password));
		SharedConstants.sqlFuncClient = SqlMapClientBuilder.buildSqlMapClient(reader, jdbcProperties);
		reader.close();
		int select = SystemCommonInfoDao.getInstance().selectFuncOne();
		System.out.println(select);
	}

	public static void main(String[] args) {
		InitData da = new InitData();
		da.initDataBase();
		try {
			da.test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void refreshBlack() {
		long now = TimeUtil.currentTimeMillis();
		if (now - refreshblacktime < TimeUtil.MIN_IN_MINILLS) {
			return;
		}
		List<SystemBlack> list = blackDaoImpl.selectNoLoadSystemBlack();
		for (SystemBlack values : list) {
			if (!StringUtils.isBlank(values.getIp())) {
				List<String> valList = SysInfManager.getInstance().getIpBlackList();
				if (!valList.contains(values.getIp())) {
					valList.add(values.getIp());
				}
			}

			if (!StringUtils.isBlank(values.getMac())) {
				List<String> valList = SysInfManager.getInstance().getMacBlackList();
				if (!valList.contains(values.getMac())) {
					valList.add(values.getMac());
				}
			}

			if (!StringUtils.isBlank(values.getDeviceCode())) {
				List<String> valList = SysInfManager.getInstance().getDcBlackList();
				if (!valList.contains(values.getDeviceCode())) {
					valList.add(values.getDeviceCode());
				}
			}

			if (!StringUtils.isBlank(values.getFlatId())) {
				List<String> valList = SysInfManager.getInstance().getFlatIdBlackList();
				if (!valList.contains(values.getFlatId())) {
					valList.add(values.getFlatId());
				}
			}
		}
		if (list != null && list.size() > 0) {
			GameBackLogger.SYS_LOG.info("refreshBlack count-->" + list.size());

		}
		refreshblacktime = now;
	}

	public void initMaskWord() {
		List<SystemChatMaskWord> list = maskWordDao.getList();
		List<String> maskWorld = new ArrayList<String>();
		for (SystemChatMaskWord values : list) {
			maskWorld.add(values.getMaskWord());
		}
		SysInfManager.getInstance().setMaskWordList(maskWorld);
		maskWordDao.updateLoad();
	}

	public void initBlack() {
		try {
			List<String> ipBlackList = new ArrayList<String>();
			List<String> macBlackList = new ArrayList<String>();
			List<String> dcBlackList = new ArrayList<String>();
			List<String> flatIdBlackList = new ArrayList<String>();
			List<SystemBlack> list = blackDaoImpl.selectSystemBlack();
			for (SystemBlack values : list) {
				if (!StringUtils.isBlank(values.getIp())) {
					ipBlackList.add(values.getIp());
				}
				if (!StringUtils.isBlank(values.getMac())) {
					macBlackList.add(values.getMac());
				}
				if (!StringUtils.isBlank(values.getDeviceCode())) {
					dcBlackList.add(values.getDeviceCode());
				}
				if (!StringUtils.isBlank(values.getFlatId())) {
					flatIdBlackList.add(values.getFlatId());
				}
			}
			SysInfManager.getInstance().setIpBlackList(ipBlackList);
			SysInfManager.getInstance().setMacBlackList(macBlackList);
			SysInfManager.getInstance().setDcBlackList(dcBlackList);
			SysInfManager.getInstance().setFlatIdBlackList(flatIdBlackList);
			blackDaoImpl.updateLoad();
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.info("refreshblack err:", e);
		}

	}

	private void initIpGroup() {
		try {
			List<IpGroup> list = userDao.getIpGroup();
			for (IpGroup ipGroup : list) {
				if (StringUtils.isBlank(ipGroup.getIp()) || ipGroup.getIp().equals("127.0.0.1")) {
					continue;
				}
				ipGroupMap.put(ipGroup.getIp(), ipGroup);
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("InitData--initIpGroup--exception", e);
		}
	}

	/**
	 * 查询空闲房间号
	 * 
	 * @return 房间号
	 * @throws
	 */
	private long queryRoomUsed() {
		try {
			return RoomDaoImpl.getInstance().queryRoomUsed();
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("InitData--queryRoomUsed--exception", e);
		}
		return -1;
	}

	/**
	 * 初始化所有房间
	 */
	private void initRoom() {
		final int count=899999;
		final long used=queryRoomUsed();
		// 初始房间号
		final long starNo = 100001L;
		// 如果没有房间
        new Thread(new Runnable() {
			@Override
			public void run() {
				if (used < count) {
					long starttime = TimeUtil.currentTimeMillis();
					GameBackLogger.SYS_LOG.info("initRoom start:" + starttime);
					// 所有房间
//					List<Long> rooms = new ArrayList<Long>();
					int cur=0;
					for (int i = 0; i < count; i++) {
						long roomId=starNo+i;
						Room room = new Room();
						room.setRoomId(roomId);
						room.setUsed(0);

						try {
							Room room1=RoomDaoImpl.getInstance().queryRoomByRoomId(roomId);
							if (room1==null){
								cur++;
								RoomDaoImpl.getInstance().addRoom(room);
							}
						} catch (Exception e) {
							GameBackLogger.SYS_LOG.error("InitData--initRoom--exception", e);
						}
						if (cur % 1000 == 0) {
							GameBackLogger.SYS_LOG.info("initRoom i:" + cur);
						}
					}
					long endtime = TimeUtil.currentTimeMillis();
					GameBackLogger.SYS_LOG.info("initRoom time:" + (endtime - starttime));
				}
			}
		}).start();
	}

	private void initMacGroup() {
		try {
			List<MacGroup> list = userDao.getMacGroup();
			for (MacGroup macGroup : list) {
				if (StringUtils.isBlank(macGroup.getMac()) || macGroup.getMac().equals("00:00:00:00:00:00")) {
					continue;
				}
				macGroupMap.put(macGroup.getMac(), macGroup);
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("InitData--initMacGroup--exception", e);
		}
	}

	public void setIpWhite(String ipWhite) {
		if (!StringUtils.isBlank(ipWhite)) {
			InitData.ipWhite = Arrays.asList(ipWhite.split(","));
		} else {
			InitData.ipWhite = new ArrayList<String>();
		}
	}

	public void setMacWhite(String macWhite) {
		if (!StringUtils.isBlank(macWhite)) {
			InitData.macWhite = Arrays.asList(macWhite.split(","));
		} else {
			InitData.macWhite = new ArrayList<String>();
		}
	}

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}

	public void setState(int state) {
		InitData.state = state;
	}

	public void setMaxIpCount(int maxIpCount) {
		InitData.maxIpCount = maxIpCount;
	}

	public void setMaxMacCount(int maxMacCount) {
		InitData.maxMacCount = maxMacCount;
	}

	public BlackDaoImpl getBlackDaoImpl() {
		return blackDaoImpl;
	}

	public void setBlackDaoImpl(BlackDaoImpl blackDaoImpl) {
		this.blackDaoImpl = blackDaoImpl;
	}

	public void setMaskWordDao(MaskWordDao maskWordDao) {
		this.maskWordDao = maskWordDao;
	}

	public SqlDaoImpl getSqlDao() {
		return sqlDao;
	}

	public void setSqlDao(SqlDaoImpl sqlDao) {
		this.sqlDao = sqlDao;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		SharedConstants.sqlClient = sqlMapClient;

	}

	private String getPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getClassLoader().getResource("").getPath());
		sb.append("csv/");
		return sb.toString();
	}

	public void destroy(){
        TaskExecutor.getInstance().shutDown();
		DruidDataSourceFactory.closeAll();
	}
}
