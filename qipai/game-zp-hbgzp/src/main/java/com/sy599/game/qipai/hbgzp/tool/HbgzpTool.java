package com.sy599.game.qipai.hbgzp.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.hbgzp.bean.HbgzpDisAction;
import com.sy599.game.qipai.hbgzp.bean.HbgzpHandCard;
import com.sy599.game.qipai.hbgzp.bean.HbgzpPlayer;
import com.sy599.game.qipai.hbgzp.bean.HbgzpTable;
import com.sy599.game.qipai.hbgzp.constant.HbgzpConstants;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;
import com.sy599.game.qipai.hbgzp.rule.HbgzpCardIndexArr;
import com.sy599.game.qipai.hbgzp.rule.HbgzpIndex;

/**
 * 跑胡子
 *
 * @author lc
 */
public class HbgzpTool {

    /**
     * 发牌
     *
     * @param copy
     * @param t
     * @return
     */

    public static List<List<Hbgzp>> zuopai(List<Integer> copy, List<List<Integer>> t,int playerCount){
        if(t.size()<playerCount)
            return null;
        List<List<Hbgzp>> list = new ArrayList<>();
        for (List<Integer> zp : t) {
            list.add(find(copy, zp));
        }
        if(list.size()>playerCount){
//            List<PaohzCard> l=list.get(playerCount);
//            for (int i = 0; i < copy.size(); i++) {
//                l.add(PaohzCard.getPaohzCard(copy.get(i)));
//            }
//            list.add(l);
        }else if(list.size()==playerCount){
            List<Hbgzp> l=new ArrayList<>();
            for (int i = 0; i < copy.size(); i++) {
                l.add(Hbgzp.getPaohzCard(copy.get(i)));
            }
            list.add(l);
        }
        return list;
    }
    
    
    
