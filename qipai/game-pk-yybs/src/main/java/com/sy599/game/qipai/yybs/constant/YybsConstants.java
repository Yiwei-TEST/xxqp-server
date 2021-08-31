package com.sy599.game.qipai.yybs.constant;

import java.util.ArrayList;
import java.util.List;

public class YybsConstants {
	/*** 初始化位置记录 */
	public static final int TABLE_STATUS_INITSEAT = 0;
	/*** 桌状态叫主 */
	public static final int TABLE_STATUS_JIAOZHU = 1;

	/*** 选主 */
	public static final int TABLE_STATUS_XUANZHU = 2;

	/*** 埋牌 */
	public static final int TABLE_STATUS_MAIPAI = 3;

	/*** 打牌 */
	public static final int TABLE_STATUS_PLAY = 4;


    /*** 选队 独战或者找队友 */
    public static final int TABLE_STATUS_XUANDUI = 10;

    /*** 留守 */
    public static final int TABLE_LIUSHOU_PLAY = 5;
    
    
    /*** 扣底 */
    public static final int TABLE_KOUDI = 6;
    
    /*** 定庄 */
    public static final int TABLE_DINGZHUANG = 7;

    //add
	/*** 飘分 */
	public static final int TABLE_STATUS_PIAOFEN = 8;
	/*** 定队 */
	public static final int TABLE_STATUS_DINGDUI= 9;
	/*** 定队 */
	public static final int TABLE_STATUS_CHANGESEAT= 11;


	/**埋牌*/
    public static final int REQ_MAIPAI=100;
    
    public static final int RES_KOUDI=200;


    
    
    /**托管**/
    public static final int action_tuoguan = 100;
	//不抽牌
	public static List<Integer> cardList_buchoupai = new ArrayList<>(52);
	static {
		// 方片 1 梅花2 红3 黑桃4 5王
		for (int n = 0; n < 2; n++) {
			for (int i = 1; i <= 4; i++) {
				for (int j = 5; j <= 15; j++) {
					int card = i * 100 + j;
					cardList_buchoupai.add(card);
				}
			}
			cardList_buchoupai.add(501);//小王
			cardList_buchoupai.add(502);//大王
		}
	}
	//只抽6
	public static List<Integer> cardList_chou6 = new ArrayList<>(52);
	static {
		// 方片 1 梅花2 红3 黑桃4 5王
		for (int n = 0; n < 2; n++) {
			for (int i = 1; i <= 4; i++) {
				for (int j = 5; j <= 15; j++) {
					if(j==6){
						continue;
					}
					int card = i * 100 + j;
					cardList_chou6.add(card);
				}
			}
			cardList_chou6.add(501);//小王
			cardList_chou6.add(502);//大王
		}
	}
	//
	//只抽红2
	public static List<Integer> cardList_chou2 = new ArrayList<>(52);
	static {
		// 方片 1 梅花2 红3 黑桃4 5王
		for (int n = 0; n < 2; n++) {
			for (int i = 1; i <= 4; i++) {
				for (int j = 5; j <= 15; j++) {
					if(j==15 && (i==1 || i==3)){
						continue;
					}
					int card = i * 100 + j;
					cardList_chou2.add(card);
				}
			}
			cardList_chou2.add(501);//小王
			cardList_chou2.add(502);//大王
		}
	}
	//不带红2且抽6
	public static List<Integer> cardList_chou2_chou6 = new ArrayList<>(52);
	static {
		// 方片 1 梅花2 红3 黑桃4 5王
		for (int n = 0; n < 2; n++) {
			for (int i = 1; i <= 4; i++) {
				for (int j = 5; j <= 15; j++) {
					if(j==6){
						continue;
					}
					if(j==15 && (i==1 || i==3)){
						continue;
					}
					int card = i * 100 + j;
					cardList_chou2_chou6.add(card);
				}
			}
			cardList_chou2_chou6.add(501);//小王
			cardList_chou2_chou6.add(502);//大王
		}
	}
	public static void main(String[] args) {
		 List<Integer>	copy = cardList_chou6.subList(8,cardList_chou6.size());
		int maxCount = copy.size() / 4;
		List<Integer> pai = new ArrayList<>();
		List<List<Integer>> list = new ArrayList<>();

		int j=1;
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (i < j*maxCount) {
				pai.add(card);
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
				j++;
			}
			
		}
		list.add(pai);
		list.add(cardList_chou6.subList(0, 8));
		System.out.println();
		System.out.println(list.get(0).size());

	}
}
