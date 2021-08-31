package com.sy599.game.qipai.zzmj.tool.hulib.core.gen_ting_table;

import com.sy599.game.qipai.zzmj.tool.hulib.util.HuUtil;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;


public class SetTable {


    private Map<Integer, Boolean> m_tbl = new HashMap<Integer, Boolean>();

    private Map<Integer, Set<Integer>> tingMap = new HashMap<Integer, Set<Integer>>();

    public boolean check(int number) {
        return m_tbl.containsKey(number);
    }

    // 检测听是否有number的key
    public boolean check_ting(int number) {
        return tingMap.containsKey(number);
    }

    // 添加听数据到缓存
    public void add_ting(Integer tingKey, int index) {
        Set<Integer> tingSet = tingMap.get(tingKey);

        if (tingSet == null) {
            tingSet = new HashSet<>();
            tingMap.put(tingKey, tingSet);
        }

        tingSet.add(index);
    }

    public void add(int key) {
        if (m_tbl.containsKey(key))
            return;

        m_tbl.put(key, true);
    }

    // 写听文件
    public void dump_ting(String name) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(HuUtil.dump_file_dir + name));
            Iterator<Entry<Integer, Set<Integer>>> iter = tingMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, Set<Integer>> entry = (Entry<Integer, Set<Integer>>) iter.next();
                bw.write(entry.getKey() + "#" + entry.getValue());
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dump(String name) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(HuUtil.dump_file_dir + name));
            Iterator<Entry<Integer, Boolean>> iter = m_tbl.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, Boolean> entry = (Entry<Integer, Boolean>) iter.next();
                bw.write(entry.getKey() + "");
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(String path) {
        BufferedReader br = null;
        try {
            InputStream is = SetTable.class.getClassLoader().getResourceAsStream(HuUtil.source_dir + path);
            br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            // int num=0;
            while ((line = br.readLine()) != null) {
                // System.out.println("line:" + line);
                m_tbl.put(Integer.parseInt(line), true);
                // num++;
            }
            br.close();
            // System.out.printf("load %s: num=%d\n", path, num);
            // if(m_tbl.containsKey(200000000)){
            // System.out.println("contains 200000000");
            // }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Map<Integer, Boolean> getM_tbl() {
        return m_tbl;
    }
}