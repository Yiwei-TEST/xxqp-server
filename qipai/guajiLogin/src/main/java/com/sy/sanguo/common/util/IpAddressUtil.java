package com.sy.sanguo.common.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.maxmind.geoip2.record.Subdivision;
import com.sy.sanguo.common.util.request.HttpUtil;
import com.sy.sanguo.common.util.request.MD5Util;
import org.apache.log4j.Logger;

import com.maxmind.db.Reader.FileMode;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;

/**
 * 从本地ip库获取ip地址值
 * <p/>
 * <font color="red">本地ip库会定时更新</font>
 * 
 * @author Administrator
 * 
 */
public final class IpAddressUtil {
	private static Logger logger = Logger.getLogger(IpAddressUtil.class);

	public static final Pattern IP_PATTERN = Pattern.compile(
			"\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");

	private static final String DATABASE_READER_FILEPATH = System.getProperties().getProperty("os.name").toLowerCase()
			.contains("windows") ? "D:/mydownloads" : IpAddressUtil.class.getClassLoader().getResource("").getPath() + "ip_file";
	private static final String DATABASE_READER_FILENAME = "GeoLite2-City.mmdb";
	private static final String DATABASE_READER_FILE = DATABASE_READER_FILEPATH + File.separator
			+ DATABASE_READER_FILENAME;
	// /data/efun/tools/ip_file
	// D:/mydownloads
	private static DatabaseReader reader = null;
	private static boolean databaseReaderExists = true;// 判断ip数据库文件是否存在且有效

	private static Timer timer = new Timer();// 定时重新加载ip数据库文件
	private static Timer timerDownFile = new Timer();// 定时重新下载ip数据库文件
	private static long reLoadCount = 0;// 加载ip数据库文件的次数
	private static AtomicBoolean databaseReaderLoading = new AtomicBoolean(false);// 控制ip数据库文件是否正在(重新)加载

	private static final String IP_DATABASE_FILE_EXT = ".gz";
	private static final String FILE_URL = "http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb"
			+ IP_DATABASE_FILE_EXT;
	private static final String FILE_MD5_URL = "http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.md5";

	private static final long RELOAD_IPADDRESS_CONFIGFILE_CYCLE = 24 * 60 * 60 * 1000;// 1day
	private static final long RELOAD_DOWNLOADFILE_CYCLE = 15 * 24 * 60 * 60 * 1000;// 15days

	static {
		System.out.println("ip file path:" + DATABASE_READER_FILEPATH);
		logger.info("ip file path:" + DATABASE_READER_FILEPATH);
		loadIpAddressConfigFile();
		initReloadFileTimer();
		initDownloadFileTimer();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// long startTime=System.currentTimeMillis();
		// for(long i=0;i<1;i++) {
		// StringBuilder strBuilder = new StringBuilder();
		// strBuilder.append(new Random().nextInt(255)).append(".");
		// strBuilder.append(new Random().nextInt(255)).append(".");
		// strBuilder.append(new Random().nextInt(255)).append(".");
		// strBuilder.append(new Random().nextInt(255));
		System.out.println(" getIpAddress " + "218.77.106.136" + ":" + getIpAddress("218.77.106.136", true));
		System.out.println(" getIpAddress " + "65.199.22.141" + ":" + getIpAddress("65.199.22.141", true));
		System.out.println(" getIpAddress " + "17.255.252.153" + ":" + getIpAddress("17.255.252.153", true));
		System.out.println(" getIpAddress " + "222.244.113.18" + ":" + getIpAddress("222.244.113.18"));
		// }
		// System.out.println((System.currentTimeMillis()-startTime)+"ms");
	}

