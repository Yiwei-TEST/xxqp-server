package com.sy599.game.util.mj.core.handle;


import com.sy599.game.util.mj.util.MjHuUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SetTable {
    private HashMap<Integer, Boolean> m_tbl = new HashMap<>();
    private Map<Integer, List<Integer>> tingMap = new HashMap<>();

    public boolean check(Integer number) {
        return m_tbl.containsKey(number);
    }

    public void add(int key) {
        if (m_tbl.containsKey(key))
            return;

        m_tbl.put(key, true);
    }

    public void dump(String name) {
    }

    public List<Integer> check_ting(Integer number) {
        List<Integer> tingList = tingMap.get(number);

        if (tingList == null || tingList.isEmpty()) {
            return null;
        }

        return tingList;
    }

    public void load_ting(String path) {
        BufferedReader br = null;
        try {
            InputStream is = SetTable.class.getClassLoader().getResourceAsStream(MjHuUtil.source_dir + path);
            br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            List<Integer> tingSet = null;
            while ((line = br.readLine()) != null) {
                String[] str = line.replace("[", "").replace("]", "").split("#");
                String key = str[0];
                String[] valList = str[1].split(",");
                tingSet = new ArrayList<>();
                for (int i = 0; i < valList.length; i++) {
                    tingSet.add(Integer.parseInt(valList[i].trim()));
                }

                tingMap.put(Integer.parseInt(key), tingSet);
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(String path) {
        BufferedReader br = null;
        try {
            InputStream is = SetTable.class.getClassLoader().getResourceAsStream(MjHuUtil.source_dir + path);
            br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = br.readLine()) != null) {
                m_tbl.put(Integer.parseInt(line), true);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}