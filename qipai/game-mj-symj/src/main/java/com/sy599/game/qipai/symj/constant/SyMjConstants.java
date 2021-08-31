package com.sy599.game.qipai.symj.constant;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.symj.rule.SyMj;

public class SyMjConstants {
	// 0胡 1碰 2明杠 3暗杠 4接杠 5杠爆(摸杠胡) 报听6
	/*** 长度*/
	public static final int ACTION_INDEX_LENGTH = 6;
    /*** 胡*/
    public static final int ACTION_INDEX_HU = 0;
    /*** 碰*/
    public static final int ACTION_INDEX_PENG = 1;
    /*** 明杠*/
    public static final int ACTION_INDEX_MINGGANG = 2;
    /*** 暗杠*/
    public static final int ACTION_INDEX_ANGANG = 3;
    /*** 接杠*/
    public static final int ACTION_INDEX_CHI= 4;//改吃
    /*** 自摸  */
    public static final int ACTION_INDEX_ZIMO = 5;
//    /*** 杠爆（摸杠胡）*/
//    public static final int ACTION_INDEX_GANGBAO = 5;
//    /*** 报听*/
//    public static final int ACTION_INDEX_BAOTING = 6;
    
    /**托管**/
    public static final int action_tuoguan = 100;
    /** 自摸次数 */
    public static final int ACTION_COUNT_INDEX_ZIMO = 0;
    /** 接炮次数 */
    public static final int ACTION_COUNT_INDEX_JIEPAO = 1;
    /** 点炮次数 */
    public static final int ACTION_COUNT_INDEX_DIANPAO = 2;
    /** 暗杠次数 */
    public static final int ACTION_COUNT_INDEX_ANGANG = 3;
    /** 明杠次数 */
    public static final int ACTION_COUNT_INDEX_MINGGANG = 4;
    
    /** 门清  */
    public static final int HU_MENQING = 0;
    /** 清一色  */
    public static final int HU_QINGYISE = 1;
    /** 七小对  */
    public static final int HU_QIXIAODUI = 2;
    /** 大对碰  */
    public static final int HU_DADUIPENG = 3;
    /** 龙七对  */
    public static final int HU_LONGQIDUI = 4;
    /** 风一色  */
    public static final int HU_FENGYISE = 5;
    /** 十三幺  */
    public static final int HU_SHISANYAO = 6;
    
    /** 平胡  */
    public static final int HU_PINGHU = 100;
    /** 抢杠胡  */
    public static final int HU_QIANGGANGHU = 101;
    /** 自摸胡  */
    public static final int HU_ZIMO = 102;
    /** 杠开胡  */
    public static final int HU_GANGKAI = 103;
    /** 接炮胡  */
    public static final int HU_JIPAO = 104;
    
    /** 放炮 */
    public static final int HU_FANGPAO = 201;
    /** 杠炮 */
    public static final int HU_GANGPAO = 202;
    /** 点杠 */
    public static final int HU_DIANGANG = 203;
    
    /** 桌状态锤 */
    public static final int TABLE_STATUS_CHUI = 1;
    
	public static boolean isTest = false;
	public static boolean isTestAh = false;

	public static List<Integer> shaoyang_mjList = new ArrayList<>();
	public static List<Integer> shaoyang_mjList_daiFeng = new ArrayList<>();//带风


    /** xx秒后进入托管**/
    public static final int AUTO_TIMEOUT = GameServerConfig.isDeveloper() ? 20 : 180;
    /** xx秒后开始托管倒计时*/
    public static final int AUTO_CHECK_TIMEOUT = 10;
    /** 托管后xx秒自动出牌**/
    public static final int AUTO_PLAY_TIME = 2;
    /** 托管后xx秒自动准备**/
    public static final int AUTO_READY_TIME = 4;
    /** 托管后xx秒自动胡**/
    public static final int AUTO_HU_TIME = 2;


	static {
		if (GameServerConfig.isDeveloper()) {
			isTest = true;
			isTestAh = true;
		}

		// ///////////////////////
		// 筒万条 108张
		for (int i = 1; i <= 108; i++) {
			shaoyang_mjList.add(i);
			shaoyang_mjList_daiFeng.add(i);
		}
		//东南西北
        for (int i = 109; i <= 124; i++){
        	shaoyang_mjList_daiFeng.add(i);
        }
        //红中
		for (int i = 201; i <= 204; i++) {
			shaoyang_mjList_daiFeng.add(i);
		}
		//发白
		for(int i = 205; i <= 212; i++){
			shaoyang_mjList_daiFeng.add(i);
		}
	}

	/**
     * 根据玩法获取牌
     * @param playType
     * @return
     */
    public static List<Integer> getMajiangList(int isDaiFeng){
    	if(isDaiFeng == 1){
    		return new ArrayList<>(shaoyang_mjList_daiFeng);
    	}else{
    		return new ArrayList<>(shaoyang_mjList);
    	}
    }
    public static List<SyMj> getMajiangPai(int isDaiFeng){
    	List<SyMj> majiangs = new ArrayList<>();
    	for(int i=1;i<=27;i++){
			majiangs.add(SyMj.getMajang(i));
		}
    	if(isDaiFeng == 1){
    		majiangs.add(SyMj.getMajang(109));
    		majiangs.add(SyMj.getMajang(113));
    		majiangs.add(SyMj.getMajang(117));
    		majiangs.add(SyMj.getMajang(121));
    		majiangs.add(SyMj.getMajang(201));
    		majiangs.add(SyMj.getMajang(205));
    		majiangs.add(SyMj.getMajang(209));
    	}
    	return majiangs;
    }
}
