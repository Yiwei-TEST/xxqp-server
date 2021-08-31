package com.sy599.game.qipai.zzmj.tool.hulib.core.gen_table;

import com.sy599.game.qipai.zzmj.tool.hulib.util.HuUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class SetTable {
    private HashMap<Integer, Boolean> m_tbl = new HashMap<>();

    public boolean check(int number) {
        return m_tbl.containsKey(number);
    }


    public void add(int key) {
        if (m_tbl.containsKey(key))
            return;

        m_tbl.put(key, true);
    }

    public void dump(String name) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(HuUtil.dump_file_dir + name));
            Iterator<Entry<Integer, Boolean>> iter = m_tbl.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, Boolean> entry = iter.next();
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
            br = new BufferedReader(new FileReader(HuUtil.source_dir + path));
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
}