    public static synchronized List<List<Hbgzp>> fapai(List<Integer> copy, int playerCount) {
        List<List<Hbgzp>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<Hbgzp> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(Hbgzp.getPaohzCard(id));
        }
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                list.add(new ArrayList<>(allMjs.subList(0, 20)));
            } else {
                list.add(new ArrayList<>(allMjs.subList(20 + (i - 1) * 19, 20 + (i - 1) * 19 + 19)));
            }
            if (i == playerCount - 1) {
                list.add(new ArrayList<>(allMjs.subList(20 + (i) * 19, allMjs.size())));
            }

        }
        return list;
    }

    public static synchronized List<List<Hbgzp>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<Hbgzp>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<Hbgzp>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(HbgzpHelper.find(copy, zp));
            }
        }
        List<Hbgzp> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(Hbgzp.getPaohzCard(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<Hbgzp> pai = zpList.get(0);
                    int len = 20 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 20)));
                }
            } else {
                if (zpList.size() > i) {
                    List<Hbgzp> pai = zpList.get(i);
                    int len = 19 - pai.size();
                    pai.addAll(allMjs.subList(count, count + len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, count + 19)));
                    count += 19;
                }
            }
            if (i == playerCount - 1) {
                if (zpList.size() > i + 1) {
                    List<Hbgzp> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
        return list;
    }




    public static boolean allReFaPai(List<List<Hbgzp>> list,int playerCount){
        if(!checkShuangLong(list,playerCount))
            return false;
        if(!checkWuKan(list,playerCount))
            return false;
        return true;
    }

    /**
     * 检测五坎
     * @param list
     * @param playerCount
     * @return
     */
    public static  boolean checkWuKan(List<List<Hbgzp>> list,int playerCount){
        for (int i = 0; i < playerCount; i++) {
            List<Hbgzp> cards = list.get(i);
            int []nums=new int[21];
            for (Hbgzp card:cards) {
                int k;
                int val = card.getVal();
                if(val<100){
                    k=val-1;
                }else if(val >200){
                	k=val-181;
                }else {
                    k=val-91;
                }
                nums[k]++;
            }
            int count=0;
            for (int j = 0; j < nums.length; j++) {
                if(nums[j]==3){
                    count++;
                }
            }
            if (count>3)
                return false;
        }
        return true;
    }

    /**
     * 检测双龙
     * @param list
     * @param playerCount
     * @return
     */
    public static  boolean checkShuangLong(List<List<Hbgzp>> list,int playerCount){
        for (int i = 0; i < playerCount; i++) {
            List<Hbgzp> cards = list.get(i);
            int []nums=new int[21];
            for (Hbgzp card:cards) {
                int k;
                int val = card.getVal();
                if(val<100){
                    k=val-1;
                }else if(val > 200){
                	k=val-181;
                }else {
                    k=val-91;
                }
                nums[k]++;
            }
            int count=0;
            for (int j = 0; j < nums.length; j++) {
                if(nums[j]==4){
                    count++;
                }
            }
            if (count>=2)
                return false;
        }
        return true;
    }
    

    public static boolean isTing(HbgzpPlayer player) {
        List<Hbgzp> majiangIds = new ArrayList<>(player.getHandMajiang());
        boolean isshaopai = false;
        if(player.getZhaCount()%3 ==1){
			if(majiangIds.size() % 3 == 2){
				isshaopai = true;
			}
		}else if(player.getZhaCount()%3 ==2){
			if(majiangIds.size() % 3 == 0){
				isshaopai = true;
			}
		}else{
			if(majiangIds.size() % 3 == 1){
				isshaopai = true;
			}
		}
        
        if (!isshaopai) {
            return false;
        }
        HbgzpTable table = player.getPlayingTable(HbgzpTable.class);
        if (table != null && !player.getChi().isEmpty()) {
            List<Hbgzp> allMajiangs = new ArrayList<>();
            allMajiangs.addAll(majiangIds);
            allMajiangs.addAll(player.getaGang());
            allMajiangs.addAll(player.getmGang());
            allMajiangs.addAll(player.getPeng());
            allMajiangs.addAll(player.getChi());
        }
//        getMax(majiangIds);
        return false;
    }

    /**
     * 检查麻将是否有重复
     *
     * @param majiangs
     * @return
     */
    public static boolean isPaohuziRepeat(List<Hbgzp> majiangs) {
        if (majiangs == null) {
            return false;
        }

        Map<Integer, Integer> map = new HashMap<>();
        for (Hbgzp mj : majiangs) {
            int count = 0;
            if (map.containsKey(mj.getId())) {
                count = map.get(mj.getId());
            }
            map.put(mj.getId(), count + 1);
        }
        for (int count : map.values()) {
            if (count > 1) {
                return true;
            }
        }
        return false;
    }


    /**
     * 找出相同的值
     *
     * @param copy
     * @param val
     * @return
     */
    public static List<Hbgzp> findPhzByVal(List<Hbgzp> copy, int val) {
        List<Hbgzp> list = new ArrayList<>();
        for (Hbgzp phz : copy) {
            if (phz.getVal() == val) {
                list.add(phz);
            }
        }
        return list;
    }
    /**
     * 找出相同的值的牌个数
     *
     * @param copy
     * @param val
     * @return
     */
    public static int getSameByVal(List<Hbgzp> copy, int val) {
    	int i = 0;
    	for (Hbgzp phz : copy) {
    		if (phz.getVal() == val) {
    			i++;
    		}
    	}
    	return i;
    }

    private static List<Hbgzp> find(List<Integer> copy, List<Integer> valList) {
        List<Hbgzp> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    Hbgzp phz = Hbgzp.getPaohzCard(card);
                    if (phz.getVal() == zpId) {
                        pai.add(phz);
                        iterator.remove();
                        break;
                    }
                }
            }

        }
        return pai;
    }

    /**
     * 转化为Map<val,valNum>
     */
    public static Map<Integer, Integer> toPhzValMap(List<Hbgzp> phzs) {
        Map<Integer, Integer> ids = new HashMap<>();
        if (phzs == null) {
            return ids;
        }
        for (Hbgzp phz : phzs) {

            if (ids.containsKey(phz.getVal())) {
                ids.put(phz.getVal(), ids.get(phz.getVal()) + 1);
            } else {
                ids.put(phz.getVal(), 1);
            }
        }
        return ids;
    }

    /**
     * 去除重复转化为val
     */
    public static List<Integer> toPhzRepeatVals(List<Hbgzp> phzs) {
        List<Integer> ids = new ArrayList<>();
        if (phzs == null) {
            return ids;
        }
        for (Hbgzp phz : phzs) {
            if (!ids.contains(phz.getVal())) {
                ids.add(phz.getVal());
            }
        }
        return ids;
    }

    /**
     * Id转化为牌
     *
     * @param phzIds
     * @return
     */
    public static List<Hbgzp> toPhzCards(List<Integer> phzIds) {
        List<Hbgzp> cards = new ArrayList<>();
        for (int id : phzIds) {
            cards.add(Hbgzp.getPaohzCard(id));
        }
        return cards;
    }

    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toPhzCardZeroIds(List<?> phzs) {
        List<Integer> ids = new ArrayList<>();
        if (phzs == null) {
            return ids;
        }
        for (int i = 0; i < phzs.size(); i++) {
            if (i == 0) {
                ids.add((Integer) phzs.get(i));
            } else {
                ids.add(0);
            }
        }
        return ids;
    }

    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toPhzCardIds(List<Hbgzp> phzs) {
        if (phzs == null) {
            return Collections.emptyList();
        }

        List<Integer> ids = new ArrayList<>(phzs.size());
        for (Hbgzp phz : phzs) {
            ids.add(phz.getId());
        }
        return ids;
    }

    /**
     * 跑胡子转化为牌s
     */
    public static List<Integer> toPhzCardVals(List<Hbgzp> phzs, boolean matchCase) {
        List<Integer> majiangIds = new ArrayList<>();
        if (phzs == null) {
            return majiangIds;
        }
        for (Hbgzp card : phzs) {
            if (matchCase) {
                majiangIds.add(card.getVal());
            } else {
                majiangIds.add(card.getPai());
            }
        }

        return majiangIds;
    }
    
    /**
	 * 麻将转化为majiangIds
	 * 
	 * @param majiangs
	 * @return
	 */
	public static List<Integer> toMajiangIds(List<Hbgzp> majiangs) {
		List<Integer> majiangIds = new ArrayList<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (Hbgzp majiang : majiangs) {
			majiangIds.add(majiang.getId());
		}
		return majiangIds;
	}
	/**
	 * 麻将Id转化为麻将
	 * 
	 * @param majiangIds
	 * @return
	 */
	public static List<Hbgzp> toMajiang(List<Integer> majiangIds) {
		if (majiangIds == null) {
			return new ArrayList<>();
		}
		List<Hbgzp> majiangs = new ArrayList<>();
		for (int majiangId : majiangIds) {
			if (majiangId == 0) {
				continue;
			}
			majiangs.add(Hbgzp.getPaohzCard(majiangId));
		}
		return majiangs;
	}
	/**
	 * 得到某个值的麻将
	 * 
	 * @param copy
	 * @return
	 */
	public static List<Hbgzp> getVal(List<Hbgzp> copy, int val) {
		List<Hbgzp> hongzhong = new ArrayList<>();
		Iterator<Hbgzp> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Hbgzp majiang = iterator.next();
			if (majiang.getVal() == val) {
				hongzhong.add(majiang);
			}
		}
		return hongzhong;
	}
	/**
	 * 麻将转化为majiangIds
	 * 
	 * @param majiangs
	 * @return
	 */
	public static List<Integer> toMajiangVals(List<Hbgzp> majiangs) {
		List<Integer> majiangIds = new ArrayList<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (Hbgzp majiang : majiangs) {
			majiangIds.add(majiang.getVal());
		}
		return majiangIds;
	}

    
    /**
	 * 将array组合成用delimiter分隔的字符串
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static List<Hbgzp> explodeMajiang(String str, String delimiter) {
		List<Hbgzp> list = new ArrayList<>();
		if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
			return list;
		String strArray[] = str.split(delimiter);

		for (String val : strArray) {
			Hbgzp majiang = null;
			if (val.startsWith("hp")) {
				majiang = Hbgzp.valueOf(Hbgzp.class, val);
			} else {
				Integer intVal = (Integer.valueOf(val));
				if (intVal == 0) {
					continue;
				}
				majiang = Hbgzp.getPaohzCard(intVal);
			}
			list.add(majiang);
		}
		return list;
	}
    /**
     * 得到最大相同数
     */
    public static HbgzpCardIndexArr getMax(List<Hbgzp> list) {
        HbgzpCardIndexArr card_index = new HbgzpCardIndexArr();
        Map<Integer, List<Hbgzp>> phzMap = new HashMap<>();
        for (Hbgzp phzCard : list) {
            List<Hbgzp> count;
            if (phzMap.containsKey(phzCard.getVal())) {
                count = phzMap.get(phzCard.getVal());
            } else {
                count = new ArrayList<>();
                phzMap.put(phzCard.getVal(), count);
            }
            count.add(phzCard);
        }
        for (int phzVal : phzMap.keySet()) {
            List<Hbgzp> phzList = phzMap.get(phzVal);
            switch (phzList.size()) {
                case 1:
                    card_index.addPaohzCardIndex(0, phzList, phzVal);
                    break;
                case 2:
                    card_index.addPaohzCardIndex(1, phzList, phzVal);
                    break;
                case 3:
                    card_index.addPaohzCardIndex(2, phzList, phzVal);
                    break;
                case 4:
                    card_index.addPaohzCardIndex(3, phzList, phzVal);
                    break;
                case 5:
                	card_index.addPaohzCardIndex(4, phzList, phzVal);
                	break;
            }
        }
        return card_index;
    }
    /**
     * 得到最大相同数
     */
    public static int getMaxCountByVal(List<Hbgzp> list) {
    	Map<Integer, List<Hbgzp>> phzMap = new HashMap<>();
    	for (Hbgzp phzCard : list) {
    		List<Hbgzp> count;
    		if (phzMap.containsKey(phzCard.getVal())) {
    			count = phzMap.get(phzCard.getVal());
    		} else {
    			count = new ArrayList<>();
    			phzMap.put(phzCard.getVal(), count);
    		}
    		count.add(phzCard);
    	}
    	int i=0;
    	for (int phzVal : phzMap.keySet()) {
    		List<Hbgzp> phzList = phzMap.get(phzVal);
    		if(phzList.size() > i){
    			i=phzList.size();
    		}
    	}
    	return i;
    }

    public static List<Hbgzp> findPhzCards(List<Hbgzp> cards, List<Integer> vals) {
        List<Hbgzp> findList = new ArrayList<>();
        for (int chiVal : vals) {
            boolean find = false;
            for (Hbgzp card : cards) {
                if (findList.contains(card)) {
                    continue;
                }
                if (card.getVal() == chiVal) {
                    findList.add(card);
                    find = true;
                    break;
                }
            }
            if (!find) {
                findList.clear();
                break;
            }
        }
        return findList;
    }

    /**
     * 检查出相同的牌
     */
    public static List<Hbgzp> getSameCards(List<Hbgzp> handCards, Hbgzp disCard) {
        List<Hbgzp> list = findCountByVal(handCards, disCard, true);
        if (list != null) {
            return list;
        }
        return null;

    }

    /**
     * 是否一样的牌
     */
    public static boolean isSameCard(List<Hbgzp> handCards) {
    	int val = 0;
        for (Hbgzp card : handCards) {
            if (val == 0) {
                val = card.getVal();
                continue;
            }
            if (val != card.getVal()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除牌
     *
     * @param handCards
     * @param cardVal
     * @return
     */
    public static void removePhzByVal(List<Hbgzp> handCards, int cardVal) {
        Iterator<Hbgzp> iterator = handCards.iterator();
        while (iterator.hasNext()) {
        	Hbgzp paohzCard = iterator.next();
            if (paohzCard.getVal() == cardVal) {
                iterator.remove();
            }

        }
    }

    /**
     * 是否有这张相同的牌
     */
    public static boolean isHasCardVal(List<Hbgzp> handCards, int cardVal) {
        for (Hbgzp card : handCards) {
            if (cardVal == card.getVal()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param phzList   牌List
     * @param o         值or牌
     * @param matchCase 是否为值（true取val，false取pai）
     * @return 返回与o的值相同的牌的集合
     */
    public static List<Hbgzp> findCountByVal(List<Hbgzp> phzList, Object o, boolean matchCase) {
        int val;
        if (o instanceof Hbgzp) {
            if (matchCase) {
                val = ((Hbgzp) o).getVal();

            } else {
                val = ((Hbgzp) o).getPai();

            }
        } else if (o instanceof Integer) {
            val = (int) o;
        } else {
            return null;
        }
        List<Hbgzp> result = new ArrayList<>();
        for (Hbgzp card : phzList) {
            int matchVal;
            if (matchCase) {
                matchVal = card.getVal();
            } else {
                matchVal = card.getPai();
            }
            if (matchVal == val) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * 得到某个值的麻将
     *
     * @param copy
     * @return
     */
    public static List<Hbgzp> getVals(List<Hbgzp> copy, int val, Hbgzp... exceptCards) {
        List<Hbgzp> list = new ArrayList<>();
        Iterator<Hbgzp> iterator = copy.iterator();
        while (iterator.hasNext()) {
        	Hbgzp phz = iterator.next();
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (Hbgzp except : exceptCards) {
                    if (phz == except) {
                        findExcept = true;
                        break;
                    }
                }
                if (findExcept) {
                    continue;
                }
            }
            if (phz.getVal() == val) {
                list.add(phz);
            }
        }
        return list;
    }

    /**
     * 得到某个值的跑胡子牌
     *
     * @param copy 手牌
     */
    public static Hbgzp getVal(List<Hbgzp> copy, int val, Hbgzp... exceptCards) {
        for (Hbgzp phz : copy) {
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (Hbgzp except : exceptCards) {
                    if (phz == except) {
                        findExcept = true;
                        break;
                    }
                }
                if (findExcept) {
                    continue;
                }
            }
            if (phz.getVal() == val) {
                return phz;
            }
        }
        return null;
    }

    public static void sortMin(List<Hbgzp> hasPais) {
        Collections.sort(hasPais, new Comparator<Hbgzp>() {
            @Override
            public int compare(Hbgzp o1, Hbgzp o2) {
                if (o1.getPai() < o2.getPai()) {
                    return -1;
                } else if (o1.getPai() > o2.getPai()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    /**
     * 去除重复后得到val的集合
     *
     * @param cards
     * @return
     */
    public static Map<Integer, Integer> getDistinctVal(List<Hbgzp> cards) {
        Map<Integer, Integer> valIds = new HashMap<>();
        if (cards == null) {
            return valIds;
        }
        for (Hbgzp phz : cards) {
            if (valIds.containsKey(phz.getVal())) {
                valIds.put(phz.getVal(), valIds.get(phz.getVal()) + 1);
            } else {
                valIds.put(phz.getVal(), 1);
            }
        }
        return valIds;
    }

    /**
     * 算出的牌的胡息
     *红字一句话1个子
		黑字一句话不算子
		拿在手上三个黑的算1个子，对下去三个黑的没子
		拿在手上三个红的算2个子，对下去三个红的算1个子
		一三五七九带花。带花的牌算一个子。花乙花九都算1个子。
		三五七：三1个子，花三2个子，五2个子，花五4个子，七1个子，花七2个子
		四个红字4个子，五个红字5个子
		四个黑字2个子，五个黑字3个子
		（例：手上三个七，两个花七一个不带花，2+2+1+2=7个子；对下去三个七，两个花七一个不带花，2+2+1+1=6个子）
		招：招四个黑字算2个子，开招4个红字算4个子，开完招后马上补一张牌，补到第五张算5个子，没补到第五张，下一轮再摸到就不能滑。
		滑：滑到五个黑字3个子，华到五个红字5个子。

     * @param action
     * @param cards
     * @return
     */
    public static int getOutCardHuxi(int action, List<Hbgzp> cards) {
        int huxi = 0;
        if (action == 0) {
            return huxi;
        }
       //四个红字4个子，五个红字5个子    四个黑字2个子，五个黑字3个子
        if (action == HbgzpDisAction.action_angang || action == HbgzpDisAction.action_minggang) {
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 4) {
                    continue;
                }
                
                if(Hbgzp.isHongpai(entry.getKey())){
                	return 4;
                }else{
                	return 2;
                }
            }

        }else if (action == HbgzpDisAction.action_hua) {
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
//                if (entry.getValue() != 5) {
//                    continue;
//                }
                if(Hbgzp.isHongpai(entry.getKey())){
                	return 5;
                }else{
                	return 3;
                }
            }

        } else if (action == HbgzpDisAction.action_chi) {
            // 吃 只有123 和2710 大的6分 小的3分
            List<Hbgzp> copy = new ArrayList<>(cards);
            sortMin(copy);
            huxi = getShunHuxi(copy);
        } else if (action == HbgzpDisAction.action_peng) {
            // 大的3分 小的1分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 3) {
                    continue;
                }
                if(Hbgzp.isHongpai(entry.getKey())){
                	return 1;
                }
            }

        } else if (action == HbgzpDisAction.action_kan) {
            // 大的6分 小的3分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 3) {
                    continue;
                }
                if(Hbgzp.isHongpai(entry.getKey())){
                	return 2;
                }else{
                	return 1;
                }
            }
        }
        return huxi;
    }

    /**
     * 算顺子的胡息 123 和2710 分大小 大6小3
     *
     * @param hasMajiangList
     * @return
     */
    private static int getShunHuxi(List<Hbgzp> hasMajiangList) {
        if (hasMajiangList.size() < 3) {
            return 0;
        }
        if (hasMajiangList.get(0).getPai() == hasMajiangList.get(1).getPai()) {
            //不是顺子
            return 0;
        }
        //new 红字一句话1个子  黑字一句话不算子
        for (Hbgzp card :hasMajiangList) {
			if(!card.isHongpai()){//有一个不是红牌则返回0
				return 0;
			}
		}
        return 1;
//        Hbgzp minCard = hasMajiangList.get(0);
        
//        if (minCard.getPai() == 1) {
//            if (minCard.isBig()) {
//                return 6;
//            } else {
//                return 3;
//            }
//        } else if (minCard.getPai() == 2) {
//            for (Hbgzp card : hasMajiangList) {
//                if (!c2710List.contains(card.getPai())) {
//                    return 0;
//                }
//            }
//            if (minCard.isBig()) {
//                return 6;
//            } else {
//                return 3;
//            }
//        }
//        return 0;
    }



    /**
     * 得到可以操作的牌 4张和3张是不能操作的
     */
    public static HbgzpHandCard getPaohuziHandCardBean(List<Hbgzp> handPais,List<Integer> jianCards) {
        HbgzpHandCard card = new HbgzpHandCard();
        List<Hbgzp> copy = new ArrayList<>(handPais);
        card.setHandCards(new ArrayList<>(copy));
        if(jianCards == null){
        	jianCards = new ArrayList<>();
        }
        card.setJianCards(new ArrayList<>(jianCards));
        HbgzpCardIndexArr valArr = HbgzpTool.getMax(copy);
        card.setIndexArr(valArr);
        // 去掉4张和3张
//        PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
//        if (index3 != null) {
//            copy.removeAll(index3.getPaohzList());
//        }
//        PaohuziIndex index2 = valArr.getPaohzCardIndex(2);
//        if (index2 != null) {
//            copy.removeAll(index2.getPaohzList());
//        }
        card.setOperateCards(copy);
        return card;
    }

    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @return String
     */
    public static List<Hbgzp> explodePhz(String str, String delimiter) {
        List<Hbgzp> list = new ArrayList<>();
        if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
            return list;
        String strArray[] = str.split(delimiter);

        for (String val : strArray) {
        	Hbgzp phz = null;
            if (val.startsWith("mj")) {
                phz = Hbgzp.valueOf(Hbgzp.class, val);
            } else {
                phz = Hbgzp.getPaohzCard((Integer.valueOf(val)));
            }
            list.add(phz);
        }
        return list;
    }

    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @param array
     * @param delimiter
     * @return String
     */
    public static String implodePhz(List<Hbgzp> array, String delimiter) {
        if (array == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        for (Hbgzp i : array) {
            sb.append(i.getId());
            sb.append(delimiter);
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    /**
     * 自动出牌
     */
    public static Hbgzp autoDisCard(List<Hbgzp> handPais) {
        List<Hbgzp> copy = new ArrayList<>(handPais);
        HbgzpCardIndexArr valArr = HbgzpTool.getMax(copy);
        HbgzpIndex index1 = valArr.getPaohzCardIndex(1);
        HbgzpIndex index0 = valArr.getPaohzCardIndex(0);
        int val = 0;
        if (index0 != null && !index0.getValList().isEmpty()) {
            for (int i = 0; i < index0.getValList().size(); i++) {
                if (val == 0) {
                    val = index0.getValList().get(i);
                    continue;
                }
                if (val + 1 != index0.getValList().size()) {
                    break;
                }
                if (i + 1 % 3 == 0) {
                    val = 0;
                }
            }
            if (val == 0 && index1 != null && !index1.getValList().isEmpty()) {
                val = Collections.min(index1.getValList());
            }
        } else if (index1 != null && !index1.getValList().isEmpty()) {
            val = Collections.min(index1.getValList());
        }
        if (val == 0) {
            return null;
        } else {
        	Hbgzp card = null;
            for (Hbgzp paohzCard : handPais) {
                if (paohzCard.getVal() == val) {
                    card = paohzCard;
                    break;
                }
            }
            return card;
        }
    }

    
   
    
    
    public static List<HbgzpHuLack> isHuNew1(HbgzpHandCard handCardBean, Hbgzp disCard,HbgzpPlayer player ,boolean isSelfMo) {
    	
        HbgzpHuLack lack = new HbgzpHuLack(0);
        lack.setSelfMo(isSelfMo);
        lack.setCheckCard(disCard);

        HbgzpCardIndexArr arr = handCardBean.getIndexArr();
        //判断扎过的牌  扎过一次则拿掉一个扎过的牌 再去判断牌型胡牌
        int zhaCount = player.getZhaCount();
        if(zhaCount>0){
        	HbgzpIndex index3 = arr.getPaohzCardIndex(3);
        	HbgzpIndex index4 = arr.getPaohzCardIndex(4);
        	List<Integer> jianCards = handCardBean.getJianCards();
        	Map<Integer, List<Hbgzp>> sameCardMap = new HashMap<>();
        	if(index3 != null)
        		for (Entry<Integer, List<Hbgzp>> entry :index3.getPaohzValMap().entrySet()) {
					boolean isAdd = true;
					List<Hbgzp> gzps = entry.getValue();
        			if(gzps != null && gzps.size() > 0){
						for (Hbgzp gzp:gzps) {
							if(jianCards.contains(gzp.getId())){
								isAdd = false;
							}
						}
					}
        			if(isAdd){
        				sameCardMap.put(entry.getKey(),entry.getValue());
        			}
				}
        		
        	if(index4 != null)
//        		sameCardMap.putAll(index4.getPaohzValMap());
        		for (Entry<Integer, List<Hbgzp>> entry :index4.getPaohzValMap().entrySet()) {
					int jianCount = 0;
					List<Hbgzp> gzps = entry.getValue();
        			if(gzps != null && gzps.size() > 0){
						for (Hbgzp gzp:gzps) {
							if(jianCards.contains(gzp.getId())){
								jianCount++;
							}
						}
					}
        			if(jianCount <=1){
        				sameCardMap.put(entry.getKey(),entry.getValue());
        			}
				}
        	
        	int count = sameCardMap.size();
        	if(count < zhaCount){
        		return null;
        	}
        	//删除掉所所有扎得牌 比较牌型
        	if(count == zhaCount){
        		List<HbgzpHuLack> huList = new ArrayList<>();
        		HbgzpHandCard handCardBeanCopy = handCardBean.clone();
        		HbgzpHuLack lackCopy = lack.clone();
        		for (Entry<Integer, List<Hbgzp>> entry : sameCardMap.entrySet()) {
        			shanZhaCards(handCardBeanCopy, lackCopy, entry);
				}
        		List<Hbgzp> handPais = handCardBeanCopy.getOperateCards();
        		sortMin(handPais);
        		chaiPaiNew(huList, lackCopy, handPais, disCard,handCardBeanCopy.getJianCards(),new ArrayList<Hbgzp>(),isSelfMo);
        		return huList;
        	}
        	//当扎的次数小于我手上可扎得牌的时候
        	/**扎1次 21张牌，牌型里面最多出现5个可扎的牌，遍历删除某个是否可以胡   5-1=4
    		 * 扎2次 22张牌，牌型里面最多出现5个可扎的牌，遍历删除某两个是否可以胡 5-2=3
    		 * 扎3次 23张牌，牌型里面最多出现5个可扎的牌，遍历留某两个（删除3个）是否可以胡 5-3=2
     		 * 扎4次 24张牌，牌型里面最多出现6个可扎的牌，遍历留某两个（删除4个）是否可以胡 6-4=2
    		 * 扎5次 25张牌，牌型里面最多出现6个可扎的牌，遍历留某一个（删除5个）是否可以胡 6-5=1
    		 * 扎6次 26张牌，牌型里面最多出现6个可扎的牌，全部删除是否可以胡
    		 * */
        	int diffVal = count-zhaCount;
        	if(diffVal > 0){//差距大于2的时候则只可能是扎了1次或2次
        		List<HbgzpHuLack> huList = new ArrayList<>();
        		for (Entry<Integer, List<Hbgzp>> entry : sameCardMap.entrySet()) {
        			Map<Integer, List<Hbgzp>> sameCardMapNew1 = new HashMap<>(sameCardMap);
        			List<Integer> compareVal = new ArrayList<>();//已经比较过的组合
        			int shanCount = 1;
        			HbgzpHandCard handCardBeanCopy1 = handCardBean.clone();
        			HbgzpHandCard handCardBeanCopy2 = null;
        			HbgzpHandCard handCardBeanCopy3 = null;
        			HbgzpHandCard handCardBeanCopy4 = null;
        			HbgzpHandCard handCardBeanCopy5 = null;
        			HbgzpHuLack lackCopy1 = lack.clone();
        			HbgzpHuLack lackCopy2 = null;
        			HbgzpHuLack lackCopy3 = null;
        			HbgzpHuLack lackCopy4 = null;
        			HbgzpHuLack lackCopy5 = null;
        			
        			shanZhaCards(handCardBeanCopy1, lackCopy1, entry);
        			sameCardMapNew1.remove(entry.getKey());
        			compareVal.add(entry.getKey());
        			//删第2个
                    if(zhaCount >= 2){//扎了2个的时候删了1个才能删第2个
                    	Map<Integer, List<Hbgzp>> sameCardMapNew2 = new HashMap<>(sameCardMapNew1);
                    	for (Entry<Integer, List<Hbgzp>> entry2 : sameCardMapNew1.entrySet()) {
                    		if(!compareVal.contains(entry2.getKey())){
                    			handCardBeanCopy2 = handCardBeanCopy1.clone();
                    			lackCopy2 = lackCopy1.clone();
                    			shanZhaCards(handCardBeanCopy2, lackCopy2, entry2);
                    			sameCardMapNew2.remove(entry2.getKey());
                    			shanCount = 2;
                    			if(zhaCount==2){
                    			   List<Hbgzp> handPais = handCardBeanCopy2.getOperateCards();
                                   sortMin(handPais);
                                   chaiPaiNew(huList, lackCopy2, handPais, disCard,handCardBean.getJianCards(),new ArrayList<Hbgzp>(),isSelfMo);
                    			}
                    		}
                    		//删第3个
                    		Map<Integer, List<Hbgzp>> sameCardMapNew3 = new HashMap<>(sameCardMapNew2);
                            if(zhaCount >= 3 && shanCount == 2 && handCardBeanCopy2 !=null){//扎了3个的时候已经成功删了2个才能删第3个
                            	for (Entry<Integer, List<Hbgzp>> entry3 : sameCardMapNew2.entrySet()) {
                            		if(!compareVal.contains(entry3.getKey()) && entry3.getKey() != entry2.getKey()){
                            			handCardBeanCopy3 = handCardBeanCopy2.clone();
                            			lackCopy3 = lackCopy2.clone();
                            			shanZhaCards(handCardBeanCopy3, lackCopy3, entry3);
                            			sameCardMapNew3.remove(entry3.getKey());
                            			shanCount = 3;
                            			if(zhaCount==3){
                             			   List<Hbgzp> handPais = handCardBeanCopy3.getOperateCards();
                                            sortMin(handPais);
                                            chaiPaiNew(huList, lackCopy3, handPais, disCard,handCardBean.getJianCards(),new ArrayList<Hbgzp>(),isSelfMo);
                             			}
                            		}
                            		 //删第4个
                            		Map<Integer, List<Hbgzp>> sameCardMapNew4 = new HashMap<>(sameCardMapNew3);
                                    if(zhaCount >= 4 && shanCount == 3 && handCardBeanCopy3 !=null){//扎了4个的时候已经成功删了3个才能删第4个
                                    	for (Entry<Integer, List<Hbgzp>> entry4 : sameCardMapNew3.entrySet()) {
                                    		if(!compareVal.contains(entry4.getKey())   
                                    				&& entry4.getKey() != entry2.getKey() && entry4.getKey() != entry3.getKey()){
                                    			handCardBeanCopy4 = handCardBeanCopy3.clone();
                                    			lackCopy4 = lackCopy3.clone();
                                    			shanZhaCards(handCardBeanCopy4, lackCopy4, entry4);
                                    			shanCount  = 4;
                                    			sameCardMapNew4.remove(entry4.getKey());
                                    			if(zhaCount==4){
                                      			   List<Hbgzp> handPais = handCardBeanCopy4.getOperateCards();
                                                   sortMin(handPais);
                                                   chaiPaiNew(huList, lackCopy4, handPais, disCard,handCardBean.getJianCards(),new ArrayList<Hbgzp>(),isSelfMo);
                                      			}
                                    		}
                                    		 //删第5个
                                    		Map<Integer, List<Hbgzp>> sameCardMapNew5 = new HashMap<>(sameCardMapNew4);
                                            if(zhaCount >= 5 && shanCount == 4 && handCardBeanCopy4 !=null){//扎了5个的时候已经成功删了4个才能删第5个
                                            	for (Entry<Integer, List<Hbgzp>> entry5 : sameCardMapNew4.entrySet()) {
                                            		if(!compareVal.contains(entry5.getKey()) && entry5.getKey() != entry2.getKey()
                                            				&& entry5.getKey() != entry3.getKey() && entry5.getKey() != entry4.getKey()){
                                            			handCardBeanCopy5 = handCardBeanCopy4.clone();
                                            			lackCopy5 = lackCopy4.clone();
                                            			shanZhaCards(handCardBeanCopy5, lackCopy5, entry5);
                                            			sameCardMapNew5.remove(entry5.getKey());
                                            			shanCount = 5;
                                            			if(zhaCount==5){
                                               			    List<Hbgzp> handPais = handCardBeanCopy5.getOperateCards();
                                                            sortMin(handPais);
                                                            chaiPaiNew(huList, lackCopy5, handPais, disCard,handCardBean.getJianCards(),new ArrayList<Hbgzp>(),isSelfMo);
                                               			}
                                            		}
                                            	}
                                            }
                                    	}
                                    	
                                    }
                            	}
                            }
                    	}
                    	
                    }else{
                    	 List<Hbgzp> handPais = handCardBeanCopy1.getOperateCards();
//                       if (handPais.size() == 0) {
//                           lack.setHu(true);
//                           huList.add(lack);
//                       } else {
                           sortMin(handPais);
                           chaiPaiNew(huList, lackCopy1, handPais, disCard,handCardBeanCopy1.getJianCards(),new ArrayList<Hbgzp>(),isSelfMo);
//                           if (huList.size() > 0) {
//                               return huList;
//                           }
//                       }
//                       return huList;
                    }
        		 }
        		 return huList;
        	}
        	
        }
        List<HbgzpHuLack> huList = new ArrayList<>();
        List<Hbgzp> handPais = handCardBean.getOperateCards();
        if (handPais.size() == 0) {
            lack.setHu(true);
            huList.add(lack);
        } else {
            sortMin(handPais);
            chaiPaiNew(huList, lack, handPais, disCard,handCardBean.getJianCards(),new ArrayList<Hbgzp>(),isSelfMo);
            if (huList.size() > 0) {
                return huList;
            }
        }
        return huList;
    }
    
    private static void shanZhaCards(HbgzpHandCard handCardBean,HbgzpHuLack lack,Entry<Integer, List<Hbgzp>> entry){
    	int huxi = Hbgzp.isHongpai(entry.getKey())?4:2;
		lack.changeHuxi(huxi);
        List<Hbgzp> cards = entry.getValue();
        if(cards.size() ==5){
        	boolean shanjian = false;
			for (Hbgzp gzp:cards) {
				//删掉要做成扎的牌 优先留一个捡的不删
				if(handCardBean.getJianCards().contains(gzp.getId()) && !shanjian){
					shanjian = true;
					continue;
				}
				handCardBean.getOperateCards().remove(gzp);
			}
        }else{
        	//删掉要做成扎的牌
            handCardBean.getOperateCards().removeAll(cards);
        }
        
        lack.addPhzHuCards(HbgzpDisAction.action_angang, toPhzCardIds(cards), huxi,handCardBean.getJianCards());
    }

    
    /**
     * 拆牌  先找出2个擦边得牌
     * @param huList
     * @param lack
     * @param handPais 手牌
     * @param disCard 胡的牌
     * @param jianCards  捡的牌
     * @param nochaiList 拆过没拆出牌型的牌
     */
    public static void chaiPaiNew(List<HbgzpHuLack> huList, HbgzpHuLack lack, List<Hbgzp> handPais, Hbgzp disCard,List<Integer> jianCards,List<Hbgzp> nochaiList,boolean isSelfMo) {
    	
    	if(handPais == null || handPais.size() <= 0){
    		return;
    	}
    	if(handPais.size() == 2){
        	//胡牌必须留2张能擦边的牌
    		Hbgzp gzp = handPais.get(0);
    		Hbgzp gzp1 = handPais.get(1);
    		if(gzp.getVal() == gzp1.getVal()){
    			if(jianCards.contains(gzp.getId()) ||jianCards.contains(gzp1.getId())){
    				return;
    			}
    		}
    		List<int[]> pzs = HbgzpConstants.getPaiZu(gzp.getVal());
    		if(pzs != null && pzs.size() > 0){
    			boolean ishu = false;
    			for (int[] pz : pzs) {
					for (int i = 0; i < pz.length; i++) {
						if(pz[i]==gzp1.getVal()){
							lack.setHu(true);
							ishu = true;
		                    huList.add(lack);
		                    break;
						}
					}
    				if(ishu){
    					break;
    				}
				}
    		}
    		return;
    	}
    	List<Integer> delValList = new ArrayList<>();
    	for (Hbgzp gzp : handPais) {
    		if(delValList.contains(gzp.getVal()) && !jianCards.contains(gzp.getId())){//是捡的再删一次
    			continue;
    		}
    		int val = gzp.getVal();
    		delValList.add(val);
    		
    		List<int[]> pzs = HbgzpConstants.getPaiZu(gzp.getVal());
    		Set<Integer> pzSet = getPzSet(pzs);//得到所有能擦边的牌
    		
    		List<Hbgzp> copyhandPais = new ArrayList<>(handPais);
    		copyhandPais.remove(gzp);
    		for (Integer pzVal:pzSet) {
				for (Hbgzp cpGzp : copyhandPais) {
					if(cpGzp.getVal() != pzVal){
						continue;
					}
					if(cpGzp.getVal() == gzp.getVal() && (jianCards.contains(gzp.getId()) ||jianCards.contains(cpGzp.getId()))){
						continue;
					}
					List<Hbgzp> copy2handPais = new ArrayList<>(copyhandPais);
					copy2handPais.remove(cpGzp);
					HbgzpHuLack newLack = lack.clone();
					List<Integer> kouList = new ArrayList<>();
					kouList.add(cpGzp.getId());
					kouList.add(gzp.getId());
                    newLack.addPhzHuCards(0, kouList, 0,null);
					frontchaiPaiNew(huList, newLack, copy2handPais, disCard,jianCards,nochaiList,isSelfMo);
				}
			}
		}
    }

    private static Set<Integer> getPzSet(List<int[]> pzs){
    	if(pzs == null || pzs.size() <=0){
    		return null;
    	}
    	Set<Integer> pzSet = new HashSet<>();
		for (int[] pz : pzs) {
			for (int i = 0; i < pz.length; i++) {
				pzSet.add(pz[i]);
			}
		}
		return pzSet;
    }
    
    /**
     * 拆牌
     * @param huList
     * @param lack
     * @param handPais 手牌
     * @param disCard 胡的牌
     * @param jianCards  捡的牌
     * @param nochaiList 拆过没拆出牌型的牌
     */
    public static void frontchaiPaiNew(List<HbgzpHuLack> huList, HbgzpHuLack lack, List<Hbgzp> handPais, Hbgzp disCard,List<Integer> jianCards,List<Hbgzp> nochaiList,boolean isSelfMo) {
    	

        if (handPais.size() < 3) {
        	return;
        }
        //拆顺
        Hbgzp card = handPais.get(0);//排过序的那到第一张
        int val = card.getVal();

    	List<int[]> paiZus = HbgzpConstants.getPaiZu(val);//牌组
        List<Hbgzp> handPaisCopy;
        List<Hbgzp> rmList;
        HbgzpHuLack newLack;
        if(paiZus != null && paiZus.size() > 0){
        	for (int i = 0; i < paiZus.size(); i++) {//遍历牌组，坎在最前面
        		int[] paiZu = paiZus.get(i);
            	handPaisCopy = new ArrayList<>(handPais);
                rmList = removeVals(handPaisCopy, paiZu,jianCards,disCard);//手牌里是否有这个牌组删除
                if(rmList == null){//找不到牌组删除  进入下一个  全部都删不掉则表示不能胡
            		continue;
                }else{//成功删除牌组
                	 newLack = lack.clone();
                     if (isSameCard(rmList)) {
                     	if(rmList.size() == 3){//如果是3张一样的  则表示是一坎
                     		// 三张一模一样的
                             if(!isSelfMo && disCard!=null&&rmList.contains(disCard)){
                            	int huxi = Hbgzp.isHongpai(disCard.getVal())?1:0;
                            	newLack.changeHuxi(huxi);
                             	newLack.addPhzPengHuCards(HbgzpDisAction.action_peng, toPhzCardIds(rmList), huxi,jianCards);
                             }else {
                            	 int huxi = Hbgzp.isHongpai(rmList.get(0).getVal())?2:1;
                            	 newLack.changeHuxi(huxi);
                                 newLack.addPhzHuCards(HbgzpDisAction.action_kan, toPhzCardIds(rmList), huxi,jianCards);
                             }
                     	}
                     } else {
                    	 int huxi = getShunHuxi(rmList);//否则是个顺子
                     	 newLack.changeHuxi(huxi);
                         newLack.addPhzHuCards(0, toPhzCardIds(rmList), huxi,jianCards);
                     }
                     if (handPaisCopy.size() == 0){//全都删完了，则表示可以胡
                    	 newLack.setHu(true);
                         huList.add(newLack);
                 		return;
                     } else {//进入下一次循环
                    	 frontchaiPaiNew(huList, newLack, handPaisCopy, disCard,jianCards,nochaiList,isSelfMo);
                     }
                }
               
            }
        }
    }
    
    public static List<Hbgzp> removeVals(List<Hbgzp> cards, int[] vals,List<Integer> jianCards,Hbgzp disCard) {
        List<Hbgzp> rmList = new ArrayList<>(vals.length);
        boolean iskan = false;
        if(vals[0] == vals[1] && vals[0] == vals[2]){
        	iskan = true;
        }
//        boolean bjian = false;
        for (Integer val : vals) {
            boolean hasVal = false;
            for (Hbgzp card : cards) {
                if (card.getVal() == val && !rmList.contains(card)) {
                    if(iskan){
                    	if(jianCards.contains(card.getId())){
                    		if(disCard!= null && card.getId() != disCard.getId()){
                    			continue;
                    		}
//                    		if(bjian){
//                    			continue;
//                    		}
//                    		bjian = true;
                    		rmList.add(card);
                        	hasVal = true;
                        	break;
                    	}else{
                    		rmList.add(card);
                        	hasVal = true;
                        	break;
                    	}
                    }else{
                    	rmList.add(card);
                    	hasVal = true;
                    	break;
                    }
                }
            }
            if (!hasVal) {
                return null;
            }
        }
        if (rmList.size() == vals.length) {
            cards.removeAll(rmList);
            return rmList;
        }
        return null;
    }
    

    
  public static void main(String[] args) {
	  boolean needDui = false;
	  boolean isPaoHu = true;
	  boolean isSelfMo = true;
	  int outCardHuXi = 0;
	  List<Integer> valList;
//	  valList = new ArrayList<>(Arrays.asList(3,3,3,3,6,6,6,6,4,5,8,9));
//	  valList = new ArrayList<>(Arrays.asList(2,3,10,202,203));
//	  valList = new ArrayList<>(Arrays.asList(3,3,3,3,6,6,6,6,7,7,7,7,8,8,8,8,9,9,9,9));
//	  valList = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8,9,3,101,102,201,202,203,6,6,6,6,7));
//	  valList = new ArrayList<>(Arrays.asList(101, 102, 103, 104, 104));
//	  valList = new ArrayList<>(Arrays.asList(1, 1, 1, 4, 4, 4, 7, 7, 7, 10, 9, 101, 102, 3, 106, 106, 106, 201, 202, 203));
//	  valList = new ArrayList<>(Arrays.asList(106,106,106,3,3,3,4,4,5,6,7));
	  valList = new ArrayList<>(Arrays.asList(202,202,202,203,203,1,1,2,3,4,4,5,6,7,8,9,1));
	
	
	  List<Hbgzp> cardList = val2Card(valList);
	  Hbgzp card = cardList.get(cardList.size() - 1);
	  HbgzpHandCard bean = getPaohuziHandCardBean(cardList,null);
	
	  int count = 1000000;
	  HbgzpPlayer player= new HbgzpPlayer();
	  player.setZhaCount(0);
	  List<HbgzpHuLack> hu1 =  isHuNew1(bean, card, player,true);
	  long start = System.currentTimeMillis();
//	  for (int i = 0; i < count; i++) {
//	      hu1 = isHuNew1(bean, card, true);
//	  }
	  if(hu1 != null && hu1.size() > 0){
		for (HbgzpHuLack lack : hu1) {
			  System.out.println((lack.isHu() ? "胡啦" : "不胡") + "|" + JSON.toJSONString(lack));
		}
	  }else{
		  System.out.println("胡不了");
	  }
 

	//  bean = getPaohuziHandCardBean(cardList);
	//  card = null;
	//  PaohuziHuLack hu2 = null;
	//  start = System.currentTimeMillis();
	//  for (int i = 0; i < count; i++) {
	//      hu2 = isHuNew(bean, card, isSelfMo, outCardHuXi, needDui, isPaoHu);
	//  }
	//  System.out.println("count = " + count + " timeUse = " + (System.currentTimeMillis() - start) + " ms");
	//  System.out.println((hu2.isHu() ? "胡啦" : "不胡") + "|" + JSON.toJSONString(hu2));
	}

  public static List<Hbgzp> val2Card(List<Integer> valList) {
      List<Hbgzp> allCard = new ArrayList<>(toPhzCards(HbgzpConstants.cardList));
      List<Hbgzp> cardList = new ArrayList<>();
      for (Integer val : valList) {
          for (Hbgzp card : allCard) {
              if (card.getVal() == val && !cardList.contains(card)) {
                  cardList.add(card);
                  break;
              }
          }
      }
      return cardList;
  }
}
