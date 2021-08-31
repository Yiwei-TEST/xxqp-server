package com.sy.sanguo.game.competition.util;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @notes Getting the IP address of the current machine using Java
 * 
 * @author bo
 * -
 * @version 2019年6月26日 上午10:34:02
 */
public class NetworkUtils {
	private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
	/**
	 * 获取当前机器端口号
	 */
	public static String getLocalPort() throws Exception {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName("*:type=Connector,*"), null);
		if (objectNames == null || objectNames.size() <= 0) {
			throw new IllegalStateException("Cannot get the names of MBeans controlled by the MBean server.");
		}
		for (ObjectName objectName : objectNames) {
			String protocol = String.valueOf(mBeanServer.getAttribute(objectName, "protocol"));
			String port = String.valueOf(mBeanServer.getAttribute(objectName, "port"));
			// windows下属性名称为HTTP/1.1, linux下为org.apache.coyote.http11.Http11NioProtocol
			if (protocol.equals("HTTP/1.1") || protocol.equals("org.apache.coyote.http11.Http11NioProtocol")) {
				return port;
			}
		}
		throw new IllegalStateException("Failed to get the HTTP port of the current server");
	}

	public static String getInet4Address() throws Exception{
		Enumeration<NetworkInterface> nis;
		String ip = null;
		try {
			nis = NetworkInterface.getNetworkInterfaces();
			for (; nis.hasMoreElements();) {
				NetworkInterface ni = nis.nextElement();
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				for (; ias.hasMoreElements();) {
					InetAddress ia = ias.nextElement();
					//ia instanceof Inet6Address && !ia.equals("")
					if (ia instanceof Inet4Address && !ia.getHostAddress().equals("127.0.0.1")) {
						ip = ia.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			logger.error("getServerIpAddress执行出错：" + e.getMessage() + "," + e.getCause());
			throw e;
		}
		return ip;
	}

	public static InetAddress getCurrentIp() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
				Enumeration<InetAddress> nias = ni.getInetAddresses();
				while (nias.hasMoreElements()) {
					InetAddress ia = (InetAddress) nias.nextElement();
					if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
						return ia;
					}
				}
			}
		} catch (SocketException e) {
			logger.error("{}",e);
		}
		return null;
	}

	/**
	 * 获取当前机器的IP
	 */
	public static String getLocalIP() throws Exception {
		InetAddress addr = InetAddress.getLocalHost();
		byte[] ipAddr = addr.getAddress();
		String ipAddrStr = "";
		for (int i = 0; i < ipAddr.length; i++) {
			if (i > 0) {
				ipAddrStr += ".";
			}
			ipAddrStr += ipAddr[i] & 0xFF;
		}
		return ipAddrStr;
	}



	public static int getHttpPort() {
		try {
			MBeanServer server;
			if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
				server = MBeanServerFactory.findMBeanServer(null).get(0);
			}
			else {
				return -1;
			}

			Set names = server.queryNames(new ObjectName("Catalina:type=Connector,*"),
					Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));

			Iterator iterator = names.iterator();
			if (iterator.hasNext()) {
				ObjectName name = (ObjectName) iterator.next();
				return Integer.parseInt(server.getAttribute(name, "port").toString());
			}
		}
		catch (Exception e) {
			logger.error("NetWorkUtil getHttpPort Error:{}",e);
		}
		return -1;
	}

//	private static String HTTP_IP_AND_PORT;
//	public static String getHttpIpAndPort() {
//		if(HTTP_IP_AND_PORT == null){
//			try {
//				HTTP_IP_AND_PORT = "http://" + NetworkUtils.getLocalIP() + ":" + NetworkUtils.getHttpPort();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		return HTTP_IP_AND_PORT;
//	}

	public static String getServletBaseUrl(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		StringBuffer requestURL = request.getRequestURL();
		return requestURL.toString().replace(servletPath, "");
	}

	public static void main(String[] args) throws Exception {
		System.out.println();
	}


 
}