	private static void initReloadFileTimer() {
		// 定时任务
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 6);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				loadIpAddressConfigFile();
			}
		}, cal.getTime(), RELOAD_IPADDRESS_CONFIGFILE_CYCLE);
	}

	private static void initDownloadFileTimer() {
		// 定时任务
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.DAY_OF_MONTH) >= 15) {
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
		} else {
			cal.set(Calendar.DAY_OF_MONTH, 15);
		}
		cal.set(Calendar.HOUR_OF_DAY, 5);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		timerDownFile.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				downloadIpDatabseFile();
			}
		}, cal.getTime(), RELOAD_DOWNLOADFILE_CYCLE);// RELOAD_DOWNLOADFILE_CYCLE
	}

	private static void downloadIpDatabseFile() {
		boolean hasNext = true;
		int currentCount = 0;
		while (hasNext && currentCount < 3) {
			hasNext = false;
			currentCount++;
			boolean bl = true;
			int count = 0;
			String newDatabaseFileName = UUID.randomUUID().toString().replaceAll("-", "") + ".mmdb";
			String newFileName = newDatabaseFileName + IP_DATABASE_FILE_EXT;
			File newDatabaseFile = new File(DATABASE_READER_FILEPATH, newDatabaseFileName);
			while (bl && count < 3) {
				count++;
				try {
					DownloadFileUtil.downloadNet(DATABASE_READER_FILEPATH, newFileName, FILE_URL);
					if (newDatabaseFile.exists()) {
						newDatabaseFile.delete();
					}
					GZUtil.doUncompressFile(DATABASE_READER_FILEPATH + "/" + newFileName);
					new File(DATABASE_READER_FILEPATH, newFileName).delete();
					bl = false;
					logger.info(count + " " + FILE_URL + " DownloadFile Success");
					System.out.println(count + " " + FILE_URL + " DownloadFile Success");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					bl = true;
					logger.info(count + " " + FILE_URL + " DownloadFile Exception " + e.getMessage());
					System.out.println(count + " " + FILE_URL + " DownloadFile Exception " + e.getMessage());
				}
			}
			if (!bl) {
				bl = true;
				count = 0;
				while (bl && count < 3) {
					count++;
					String md5Str = HttpUtil.getUrlReturnValue(FILE_MD5_URL);
					if (md5Str == null) {
						bl = true;
						logger.info(count + " " + FILE_MD5_URL + " DownloadFileMD5 Fail");
						System.out.println(count + " " + FILE_MD5_URL + " DownloadFileMD5 Fail");
					} else {
						bl = false;
						if (md5Str.equalsIgnoreCase(MD5Util.getFileMD5String(newDatabaseFile))) {
							logger.info(currentCount + " 文件校验成功");
							System.out.println(currentCount + " 文件校验成功");

							logger.info(
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 重新下载了ip数据库文件");
							System.out.println(
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 重新下载了ip数据库文件");
							loadIpAddressConfigFile();
						} else {
							hasNext = true;
							newDatabaseFile.delete();
							logger.info(currentCount + " 文件校验失败");
							System.out.println(currentCount + " 文件校验失败");
						}
					}
				}
			} else {
				logger.info(FILE_URL + " DownloadFile Fail");
				System.out.println(FILE_URL + " DownloadFile Fail");
			}
		}
	}

	public static void destory() {
		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
			}
			reader = null;
		}
		try {
			timer.cancel();
			timerDownFile.cancel();
		} catch (Exception e) {
		}
		timer = null;
		timerDownFile = null;
	}

	private synchronized static void loadIpAddressConfigFile() {
		databaseReaderLoading.set(true);
		reLoadCount++;
		System.out.println("DATABASE_READER_FILE load count>>" + reLoadCount);
		logger.info("DATABASE_READER_FILE load count>>" + reLoadCount);

		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reader = null;
		}

		File database = null;
		File parentFile = new File(DATABASE_READER_FILEPATH);
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		} else if (!parentFile.isDirectory()) {
			parentFile.delete();
			parentFile.mkdirs();
		}
		File[] listFiles = parentFile.listFiles();
		long lastTime = 0L;
		String dataFileName = null;
		if (listFiles != null) {
			for (File file : listFiles) {
				String fileName = file.getName();
				if (fileName.endsWith(".mmdb")) {
					if (lastTime < file.lastModified()) {
						lastTime = file.lastModified();
						dataFileName = fileName;
					}
				}
			}
		}
		if (dataFileName == null) {
			dataFileName = DATABASE_READER_FILENAME;
		}
		database = new File(DATABASE_READER_FILEPATH, dataFileName);
		if (listFiles != null) {
			for (File file : listFiles) {
				if (!dataFileName.equals(file.getName())) {
					file.delete();
				}
			}
		}
		if (database.exists()) {
			try {
				reader = new DatabaseReader.Builder(database).fileMode(FileMode.MEMORY).build();
				System.out.println("DATABASE_READER_FILE load success>>" + database.getAbsolutePath());
				logger.info("DATABASE_READER_FILE load success>>" + database.getAbsolutePath());
				databaseReaderExists = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				databaseReaderExists = false;
				System.out.println("DATABASE_READER_FILE load error>>" + database.getAbsolutePath());
				logger.info("DATABASE_READER_FILE load error>>" + database.getAbsolutePath());
			}
		} else {
			databaseReaderExists = false;
			System.out.println("DATABASE_READER_FILE not exists>>" + DATABASE_READER_FILE);
			logger.info("DATABASE_READER_FILE not exists>>" + DATABASE_READER_FILE);
			if (reLoadCount <= 3) {
				downloadIpDatabseFile();
			}
		}
		databaseReaderLoading.set(false);
	}

	/**
	 * 从ip库中获取ip信息
	 * 
	 * @param ip
	 *            ip地址
	 * @param bl
	 *            是否需要处理
	 * @return
	 */
	public static String getIpAddress(String ip, boolean bl) {
		if (!isBoolIp(ip)) {
			return ip;
		}
		while (databaseReaderLoading.get()) {
		}

		if (!databaseReaderExists) {
			System.out.println("DATABASE_READER_FILE load fail>>" + DATABASE_READER_FILE);
			logger.info("DATABASE_READER_FILE load fail>>" + DATABASE_READER_FILE);
			return ip;
		}

		String returnStr = null;

		try {
			InetAddress ipAddress = InetAddress.getByName(ip);
			CityResponse response = reader.city(ipAddress);
			Country country = response.getCountry();
			String countryCN = country.getNames().get("zh-CN");
			if (countryCN != null) {
				returnStr = bl ? countryCN.replaceAll("大韩民国", "韩国") : countryCN;
			}
			City city = response.getCity();
			String cityCN = city.getNames().get("zh-CN");
			if (cityCN != null) {
				if (bl) {
//					String countryCity=PropUtil.getString("countryCity");
//					if("true".equals(countryCity)){
//						returnStr += cityCN;
//					}
				} else {
					returnStr += cityCN;
				}
			}
		} catch (Exception e) {
			logger.info("DATABASE_READER_FILE select ip fail>>" + ip);
		}
		if (returnStr == null) {
			returnStr = "未知地址";
		}
		return returnStr;
	}

	/**
	 * 判断是否为合法ip地址
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isBoolIp(String ip) {
		if (ip == null) {
			return false;
		}
		return IP_PATTERN.matcher(ip).matches();
	}

	/**
	 * 判断是否数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		if (str != null && str.matches("\\d*")) {
			return true;
		} else {
			return false;
		}
	}


    /**
     * 从ip库中获取ip信息
     *
     * @param ip ip地址
     * @return
     */
    public static Map<String, String> getIpAddress(String ip) {
        if (!isBoolIp(ip)) {
            return null;
        }
        while (databaseReaderLoading.get()) {
        }
        if (!databaseReaderExists) {
            System.out.println("DATABASE_READER_FILE load fail>>" + DATABASE_READER_FILE);
            logger.info("DATABASE_READER_FILE load fail>>" + DATABASE_READER_FILE);
            return null;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            Map<String, String> map = new HashMap<>();
            String countryCN = response.getCountry().getNames().get("zh-CN");
            if (countryCN != null) {
                countryCN = countryCN.replaceAll("大韩民国", "韩国");
                map.put("country", countryCN);
                List<Subdivision> subdivisions = response.getSubdivisions();
                if (subdivisions != null && subdivisions.size() > 0) {
                    String provinceCN = subdivisions.get(0).getNames().get("zh-CN");
                    if (provinceCN != null) {
                        map.put("province", provinceCN);
                    }
                }
                String cityCN = response.getCity().getNames().get("zh-CN");
                if (cityCN != null) {
                    map.put("city", cityCN);
                }
            }
            return map;
        } catch (Exception e) {
            logger.info("DATABASE_READER_FILE select ip fail>>" + ip);
        }
        return null;
    }

}
