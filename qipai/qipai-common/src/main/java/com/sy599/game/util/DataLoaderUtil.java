package com.sy599.game.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DataLoaderUtil {
    private static List<String> ROBOT_NAMES = new ArrayList<>();
    private static Random ROBOT_RANDOM = new SecureRandom();

    /**
     * 初始化机器人名字
     */
    public static void initRobotNames(String path){
        File file = new File(path);

        if (!file.exists() || !file.isFile()){
            return;
        }

        List<String> robotNames = new ArrayList<>();
        try{
            List dataList = FileUtils.readLines(file, "UTF-8");
            if (dataList!=null){
                for (Object str:dataList){
                    String temp=String.valueOf(str);
                    if (temp.length()>0){
                        String[] names=temp.split(",");
                        for (String name:names){
                            if (name.length()>0){
                                robotNames.add(name);
                            }
                        }
                    }
                }
                ROBOT_NAMES=robotNames;
            }
        }catch (Exception e){
            LogUtil.e("initRobotNames Exception:"+e.getMessage(),e);
        }
    }

    public static String loadRandomRobotName(){
        return ROBOT_NAMES.size()>0?ROBOT_NAMES.get(ROBOT_RANDOM.nextInt(ROBOT_NAMES.size())):"机器人";
    }

}
