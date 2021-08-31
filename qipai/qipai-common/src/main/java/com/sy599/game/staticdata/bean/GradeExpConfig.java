package com.sy599.game.staticdata.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 芒果跑得快段位配置
 */
public class GradeExpConfig {

    public static int maxGrade = 8;

    private static Map<Integer, GradeExpConfigInfo> gradeExpConfigMap = new ConcurrentHashMap<>();

    public static void setGradeExpConfigMap(Map<Integer, GradeExpConfigInfo> map) {
        gradeExpConfigMap = map;
    }

    public static Map<Integer, GradeExpConfigInfo> getGradeExpConfigMap() {
        return gradeExpConfigMap;
    }

    public static GradeExpConfigInfo getGradeExpConfigInfo(int grade) {
        return gradeExpConfigMap.get(grade);
    }
}
