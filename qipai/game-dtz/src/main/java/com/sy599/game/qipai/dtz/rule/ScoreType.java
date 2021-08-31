package com.sy599.game.qipai.dtz.rule;

/**
 * 分数的枚举
 * @author lc
 *
 */
public enum ScoreType {
	
	/** 5*/
	POINT_5(1),
	/** 10*/
	POITN_10(2),
	/** k*/
	POINT_K(3),
	/** 地炸*/
	POINT_BD(4),
	
	/** 筒子*/
	POINT_TZ(5),
	/** 筒子A*/
	POINT_TZ_A(51),
	/** 筒子2*/
	POINT_TZ_2(52),
	/** 筒子k*/
	POINT_TZ_K(53),
	/** 筒子小王*/
	POINT_TZ_S(54),
	/** 筒子大王*/
	POINT_TZ_B(55),
	
	/** 喜*/
	POINT_XI(6),
	/** 小王的息*/
	POINT_JACKER_S(7),
	/** 大王的息*/
	POINT_JACKER_B(8),
    /** 5-Q喜*/
    POINT_Xi_5Q(9),
    /** K喜*/
    POINT_Xi_K(10),
    /** A喜*/
    POINT_Xi_A(11),
    /** 2喜*/
    POINT_Xi_2(12),

    ;
	
	private int type;

	private ScoreType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
}
