package com.sy.sanguo.game.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sy599.common.security.SecuritConstant;
import net.sy599.common.security.SecuritConstantImpl;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.sy.sanguo.game.bean.OrderInfo;
import com.sy.sanguo.game.bean.RegInfo;

public class MixedScript {
	
	@SuppressWarnings("unchecked")
	public void mixed(){
		try{
			List<String> dbnames = new ArrayList<String>();
//			dbnames.add("xsguposambst");
//			dbnames.add("xsguposambdk");
			dbnames.add("xsguposambxm");
//			dbnames.add("xsguposambop");
//			dbnames.add("xsguposamb91");
//			dbnames.add("xsguposambdl");
//			dbnames.add("xsguposambuc");
//			dbnames.add("xsguposamblx");
			
			
			List<String> pfs = new ArrayList<String>();
//			pfs.add("qidian");
//			pfs.add("duoku");
			pfs.add("xiaomi");
//			pfs.add("oppo");
//			pfs.add("m91");
//			pfs.add("dangle");
//			pfs.add("uc");
//			pfs.add("lenovo");
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			for(String dbname:dbnames){
				SqlMapClient client = getClient(dbname);
				
				File file = new File(dbname+".mobile_reg.sql");
				BufferedWriter bw1 = new BufferedWriter(new FileWriter(file.getPath(), true));
				int start = 0;
				int limit = 1000;
				int count = 0;
				int mirrorsCount = limit;
				
				while(mirrorsCount == limit){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("start", start);
					map.put("end", limit);
					List<RegInfo> users = client.queryForList("mix.getAllUser", map);
					
					String sql = "insert into mobile_reg values ('%s','%s','%s','%s','%s','%s','%s') on duplicate key update sessCode='%s';\r\n";
					for(RegInfo user:users){
						String playerSid = user.getPlayedSid() == null ? "" : user.getPlayedSid();
						String realsql = String.format(
								sql,
								user.getFlatId(),
								pfs.get(dbnames.indexOf(dbname)),
								user.getPw(),
								playerSid,
								user.getSessCode(),
								sdf.format(user.getRegTime()),
								sdf.format(user.getLogTime()),
								user.getSessCode()
						);
						bw1.write(realsql);
						bw1.flush();
					}
					mirrorsCount = users.size();
					count+= mirrorsCount;
					start+=limit;
				}
	            bw1.close();
				System.out.println(dbname+" mobile_reg all user::"+count);
				
				
				
				File fileo = new File(dbname+".order_info.sql");
				BufferedWriter bw2 = new BufferedWriter(new FileWriter(fileo.getPath(), true));
				start = 0;
				count = 0;
				mirrorsCount = limit;
				while(mirrorsCount == limit){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("start", start);
					map.put("end", limit);
					List<OrderInfo> orders = client.queryForList("mix.getAllOrder", map);
					
					String osql = "insert into order_info (flat_id,order_id,server_id,order_amount,is_sent,platform,create_time) values ('%s','%s','%s','%s','%s','%s','%s');\r\n";
					for(OrderInfo order:orders){
						String realosql = String.format(
								osql,
								order.getFlat_id(),
								order.getOrder_id(),
								order.getServer_id(),
								order.getOrder_amount(),
								1,
								order.getPlatform(),
								sdf.format(order.getCreate_time())
						);
						bw2.write(realosql);
						bw2.flush();
					}
					mirrorsCount = orders.size();
					count+= mirrorsCount;
					start+=limit;
				}
				bw2.close();
				System.out.println(dbname+" order_info all order::"+count);
				client.getDataSource().getConnection().close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public SqlMapClient getClient(String dbname){
		String strpwd = "jdbc.password1";
		String struser = "jdbc.user1";
		String strurl = "jdbc.url1";
		SqlMapClient client = null;
		try { 
			Properties jdbcProperties = new Properties();
			String filepath = "D:/meworkspace/sanguoLogin/WebRoot/WEB-INF/";
			jdbcProperties.load(new FileInputStream(filepath+"config/jdbc.properties"));
			Reader reader = Resources.getResourceAsReader("config/SqlMapConfigMix.xml");
			String md5Password = jdbcProperties.getProperty(strpwd);
			SecuritConstant des = new SecuritConstantImpl();
		    Properties jdbc = new Properties();
		    jdbc.setProperty("jdbc.password", des.decrypt(md5Password));
		    String url = jdbcProperties.getProperty(strurl);
		    url = url.replace("{0}", dbname);
		    jdbc.setProperty("jdbc.url", url);    
		    String user = jdbcProperties.getProperty(struser);
		    jdbc.setProperty("jdbc.user", user);
		    String driver = jdbcProperties.getProperty("jdbc.ClassDriver");
		    jdbc.setProperty("jdbc.ClassDriver", driver);
		    client = SqlMapClientBuilder.buildSqlMapClient(reader,jdbc);  
		    reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return client;
	}
	
	public static void main(String[] args){
		try {
			SecuritConstant des = new SecuritConstantImpl();
			System.out.println(des.encrypt("root"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
