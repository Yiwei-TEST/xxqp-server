package com.sy599.game.qipai.zjmj.constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.zjmj.rule.ZjMj;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;

public class ZjMjConstants {
    // 0胡 1碰 2明杠 3暗杠 4接杠 5杠爆(摸杠胡) 报听6
    /*** 胡*/
    public static final int ACTION_INDEX_HU = 0;
    /*** 碰*/
    public static final int ACTION_INDEX_PENG = 1;
    /*** 明杠*/
    public static final int ACTION_INDEX_MINGGANG = 2;
    /*** 暗杠*/
    public static final int ACTION_INDEX_ANGANG = 2;
    /*** 接杠*/
    public static final int ACTION_INDEX_CHI = 4;//改吃
    /*** 自摸  */
    public static final int ACTION_INDEX_ZIMO = 5;
    /*** 摇杠*/
    public static final int ACTION_INDEX_YAOGANG = 6;

    /*** 自摸次数 */
    public static final int ACTION_COUNT_INDEX_ZIMO = 0;
    /*** 接炮次数 */
    public static final int ACTION_COUNT_INDEX_JIEPAO = 1;
    /*** 点炮次数 */
    public static final int ACTION_COUNT_INDEX_DIANPAO = 2;
    /*** 暗杠次数 */
    public static final int ACTION_COUNT_INDEX_ANGANG = 3;
    /*** 明杠次数 */
    public static final int ACTION_COUNT_INDEX_MINGGANG = 4;
    /*** 中鸟次数 */
    public static final int ACTION_COUNT_INDEX_ZHONGNIAO = 5;

    /*** 抢杠胡  */
    public static final int HU_QIANGGANGHU = 101;
    /*** 自摸胡  */
    public static final int HU_ZIMO = 102;
    /*** 杠开胡  */
    public static final int HU_GANGKAI = 103;
    /*** 接炮胡  */
    public static final int HU_JIPAO = 104;
    /*** 杠上炮  */
    public static final int HU_GANGPAO = 113;
    
    /*** 碰碰胡  */
    public static final int HU_PENGPENGHU = 105;
    /*** 清一色  */
    public static final int HU_QINGYISE = 106;
    /*** 七对  */
    public static final int HU_QIDUI = 107;
    /*** 豪7  */
    public static final int HU_HAOQIDUI = 108;
    /*** 黑胡  */
    public static final int HU_HEIHU = 109;
    /*** 将将胡  */
    public static final int HU_JIANGJIANGHU = 110;
    /*** 板板胡  */
    public static final int HU_BANBANHU = 111;
    /*** 四红中胡  */
    public static final int HU_SIHONGZHONG = 112;
    /*** 双豪7  */
    public static final int HU_SHUANGHAOQIDUI = 114;
    
    /*** 放炮 */
    public static final int HU_FANGPAO = 201;

    public static boolean isTest = false;
    public static boolean isTestAh = false;

    public static List<Integer> hongzhong_mjList = new ArrayList<>();
    
    public static List<Integer> noHongzhong_mjList = new ArrayList<>();

    /*** xx秒后进入托管**/
    public static int AUTO_TIMEOUT = 20;
    /*** 防恶意托管时间**/
    public static int AUTO_TIMEOUT2 = 10;
    /*** 托管后xx秒自动出牌**/
    public static final int AUTO_PLAY_TIME = 0;
    /*** 托管后xx秒自动准备**/
    public static int AUTO_READY_TIME = 10;
    /*** 托管后xx秒自动胡**/
    public static final int AUTO_HU_TIME = 0;


    /*** 桌状态飘分 */
    public static final int TABLE_STATUS_PIAO = 1;

    static {
        if (GameServerConfig.isDeveloper()) {
            isTest = true;
            isTestAh = true;
        }
        // ///////////////////////
        // 筒万条 108张+4红中
        for (int i = 1; i <= 108; i++) {
            hongzhong_mjList.add(i);
            noHongzhong_mjList.add(i);
        }
        //红中
        for (int i = 201; i <= 204; i++) {
            hongzhong_mjList.add(i);
        }

        String autoPlayConfig = ResourcesConfigsUtil.loadServerPropertyValue("autoPlayConfigHzMj", "");
        if (StringUtils.isNotBlank(autoPlayConfig)) {
            String[] split = autoPlayConfig.split(",");
            AUTO_TIMEOUT = Integer.valueOf(split[0]);
            AUTO_TIMEOUT2 = Integer.valueOf(split[1]);
            AUTO_READY_TIME = Integer.valueOf(split[2]);
        }
    }

    /***
     * 根据玩法获取牌
     * @return
     */
    public static List<Integer> getMajiangList() {
        return new ArrayList<>(hongzhong_mjList);
    }
    
    public static List<Integer> getNoHongzhong_mjList() {
        return new ArrayList<>(noHongzhong_mjList);
    }
    

    public static List<ZjMj> getMajiangPai() {
        List<ZjMj> majiangs = new ArrayList<>();
        for (int i = 1; i <= 27; i++) {
            majiangs.add(ZjMj.getMajang(i));
        }
        return majiangs;
    }
    

	 /**
    * 是否有胡操作
    *
    * @param actionList
    * @return
    */
   public static boolean hasHu(List<Integer> actionList) {
       return hasAction(actionList, ACTION_INDEX_HU) || hasAction(actionList,ACTION_INDEX_ZIMO);
   }
   
   /**
    * 是否有胡操作
    *
    * @param actionList
    * @return
    */
   public static boolean hasGang(List<Integer> actionList) {
       return hasAction(actionList, ACTION_INDEX_MINGGANG);
   }
   
   /**
    * 是否有操作
    *
    * @param actionList
    * @param action     操作
    * @return
    */
   private static boolean hasAction(List<Integer> actionList, int action) {
       if (actionList != null && actionList.size() > action) {
           return actionList.get(action) == 1;
       }
       return false;
   }

   /**
    * 只保留胡操作
    *
    * @param actionList
    * @return
    */
   public static List<Integer> keepHu(List<Integer> actionList) {
       if (actionList == null || actionList.size() < ACTION_INDEX_HU + 1) {
           return Collections.EMPTY_LIST;
       }
       List<Integer> res = new ArrayList<>();
       for (int i = 0; i < actionList.size(); i++) {
           if (i == ACTION_INDEX_HU) {
               res.add(actionList.get(i));
           } else {
               res.add(0);
           }
       }
       return res;
   }
}
