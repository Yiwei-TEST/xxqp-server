package com.sy599.game.qipai.hbgzp.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;
import com.sy599.game.util.ResourcesConfigsUtil;

public class HbgzpConstants {
	// 0胡 1碰 2明杠 3暗杠 4接杠 5杠爆(摸杠胡) 报听6
	/*** 长度*/
	public static final int ACTION_INDEX_LENGTH = 7;
    /*** 胡*/
    public static final int ACTION_INDEX_HU = 0;
    /*** 碰*/
    public static final int ACTION_INDEX_PENG = 1;
    /*** 招*/
    public static final int ACTION_INDEX_MINGGANG = 2;
    /*** 扎*/
    public static final int ACTION_INDEX_ANGANG = 3;
    /*** 捡*/
    public static final int ACTION_INDEX_CHI= 4;//改吃
    /*** 滑*/
    public static final int ACTION_INDEX_HUA= 6;
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
    /** 滑次数 */
    public static final int ACTION_COUNT_INDEX_HUA = 5;
    
    
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
		for (int i = 1; i <= 110; i++) {
			shaoyang_mjList.add(i);
		}
	}

	/**
     * 根据玩法获取牌
     * @param playType
     * @return
     */
    public static List<Integer> getMajiangList(){
		return new ArrayList<>(shaoyang_mjList);
    }
    public static List<Hbgzp> getMajiangPai(){
    	List<Hbgzp> majiangs = new ArrayList<>();
    	for(int i=1;i<=110;i++){
			majiangs.add(Hbgzp.getPaohzCard(i));
		}
    	return majiangs;
    }
    
    public static String actionListToString(List<Integer> actionList) {
        if (actionList == null || actionList.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < actionList.size(); i++) {
            if (actionList.get(i) == 1) {
                switch (i) {
                    case 0:
                        sb.append("hu").append(",");
                        break;
                    case 1:
                        sb.append("peng").append(",");
                        break;
                    case 2:
                        sb.append("zhao").append(",");
                        break;
                    case 3:
                        sb.append("zha").append(",");
                        break;
                    case 4:
                        sb.append("jian").append(",");
                        break;
                    case 5:
                        sb.append("zimo").append(",");
                        break;
                    case 6:
                        sb.append("hua").append(",");
                        break;
                    default:
                        sb.append("未知").append(i).append(",");
                }
            }

        }
        sb.append("]");
        return sb.toString();
    }
    
    // 跑胡子
    public static List<Integer> cardList = new ArrayList<>();
    /**
     * 牌组
     **/
    public static final Map<Integer, List<int[]>> paiZuMap = new HashMap<>();

    static {
        isTest = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("test"));
        for (int i = 1; i <= 110; i++) {
            cardList.add(i);
        }
        //初始化牌组
        for (int i = 1; i <= 10; i++) {
            paiZuMap.put(i, initPaiZu(i));
        }
        for (int i = 101; i <= 106; i++) {
            paiZuMap.put(i, initPaiZu(i));
        }
        for (int i = 201; i <= 206; i++) {
        	paiZuMap.put(i, initPaiZu(i));
        }

    }

	public static List<int[]> getPaiZu(int val) {
    		return paiZuMap.get(val);
    }

    public static List<int[]> initPaiZu(int val) {
        List<int[]> res = new ArrayList<>();

        //三张一样
        res.add(new int[]{val, val, val});
//        res.add(new int[]{val, val, val,val});
        if(val == 101 || val == 102 || val == 3){//化三千
        	res.add(new int[]{101, 3, 102});
        }else if(val == 103 || val == 104 || val == 1){//孔、一、己
        	res.add(new int[]{103, 1, 104});
        }else if(val == 105 || val == 10 || val == 7){//七、十、土
        	res.add(new int[]{7, 10, 105});
        }else if(val == 8 || val == 9 || val == 106){//八、九、子
        	res.add(new int[]{8, 9, 106});
        }else if(val == 201 || val == 202 || val == 203){//可、知、礼
        	res.add(new int[]{201, 202, 203});
        }else if(val == 204 || val == 205 || val == 206){//上、大、人
        	res.add(new int[]{204, 205, 206});
        }
        
        if(val <= 10){
        	 //做顺
            if (val % 100 == 10) {
                res.add(new int[]{val - 2, val - 1, val});
            } else if (val % 100 == 9) {
                res.add(new int[]{val - 2, val - 1, val});
                res.add(new int[]{val - 1, val, val + 1});
            } else if (val % 100 == 1) {
                res.add(new int[]{val, val + 1, val + 2});
            } else if (val % 100 == 2) {
                res.add(new int[]{val - 1, val, val + 1});
                res.add(new int[]{val, val + 1, val + 2});
            } else {
                res.add(new int[]{val - 2, val - 1, val});
                res.add(new int[]{val - 1, val, val + 1});
                res.add(new int[]{val, val + 1, val + 2});
            }
        }
        return res;
    }
}
