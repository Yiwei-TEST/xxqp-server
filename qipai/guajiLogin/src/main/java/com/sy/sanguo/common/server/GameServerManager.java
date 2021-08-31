package com.sy.sanguo.common.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.util.CsvReader;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.bean.PayItemMsg;
import com.sy599.sanguo.util.TimeUtil;

public class GameServerManager {
	private Map<Integer, Server> servers = new HashMap<Integer, Server>();
	public static Map<Integer, PayBean> payAountBeans = new HashMap<Integer, PayBean>();
	public static Map<Integer, PayBean> payBeans = new ConcurrentHashMap<>();
	private static GameServerManager _inst = new GameServerManager();

	private GameServerManager() {
		initServer();
		initPayData();
	}

	private void initPayData() {
		String path = this.getClass().getClassLoader().getResource("").getPath();
		path = path + "csv/pay.csv";
		List<String[]> list = readCSV(path, false);
		for (String[] values : list) {
			PayBean bean = new PayBean();
			int i = 0;
			bean.setId(StringUtil.getIntValue(values, i++));
			bean.setDesc(StringUtil.getValue(values, i++));
			bean.setAmount(StringUtil.getIntValue(values, i++));
			bean.setYuanbao(StringUtil.getIntValue(values, i++));
			bean.setSpecialGive(StringUtil.getIntValue(values, i++));
			bean.setPs(StringUtil.getValue(values, i++));
			bean.setName(StringUtil.getValue(values, i++));
			String begintime = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(begintime)) {
				bean.setBeginTime(TimeUtil.parseTimeInDate(begintime));
			}
			String endtime = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(endtime)) {
				bean.setEndTime(TimeUtil.parseTimeInDate(endtime));
			}
			bean.setOrder(StringUtil.getIntValue(values, i++));
			bean.setPayType(StringUtil.getValue(values, i++));
			payBeans.put(bean.getId(), bean);
			payAountBeans.put(bean.getAmount(), bean);
		}
	}

	private void initServer() {
		String path = this.getClass().getClassLoader().getResource("").getPath();
		path = path + "csv/socketInfo.csv";
		List<String[]> list = readCSV(path, false);
		for (String[] values : list) {
			Server ser = new Server();
			ser.setId(parseInt(values[0]));
			ser.setName(values[1]);
			ser.setHost(parseString(values[2]));
			ser.setPort(parseInt(values[3]));
			ser.setWeight(parseInt(values[4]));
			ser.setUrl(parseString(values[5]));
			servers.put(ser.getId(), ser);
		}
	}
	
	
	public PayBean getIosPayBeanByAmount(int amount) {
		for (PayBean bean : payBeans.values()) {
			if (bean.getId() < 10 || bean.getId() > 19) {
				continue;
			}
			if (bean.getAmount() == amount) {
				return bean;
			}
		}
		return null;
	}

	public PayBean getIosPayBean(String pname, int amount) {
		for (PayBean bean : payBeans.values()) {
			if(StringUtils.isBlank(bean.getDesc())){
				continue;
			}
			if (bean.getAmount() == amount && bean.getDesc().contains(pname)) {
				return bean;
			}
		}
		return null;
	}

	public PayBean getIosPayBeanByProductId(String productId) {
		for (PayBean bean : payBeans.values()) {
//			if (bean.getId() < 10 || bean.getId() > 19) {
//				continue;
//			}
			if (StringUtils.isBlank(bean.getDesc())) {
				continue;
			}
			if (bean.getDesc().equals(productId)) {
				return bean;
			}
		}
		return null;
	}

	public PayBean getPayBeanByAmount(int amount) {
		for (PayBean bean : payBeans.values()) {
			if (bean.getId() > 10) {
				continue;
			}
			if (bean.getAmount() == amount) {
				return bean;
			}
		}
		return null;
	}

	public PayBean getPayBean(int id) {
		return this.payBeans.get(id);
	}

	/**
	 * 检查是否有金额相同的品项
	 *
	 * @return
	 */
	public boolean hasSameAmountPayItem(){
		for (PayBean payBean:payBeans.values()){
		    if (payBean.getId()<=10){
                for (PayBean payBean0:payBeans.values()){
                    if (payBean0.getId()<=10&&payBean.getId()!=payBean0.getId()&&payBean.getAmount()==payBean0.getAmount()){
                        return true;
                    }
                }
            }
		}
		return false;
	}

	public static GameServerManager getInstance() {
		return _inst;
	}

	public boolean isCorrectServerId(int serverId) {
		return servers.containsKey(serverId);
	}

	public Server getServer(int serverId) {
		return servers.get(serverId);
	}

	public static String parseString(String value) {
		if (StringUtils.isBlank(value)) {
			return "0";
		} else {
			return value;
		}
	}

	public static int parseInt(String value) {
		if (StringUtils.isBlank(value)) {
			return 0;
		} else {
			return Integer.parseInt(value);
		}
	}

	/**
	 * 读取csv文件
	 * 
	 * @param filePath
	 *            csv目录下的子文件夹目录名/csv的文件名
	 * @param includeHeader
	 *            list是否包含第一行
	 * @return List<String[]> String[]的每个值依次为csv文件每一行从左到右的单元格的值
	 */
	public static List<String[]> readCSV(String filePath, boolean includeHeader) {
		List<String[]> list = new ArrayList<String[]>();
		CsvReader reader = null;
		try {
			reader = new CsvReader(filePath, ',', Charset.forName("UTF-8"));
			/** csv的第一行 * */
			reader.readHeaders();
			String[] headers = reader.getHeaders();
			if (includeHeader) {
				// 读取UTF-8格式有bug 需去掉第一个字符的空格
				headers[0] = headers[0].substring(1);
				list.add(headers);
			}
			/** 从第二行开始读 * */
			while (reader.readRecord()) {
				String[] values = reader.getValues();
				if (values.length != 0 && !StringUtils.isBlank(values[0])) {
					list.add(values);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			/** 关闭reader * */
			if (reader != null) {
				reader.close();
			}
		}
		return list;
	}

	public Map<Integer, Server> getServers() {
		return servers;
	}

	public List<PayItemMsg> getIosPayItemMsg() {
		List<PayItemMsg> list = new ArrayList<>();
		int i = 0;
		for (PayBean bean : payBeans.values()) {
			if (bean.getId() < 10) {
				i++;
				list.add(bean.buildMsg());
			}
			if (i >= 6) {
				break;
			}

		}
		return list;
	}

	public static void main(String[] args) {
		System.out.println(JacksonUtil.writeValueAsString(getInstance().getIosPayBeanByAmount(30)));
	}

}
