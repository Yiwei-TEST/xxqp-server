package com.sy599.game.db.bean.competition;

import java.util.Date;

import lombok.Data;

@Data
public class CompetitionRoomConfig {

    public static final int STATE_VALID = 1;
    public static final int STATE_UNVALID = 2;

    public static final int TYPE_GOLD = 1;

    private Long keyId;
    private Integer soloType;
    private Integer state;
    private Integer playType;
    private String name;
    private Integer playerCount;
    private Integer totalBureau;
    private String tableMsg;
    private String ratioMsg;
    private Integer order;
    private Date createdTime;
    private Date lastUpTime;


	// -----以下与数据库无关
	/*** 金币场：门票 ***/
	private long ticket = 0;
	/*** 金币场：加入分数限制 ***/
	private long joinLimit = 0;
	/*** 金币场：倍率 ***/
	private long rate = 1;

    public boolean isValid(){
        return state == STATE_VALID;
    }
}
