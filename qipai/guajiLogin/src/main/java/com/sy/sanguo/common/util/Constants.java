package com.sy.sanguo.common.util;

import java.io.File;
import java.util.HashMap;

public final class Constants {
    /**无房号金币场房间最小id**/
    public static final long MIN_GOLD_ID = 10000000L;

    public static final String GAME_FILE = "config"+File.separator+"game.properties";

    public static final HashMap<String,Integer> userMsgMap=new HashMap<>();

    public static final int SEX_MALE = 1;
    public static final int SEX_FEMALE = 2;

    static {
        userMsgMap.put("realName",1);
        userMsgMap.put("idCard",2);
        userMsgMap.put("phone",3);
    }

    public static Integer loadUserMsgType(String msgKey){
        return userMsgMap.get(msgKey);
    }
}
