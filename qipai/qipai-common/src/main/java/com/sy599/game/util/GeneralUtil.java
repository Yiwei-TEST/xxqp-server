package com.sy599.game.util;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public final class GeneralUtil {

    /**
     * 获取当前服信息（包括当前工作目录、网卡信息）
     *
     * @return
     */
    public static String loadCurrentServerMsg() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("user.dir:").append(System.getProperty("user.dir"));

        List<String> networkInterfaceList = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                StringBuffer stringBuffer = new StringBuffer();
                NetworkInterface networkInterface = enumeration.nextElement();
                if (networkInterface != null) {
                    byte[] bytes = networkInterface.getHardwareAddress();
                    if (bytes != null) {
                        stringBuffer.append(networkInterface.getName()).append(":");
                        for (int i = 0; i < bytes.length; i++) {
                            if (i != 0) {
                                stringBuffer.append("-");
                            }
                            int tmp = bytes[i] & 0xff; // 字节转换为整数
                            String str = Integer.toHexString(tmp);
                            if (str.length() == 1) {
                                stringBuffer.append("0" + str);
                            } else {
                                stringBuffer.append(str);
                            }
                        }
                        networkInterfaceList.add(stringBuffer.toString());
                    }
                }
            }
        } catch (Exception e) {
        }

        if (networkInterfaceList.size() > 0) {
            Collections.sort(networkInterfaceList);
        }
        stringBuilder.append(",NetworkInterfaces:").append(networkInterfaceList);

        return stringBuilder.toString().toLowerCase();
    }
}
