package com.sy599.game.qipai.xtbp.constant;

import java.util.ArrayList;
import java.util.List;

public class XtbpConstants {

	/*** 桌状态叫分 */
    public static final int TABLE_STATUS_JIAOFEN = 1;
    
    /*** 选主 */
    public static final int TABLE_STATUS_XUANZHU = 2;
    
    /*** 埋牌 */
    public static final int TABLE_STATUS_MAIPAI = 3;
    
    /*** 打牌 */
    public static final int TABLE_STATUS_PLAY = 4;
    
    
    /*** 留守 */
    public static final int TABLE_LIUSHOU_PLAY = 5;
    
    
    /*** 扣底 */
    public static final int TABLE_KOUDI = 6;
    
    /*** 定庄 */
    public static final int TABLE_DINGZHUANG = 7;

    //add
	/*** 飘分 */
	public static final int TABLE_STATUS_PIAOFEN = 8;
	/*** 留守 */
	public static final int TABLE_PIAOFEN_PLAY = 9;


	/**埋牌*/
    public static final int REQ_MAIPAI=100;
    
    public static final int RES_KOUDI=200;
    
    
    
    /**托管**/
    public static final int action_tuoguan = 100;

	// public static List<Integer> cardList_16 = new ArrayList<>(52);
	public static List<Integer> cardList = new ArrayList<>(52);
	static {
		// 方片 1 梅花2 红3 黑桃4 5王
		// 三打哈玩法5-A 2
		for (int n = 0; n < 2; n++) {
			for (int i = 1; i <= 4; i++) {
				for (int j = 5; j <= 15; j++) {
					if(j==6){
						continue;
					}
					int card = i * 100 + j;
					cardList.add(card);
				}
			}
			cardList.add(501);//小王
			cardList.add(502);//大王
		}

		// cardList.remove(Integer.valueOf(413));
		// cardList.add(214);
		// cardList.add(415);
	}
	public static void main(String[] args) {
		 List<Integer>	copy = cardList.subList(8,cardList.size());
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
		list.add(cardList.subList(0, 8));
		
		System.out.println(list);

	}
}
