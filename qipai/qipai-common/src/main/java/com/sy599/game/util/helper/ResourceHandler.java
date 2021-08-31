package com.sy599.game.util.helper;

import com.sy599.game.staticdata.CsvReader;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 资源动态加载
 *
 * @author liuping
 */
public abstract class ResourceHandler {

    /**
     * 需实现资源重新加载接口
     *
     * @param resourceFileName
     */
    public void reload(String resourceFileName) {
    }

    /**
     * 加载Properties文件
     *
     * @param resourcePath
     * @return
     */
    public Properties loadFromFile(String resourcePath) {
        Properties payProperties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(resourcePath);
            payProperties.load(fis);
            fis.close();
        } catch (Exception e) {
        }
        return payProperties;
    }

    private static boolean isRead(String resourcePath) {
        File file = new File(resourcePath);
        if (!file.exists()) {
            LogUtil.msgLog.error("--------" + resourcePath + "is not exists----------");
            return false;
        }
        return true;
    }

    /**
     * 读取csv文件
     *
     * @param resourcePath      csv目录下的子文件夹目录名/csv的文件名
     * @param includeHeader list是否包含第一行
     * @return List<String[]> String[]的每个值依次为csv文件每一行从左到右的单元格的值
     */
    public static List<String[]> readCSVResource(String resourcePath, boolean includeHeader) {
        List<String[]> list = new ArrayList<String[]>();
        if (!isRead(resourcePath)) {
            return list;
        }
        CsvReader reader = null;
        try {
            reader = new CsvReader(resourcePath, ',', Charset.forName("UTF-8"));
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

    /**
     * getValue读取csv
     *
     * @param values
     * @param index
     * @return
     */
    protected static String getStrValue(String[] values, int index) {
        return StringUtil.getValue(values, index);
    }
}
