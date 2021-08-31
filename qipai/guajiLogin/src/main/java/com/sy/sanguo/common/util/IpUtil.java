package com.sy.sanguo.common.util;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 获取ip地址
 *
 * @author Administrator
 */
public class IpUtil {
    private static final Logger log = Logger.getLogger(IpUtil.class);

    private static final Pattern PATTERN_IP_ADDR	= Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /** 检查字符串是否符合 IP 地址格式 */
    public final static boolean isStrIPAddress(String str)
    {
        return PATTERN_IP_ADDR.matcher(str).matches();
    }

    /**
     * @param request
     * @return IP Address
     */
    public static final String getIpAddrByRequest(HttpServletRequest request) {
        return getIpAddr(request);
    }

    /**
     * @param request
     * @return IP Address
     */
    public static final String getIpAddr(HttpServletRequest request) {
//        String ip = request.getHeader("X-Real-IP");
        String ip = request.getHeader("X-Forwarded-For");
        String fix = getIpAddr(ip);
        if (fix != null) {
            if (fix.length() != 0) {
                ip = fix;
            } else {
//                String ip1 = ip;
//                ip = request.getHeader("X-Forwarded-For");
//                String ip1;
//                int idx = ip!= null ? ip.indexOf(",") : -1;
//                if (idx > -1) {
//                    ip1 = ip.substring(0, idx);
//                } else {
//                    ip1 = null;
//                }
                ip = request.getHeader("X-Real-IP");
                fix = getIpAddr(ip);
                if (fix != null) {
                    if (fix.length() != 0) {
                        ip = fix;
                    } else {
//                        if (ip1 == null && ip != null) {
//                            int idx = ip.indexOf(",");
//                            if (idx > -1) {
//                                ip1 = ip.substring(0, idx);
//                            } else {
//                                ip1 = ip;
//                            }
//                        }
                        String ip1 = ip;
                        ip = request.getHeader("Proxy-Client-IP");
                        fix = getIpAddr(ip);
                        if (fix != null) {
                            if (fix.length() != 0) {
                                ip = fix;
                            } else {
                                if (ip1 == null) {
                                    ip1 = ip;
                                }
                                ip = request.getHeader("WL-Proxy-Client-IP");
                                fix = getIpAddr(ip);
                                if (fix != null) {
                                    if (fix.length() != 0) {
                                        ip = fix;
                                    } else {
                                        if (ip1 == null) {
                                            ip1 = ip;
                                        }

                                        if (ip1 == null) {
                                            ip = request.getRemoteAddr();

                                            if(ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {
                                                try {
                                                    ip = getRealIp();
                                                } catch (Exception e) {
                                                } finally {
                                                    if (ip == null || ip.length() == 0 || !isStrIPAddress(ip)) {
                                                        log.error(new StringBuilder("=====getIpAddr:error===== ip>>>").append(ip).toString());
                                                    }
                                                }
                                            }

                                        } else {
                                            ip = ip1;
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        return ip;
    }

    /**
     * 是否是内网ip<br/>
     * 10.*.*.* 172.*.*.* 192.*.*.*
     *
     * @param ip
     * @return
     */
    private static final boolean isIntranet0(String ip) {
        return ip != null && (ip.startsWith("172.") || ip.startsWith("192.") || ip.startsWith("10."));
    }

    /**
     * 是否是内网ip<br/>
     * 10.*.*.* 172.*.*.* 192.*.*.*
     *
     * @param ip
     * @return
     */
    public static final boolean isIntranet(String ip) {
        return isIntranet0(ip) && isStrIPAddress(ip);
    }

    /**
     * @param ip
     * @return
     * @see #isIntranet(String)
     */
    public static final boolean isNotIntranet(String ip) {
        return !isIntranet(ip);
    }

    /**
     * 是否是内网ip<br/>
     * 10.*.*.* 172.*.*.* 192.*.*.*
     *
     * @param request
     * @return
     * @see #getIpAddr(HttpServletRequest)
     * @see #isIntranet(String)
     */
    public static final boolean isIntranet(HttpServletRequest request) {
        return isIntranet(getIpAddr(request));
    }

    /**
     * @param request
     * @return
     * @see #isIntranet(HttpServletRequest)
     */
    public static final boolean isNotIntranet(HttpServletRequest request) {
        return !isIntranet(request);
    }


    /**
     * 修正ip<br/>
     * null:无需更改，""：需要更改，其他值表示修正后的ip
     *
     * @param ip
     * @return
     */
    private static final String getIpAddr(String ip) {
        // "unknown".equalsIgnoreCase(ip)替换为(ip.length()==7&&ip.indexOf(".")==-1)
        if (ip == null) {
            return "";
        } else {
            int length = ip.length();
            if (length < 7 || ip.indexOf(".") == -1) {
                return "";
            }
            if (ip.indexOf(",") == -1 && length <= 15) {
                if (isIntranet0(ip)) {
                    return "";
                } else if (isStrIPAddress(ip)) {
                    return null;
                } else {
                    log.error(new StringBuilder("=====getIpAddr:error===== ip>>>").append(ip).toString());
                    return "";
                }
            } else {
                String[] ips = ip.split("\\,| ");
                for (String temp : ips) {
                    temp = temp.trim();
                    length = temp.length();
                    if (length > 0) {
                        if (isStrIPAddress(temp)) {
                            if (!isIntranet0(ip)) {
                                return temp;
                            }
                        } else {
                            log.error(new StringBuilder("=====getIpAddr:error===== ip>>>").append(temp).toString());
                        }
                    }
                }
                return "";
            }
        }
    }

    /**
     * @return 本机有外网IP则返回外网IP，否则返回本地IP
     * @throws SocketException
     */
    private final static String getRealIp() throws SocketException {
        String localip = null;// 本地IP，如果没有配置外网IP则返回它
        String netip = null;// 外网IP

        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        boolean finded = false;// 是否找到外网IP
        while (netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
                    localip = ip.getHostAddress();
                }
            }
        }

        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }
}