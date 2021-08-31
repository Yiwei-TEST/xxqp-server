package com.sy599.game.qipai.ahmj.tool.ting;


import java.io.*;
import java.util.*;

public class TingResouce {

    static Map<Long,Integer>[] allMap=new HashMap[9];


    public static void init(){

        BufferedReader br = null;
        try {
            InputStream is = TingResouce.class.getClassLoader().getResourceAsStream("huCode/huCode.txt");
            br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            int count=0;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("_");
                int i = Integer.parseInt(split[0]);
                Map<Long, Integer> map = allMap[i];
                if (map==null){
                    map=new HashMap<>();
                    allMap[i]=map;
                }
                map.put(Long.parseLong(split[1]),Integer.parseInt(split[2]));
                count++;
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

    /**
     *
     * @param bossNum 带了几个王
     * @param code    胡牌码
     * @return        剩余王
     */
    public static int getRemainBossNum(int bossNum,long code){
        if(allMap[bossNum].containsKey(code)){
            return allMap[bossNum].get(code);
        }
        return -1;
    }




}
