package com.sy599.game.qipai.penghuzi.tool;

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

import org.apache.commons.lang.StringUtils;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.penghuzi.bean.PenghuziHandCard;
import com.sy599.game.qipai.penghuzi.bean.PenghuziHuBean;
import com.sy599.game.qipai.penghuzi.bean.PenghuziPlayer;
import com.sy599.game.qipai.penghuzi.bean.PenghuziTable;
import com.sy599.game.qipai.penghuzi.bean.PenghzDisAction;
import com.sy599.game.qipai.penghuzi.constant.PengHZMingTang;
import com.sy599.game.qipai.penghuzi.constant.PenghuziConstant;
import com.sy599.game.qipai.penghuzi.constant.PenghzCard;
import com.sy599.game.qipai.penghuzi.rule.PenghuziIndex;
import com.sy599.game.qipai.penghuzi.rule.PenghzCardIndexArr;

/**
 * 跑胡子
 *
 * @author lc
 */
public class PenghuziTool {
    /**
     * 发牌
     *
     * @param copy
     * @param t
     * @return
     */
    public static List<Integer> c2710List = Arrays.asList(2, 7, 10);

    public static synchronized List<List<PenghzCard>> fapai(List<Integer> copy, List<List<Integer>> t,int playerCount) {
    	
        List<List<PenghzCard>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<PenghzCard> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(find(copy, zp));
                testcount += zp.size();
            }
            if (list.size() == 3) {
                list.add(toPhzCards(copy));
            }
        }else {

            List<Integer> copy2 = new ArrayList<>(copy);
            
            int fapaiCount = 14 * playerCount + 1 - testcount;
            if (pai.size() >= 15) {
                list.add(pai);
                pai = new ArrayList<>();
            }

            boolean test = false;
            if (list.size() > 0) {
                test = true;
            }

            for (int i = 0; i < fapaiCount; i++) {
                // 发牌张数=21*4+1 正好第一个发牌的人14张其他人13张
                PenghzCard majiang = PenghzCard.getPaohzCard(copy.get(i));
                copy2.remove((Object) copy.get(i));
                if (test) {
                    if (i < j * 14) {
                        pai.add(majiang);
                    } else {
                        list.add(pai);
                        pai = new ArrayList<>();
                        pai.add(majiang);
                        j++;
                    }
                } else {
                    if (i <= j * 14) {
                        pai.add(majiang);
                    } else {
                        list.add(pai);
                        pai = new ArrayList<>();
                        pai.add(majiang);
                        j++;
                    }
                }

            }
            list.add(pai);
            List<PenghzCard> left = new ArrayList<>();
            for (int i = 0; i < copy2.size(); i++) {
                left.add(PenghzCard.getPaohzCard(copy2.get((i))));
            }
            list.add(left);
        }
        if(allReFaPai(list,playerCount)){
            return list;
        }else {
            return fapai(copy,null,playerCount);
        }
    }
    
	public static List<PenghzCard>  getTingZps(List<PenghzCard> handPais,PenghuziPlayer player) {
		PenghuziTable table = player.getPlayingTable(PenghuziTable.class);
		List<PenghzCard>  huCards = new ArrayList<PenghzCard> ();
		if(table==null){
			return huCards;
		}
		
		List<Integer> copy = new ArrayList<>(PenghuziConstant.cardList);
	
		for(Integer id : copy) {
			PenghzCard card =  PenghzCard.getPaohzCard(id);
			if(!checkHuCard(huCards, card)) {
				continue;
			}
			if(handPais.contains(card)){
				continue;
			}
			PenghuziHuLack lack = player.checkHuCard2(card, true,handPais,true);
			handPais.remove(card);
			if(lack.isHu()){
				huCards.add(card);
			}else {
				lack =player.checkPaoHu2(card, true, true, handPais,true);
				if(lack.isHu()){
					huCards.add(card);
				}
			}
		}
		
	//	
		return huCards;
	}
	
	
	private static boolean checkHuCard(List<PenghzCard>  huCards,PenghzCard card){
		if(huCards.size() ==0) {
			return true;
		}
		for(PenghzCard huC : huCards) {
			if(huC.getVal() == card.getVal()) {
				return false;
			}
		}
		return true;
	}
	

    public static boolean allReFaPai(List<List<PenghzCard>> list,int playerCount){
        if(!checkShuangLong(list,playerCount))
            return false;
        if(!checkTianHu(list))
            return false;
        return true;
    }

    public static synchronized boolean checkShuangLong(List<List<PenghzCard>> list,int playerCount){
        for (int i = 0; i < playerCount; i++) {
            List<PenghzCard> cards = list.get(i);
            int []nums=new int[20];
            for (PenghzCard card:cards) {
                int k;
                int val = card.getVal();
                if(val<100){
                    k=val-1;
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

    public static synchronized boolean checkTianHu(List<List<PenghzCard>> list){
//        List<PenghzCard> banker = list.get(0);
        for(List<PenghzCard> cardsh: list){
        	  PenghuziHuLack hu = isHuNew(PenghuziTool.getPaohuziHandCardBean(cardsh), null, false, 0, false, false,true);
              if(hu.isHu())
                  return false;
        }
        return true;
    }

    /**
     * 检查麻将是否有重复
     *
     * @param majiangs
     * @return
     */
    public static boolean isPaohuziRepeat(List<PenghzCard> majiangs) {
        if (majiangs == null) {
            return false;
        }

        Map<Integer, Integer> map = new HashMap<>();
        for (PenghzCard mj : majiangs) {
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
     * 找出红牌
     *
     * @param copy
     * @return
     */
    public static List<PenghzCard> findRedPhzs(List<PenghzCard> copy) {
        List<PenghzCard> find = new ArrayList<>();
        for (PenghzCard card : copy) {
            if (c2710List.contains(card.getPai())) {
                find.add(card);
            }
        }
        return find;
    }

    /**
     * 找出小牌
     *
     * @param copy
     * @return
     */
    public static List<PenghzCard> findSmallPhzs(List<PenghzCard> copy) {
        List<PenghzCard> find = new ArrayList<>();
        for (PenghzCard card : copy) {
            if (!card.isBig()) {
                find.add(card);
            }
        }
        return find;
    }

    /**
     * 找出相同的值
     *
     * @param copy
     * @param val
     * @return
     */
    public static List<PenghzCard> findPhzByVal(List<PenghzCard> copy, int val) {
        List<PenghzCard> list = new ArrayList<>();
        for (PenghzCard phz : copy) {
            if (phz.getVal() == val) {
                list.add(phz);
            }
        }
        return list;
    }

    private static List<PenghzCard> find(List<Integer> copy, List<Integer> valList) {
        List<PenghzCard> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    PenghzCard phz = PenghzCard.getPaohzCard(card);
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
    public static Map<Integer, Integer> toPhzValMap(List<PenghzCard> phzs) {
        Map<Integer, Integer> ids = new HashMap<>();
        if (phzs == null) {
            return ids;
        }
        for (PenghzCard phz : phzs) {

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
    public static List<Integer> toPhzRepeatVals(List<PenghzCard> phzs) {
        List<Integer> ids = new ArrayList<>();
        if (phzs == null) {
            return ids;
        }
        for (PenghzCard phz : phzs) {
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
    public static List<PenghzCard> toPhzCards(List<Integer> phzIds) {
        List<PenghzCard> cards = new ArrayList<>();
        for (int id : phzIds) {
            cards.add(PenghzCard.getPaohzCard(id));
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
    
    
    public static boolean isDuiZi(List<Integer> handPs){
    	HashSet<Integer> set = new HashSet<Integer>();
    	for(Integer id: handPs){
    		PenghzCard pc = 	PenghzCard.getPaohzCard(id);
    		set.add(pc.getVal());
    	}
    	if(set.size()==1){
    		return true;
    	}
    	
    	return false;
    }

    
    
    public static int getHandPaiCo(List<Integer> handPs){
    	HashSet<Integer> set = new HashSet<Integer>();
    	for(Integer id: handPs){
    		PenghzCard pc = 	PenghzCard.getPaohzCard(id);
    		set.add(pc.getVal());
    	}
    	
    	return set.size();
    }
    

    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toPhzCardIds(List<PenghzCard> phzs) {
        if (phzs == null) {
            return Collections.emptyList();
        }

        List<Integer> ids = new ArrayList<>(phzs.size());
        for (PenghzCard phz : phzs) {
            ids.add(phz.getId());
        }
        return ids;
    }

    /**
     * 跑胡子转化为牌s
     */
    public static List<Integer> toPhzCardVals(List<PenghzCard> phzs, boolean matchCase) {
        List<Integer> majiangIds = new ArrayList<>();
        if (phzs == null) {
            return majiangIds;
        }
        for (PenghzCard card : phzs) {
            if (matchCase) {
                majiangIds.add(card.getVal());
            } else {
                majiangIds.add(card.getPai());
            }
        }

        return majiangIds;
    }

    /**
     * 得到最大相同数
     */
    public static PenghzCardIndexArr getMax(List<PenghzCard> list) {
        PenghzCardIndexArr card_index = new PenghzCardIndexArr();
        Map<Integer, List<PenghzCard>> phzMap = new HashMap<>();
        for (PenghzCard phzCard : list) {
            List<PenghzCard> count;
            if (phzMap.containsKey(phzCard.getVal())) {
                count = phzMap.get(phzCard.getVal());
            } else {
                count = new ArrayList<>();
                phzMap.put(phzCard.getVal(), count);
            }
            count.add(phzCard);
        }
        for (int phzVal : phzMap.keySet()) {
            List<PenghzCard> phzList = phzMap.get(phzVal);
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
            }
        }
        return card_index;
    }

    /**
     * 是否能吃
     *
     * @param handCards
     * @param disCard
     * @return
     */
    public static List<PenghzCard> checkChi(List<PenghzCard> handCards, PenghzCard disCard) {
        int disVal = disCard.getVal();
        int otherVal = disCard.getOtherVal();

        List<Integer> chi0 = new ArrayList<>(Arrays.asList(disVal, otherVal));
        List<Integer> chi4 = new ArrayList<>(Arrays.asList(otherVal, otherVal));
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disVal - 2, disVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disVal - 1, disVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disVal + 1, disVal + 2));
        List<List<Integer>> chiList = new ArrayList<>();
        chiList.add(chi0);
        chiList.add(chi4);
        chiList.add(chi1);
        chiList.add(chi2);
        chiList.add(chi3);

        // // 不区分大小写 找到值
        // List<PenghzCard> vals = findCountByVal(handCards, disCard, false);
        // if (vals != null && vals.size() >= 2) {
        // int needCards = 3;
        // if (!vals.contains(disCard)) {
        // // 只能是3个牌
        // vals.add(disCard);
        // }
        //
        // if (vals.size() == needCards) {
        // if (isSameCard(vals)) {
        // // 不能是一样的牌
        // return new ArrayList<PenghzCard>();
        // }
        // return vals;
        //
        // } else if (vals.size() > needCards) {
        // List<PenghzCard> list = new ArrayList<>();
        // if (needCards == 3) {
        // list.add(disCard);
        // needCards = 2;
        // }
        // int k = 0;
        // int s = 1;
        // for (int i = 0; i < vals.size(); i++) {
        // PenghzCard card = vals.get(i);
        // if (!list.contains(card)) {
        // if (card.getId() == disCard.getId()) {
        // if (s >= 2) {
        // continue;
        // } else {
        // s++;
        //
        // }
        // }
        //
        // k++;
        // list.add(card);
        //
        // }
        //
        // if (k >= needCards) {
        // break;
        // }
        // }
        // if (isSameCard(list)) {
        // // 不能是一样的牌
        // return new ArrayList<PenghzCard>();
        // }
        // return list;
        // }
        // return new ArrayList<PenghzCard>();
        // }
        // 检查2 7 10

        List<Integer> val2710 = Arrays.asList(2, 7, 10);
        if (val2710.contains(disCard.getPai())) {
            List<Integer> chi2710 = new ArrayList<>();
            for (int val : val2710) {
                if (disCard.getPai() == val) {
                    continue;
                }
                chi2710.add(disCard.getCase() + val);

            }

            chiList.add(chi2710);

        }

        List<PenghzCard> copy = new ArrayList<>(handCards);
        copy.remove(disCard);

        for (List<Integer> chi : chiList) {
            List<PenghzCard> findList = findPhzCards(copy, chi);
            if (!findList.isEmpty()) {
                return findList;
            }

        }

        // List<Integer> ids = toPhzCardVals(copy, true);
        // for (List<Integer> chi : chiList) {
        // for (int chiVal : chi) {
        // for (int id : ids) {
        // if (chiVal == id) {
        //
        // }
        // }
        // }
        // if (ids.containsAll(chi)) {
        // return findByVals(handCards, chi);
        // }
        // }

        return new ArrayList<PenghzCard>();
    }

    public static List<PenghzCard> findPhzCards(List<PenghzCard> cards, List<Integer> vals) {
        List<PenghzCard> findList = new ArrayList<>();
        for (int chiVal : vals) {
            boolean find = false;
            for (PenghzCard card : cards) {
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
    public static List<PenghzCard> getSameCards(List<PenghzCard> handCards, PenghzCard disCard) {
        List<PenghzCard> list = findCountByVal(handCards, disCard, true);
        if (list != null) {
            return list;
        }
        return null;

    }

    /**
     * 是否一样的牌
     */
    public static boolean isSameCard(List<PenghzCard> handCards) {
        int val = 0;
        for (PenghzCard card : handCards) {
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
    public static void removePhzByVal(List<PenghzCard> handCards, int cardVal) {
        Iterator<PenghzCard> iterator = handCards.iterator();
        while (iterator.hasNext()) {
            PenghzCard paohzCard = iterator.next();
            if (paohzCard.getVal() == cardVal) {
                iterator.remove();
            }

        }
    }

    /**
     * 是否有这张相同的牌
     */
    public static boolean isHasCardVal(List<PenghzCard> handCards, int cardVal) {
        for (PenghzCard card : handCards) {
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
    public static List<PenghzCard> findCountByVal(List<PenghzCard> phzList, Object o, boolean matchCase) {
        int val;
        if (o instanceof PenghzCard) {
            if (matchCase) {
                val = ((PenghzCard) o).getVal();

            } else {
                val = ((PenghzCard) o).getPai();

            }
        } else if (o instanceof Integer) {
            val = (int) o;
        } else {
            return null;
        }
        List<PenghzCard> result = new ArrayList<>();
        for (PenghzCard card : phzList) {
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

    public static List<PenghzCard> findByVals(List<PenghzCard> majiangs, List<Integer> vals) {
        List<PenghzCard> result = new ArrayList<>();
        for (int val : vals) {
            for (PenghzCard majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }

        return result;
    }

    public static PenghuziHuBean checkHu(List<PenghzCard> handCard) {
        if (handCard == null || handCard.isEmpty()) {
            return null;
        }
        PenghuziHuBean bean = new PenghuziHuBean();

        List<PenghzCard> copy = new ArrayList<>(handCard);
        bean.setHandCards(new ArrayList<>(copy));

        PenghzCardIndexArr valArr = PenghuziTool.getMax(copy);
        bean.setValArr(valArr);

        // 去掉3张和4张一样的牌
        PenghuziIndex index3 = valArr.getPaohzCardIndex(3);
        if (index3 != null) {
            copy.removeAll(index3.getPaohzList());
        }
        PenghuziIndex index2 = valArr.getPaohzCardIndex(2);
        if (index2 != null) {
            copy.removeAll(index2.getPaohzList());
        }
        bean.setOperateCards(copy);

        return bean;
    }

    // public static void chaiPai(PenghuziHuBean bean) {
    // 不管大2小2先找出来
    // List<PenghzCard> find2 = findCountByVal(bean.getOperateCards(), 2,
    // false);
    // for (PenghzCard card2 : find2) {
    // 是否有123 或者2710可以配对

    // }

    // }

    /**
     * 得到某个值的麻将
     *
     * @param copy
     * @return
     */
    public static List<PenghzCard> getVals(List<PenghzCard> copy, int val, PenghzCard... exceptCards) {
        List<PenghzCard> list = new ArrayList<>();
        Iterator<PenghzCard> iterator = copy.iterator();
        while (iterator.hasNext()) {
            PenghzCard phz = iterator.next();
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (PenghzCard except : exceptCards) {
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
    public static PenghzCard getVal(List<PenghzCard> copy, int val, PenghzCard... exceptCards) {
        for (PenghzCard phz : copy) {
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (PenghzCard except : exceptCards) {
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

    public static void sortMin(List<PenghzCard> hasPais) {
        Collections.sort(hasPais, new Comparator<PenghzCard>() {
            @Override
            public int compare(PenghzCard o1, PenghzCard o2) {
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

    // 拆牌
    public static boolean chaipai(PenghuziHuLack lack, List<PenghzCard> hasPais) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais);
        if (hu)
            return true;
        return false;
    }

    public static boolean chaiSame(PenghuziHuLack lack, List<PenghzCard> hasPais, PenghzCard minCard, List<PenghzCard> sameList) {
        if (sameList.size() < 3) {
            // 小于3张牌没法拆
            return false;
        } else if (sameList.size() == 3) {
            // 大小加一起正好3张牌
            boolean chaisame = chaishun3(lack, hasPais, minCard, false, false, sameList.get(0).getVal(), sameList.get(1).getVal(), sameList.get(2).getVal());
            if (!chaisame) {
                return false;
            } else {
                return true;
            }
        } else if (sameList.size() > 3) {
            int minVal = minCard.getVal();
            List<List<Integer>> modelList = getSameModel(minVal);
            for (List<Integer> model : modelList) {
                PenghuziHuLack copyLack = lack.clone();
                List<PenghzCard> copyHasPais = new ArrayList<>(hasPais);
                boolean chaisame = chaishun3(copyLack, copyHasPais, minCard, false, false, model.get(0), model.get(1), minVal);
                if (chaisame) {
                    lack.copy(copyLack);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 拆坎子
     *
     * @param hasPais canoperateHandPais
     */
    private static boolean chaikan(PenghuziHuLack lack, List<PenghzCard> hasPais, PenghzCard minCard) {
        List<PenghzCard> sameValList = findCountByVal(hasPais, minCard, true);
        if (sameValList.size() == 3) {
            boolean chaiSame = chaishun3(lack, hasPais, minCard, false, false, sameValList.get(0).getVal(), sameValList.get(1).getVal(), sameValList.get(2).getVal());
            if (chaiSame) {
                return true;
            }
        }
        return false;
    }

    private static boolean chaiSame0(PenghuziHuLack lack, List<PenghzCard> hasPais, PenghzCard minCard) {
        List<PenghzCard> sameList = findCountByVal(hasPais, minCard, false);
        if (sameList.size() < 3) {
            return false;
        }

        // 拆相同
        PenghuziHuLack copyLack = lack.clone();
        List<PenghzCard> copyHasPais = new ArrayList<>(hasPais);
        if (chaikan(copyLack, copyHasPais, minCard)) {
            lack.copy(copyLack);
            return true;
        }

        // 拆相同2
        boolean chaiSame = chaiSame(copyLack, copyHasPais, minCard, sameList);
        if (chaiSame) {
            lack.copy(copyLack);
            return true;
        }
        return false;
    }

    /**
     * 拆顺
     */
    public static boolean chaishun(PenghuziHuLack lack, List<PenghzCard> hasPais) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        PenghzCard minCard = hasPais.get(0);
        int minVal = minCard.getVal();
        int minPai = minCard.getPai();
        boolean isTryChaiSame = false;
        PenghuziHuLack copyLack = null;
        List<PenghzCard> copyHasPais = null;
        if (minPai != 1 && minPai != 2) {
            // 如果不是1 和2 尝试拆相同
            isTryChaiSame = true;
            boolean isHu = chaiSame0(lack, hasPais, minCard);
            if (isHu) {
                return true;
            }
        } else {
            copyLack = lack.clone();
            copyHasPais = new ArrayList<>(hasPais);
        }

        // 拆顺子
        int pai1 = minVal;
        int pai2 = 0;
        int pai3 = 0;
        if (pai1 % 100 == 10) {
            pai1 = pai1 - 2;

        } else if (pai1 % 100 == 9) {
            pai1 = pai1 - 1;
        }
        pai2 = pai1 + 1;
        pai3 = pai2 + 1;
        boolean check2710 = false;
        boolean chaishun = false;
        if (!lack.isHasFail2710Val(minVal) && minCard.getPai() == 2) {
            // 2 7 10
            int pai7 = minCard.getCase() + 7;
            int pai10 = minCard.getCase() + 10;
            check2710 = true;
            PenghuziHuLack copyLack2 = lack.clone();
            List<PenghzCard> copyHasPais2 = new ArrayList<>(hasPais);
            chaishun = chaiShun(copyLack2, copyHasPais2, minCard, true, check2710, pai1, pai7, pai10);
            if (chaishun) {
                lack.copy(copyLack2);
            } else {
                // 拆2 7 10组合失败
                chaishun = chaiShun(lack, hasPais, minCard, true, false, pai1, pai2, pai3);
            }
        } else {
            // 拆顺
            chaishun = chaiShun(lack, hasPais, minCard, true, check2710, pai1, pai2, pai3);
        }

        if (!chaishun && !isTryChaiSame) {
            chaishun = chaiSame0(copyLack, copyHasPais, minCard);
            if (chaishun) {
                lack.copy(copyLack);

            }
        }
        return chaishun;

    }

    public static boolean chaiShun(PenghuziHuLack lack, List<PenghzCard> hasPais, PenghzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3) {
        // 拆顺
        boolean chaishun = chaishun3(lack, hasPais, minCard, true, check2710, pai1, pai2, pai3);
        if (!chaishun) {
            if (check2710) {
                return chaishun(lack, hasPais);
            }
            // 拆同牌
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    private static List<List<Integer>> getSameModel(int val) {
        List<List<Integer>> result = new ArrayList<>();
        int smallpai = val % 100;
        int bigpai = 100 + smallpai;
        List<Integer> model1 = Arrays.asList(bigpai, smallpai);
        List<Integer> model2 = Arrays.asList(bigpai, bigpai);
        List<Integer> model3 = Arrays.asList(smallpai, smallpai);

        result.add(model2);
        result.add(model3);
        result.add(model1);
        return result;
    }

    private static boolean chaishun3(PenghuziHuLack lack, List<PenghzCard> hasPais, PenghzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3) {
        int minVal = minCard.getVal();

        List<Integer> lackList = new ArrayList<>();
        PenghzCard num1 = getVal(hasPais, pai1);
        PenghzCard num2 = getVal(hasPais, pai2, num1);
        PenghzCard num3 = getVal(hasPais, pai3, num1, num2);

        // 找到一句话的
        List<PenghzCard> hasMajiangList = new ArrayList<>();
        if (num1 != null) {
            hasMajiangList.add(num1);
        }
        if (num2 != null) {
            hasMajiangList.add(num2);
        }
        if (num3 != null) {
            hasMajiangList.add(num3);
        }

        // 一句话缺少的
        if (num1 == null) {
            lackList.add(pai1);
        }
        if (num2 == null) {
            lackList.add(pai2);
        }
        if (num3 == null) {
            lackList.add(pai3);
        }

        int lackNum = lackList.size();
        if (lackNum > 0) {
            // 看看三张牌是否相同
            if (lack.getHongzhongNum() <= 0) {
                if (check2710) {
                    lack.addFail2710Val(minVal);
                    return chaipai(lack, hasPais);
                }

                // 检查是不是对子
                // if (lack.isNeedDui()) {
                // List<PenghzCard> count = getVal(hasPais,
                // hasMajiangList.get(0).getVal());
                // if (count.size() == 2) {
                // lack.setNeedDui(false);
                // hasPais.removeAll(count);
                // return chaipai(lack, hasPais);
                // }
                // }

                return false;
            }

            // 做成一句话缺少2张以上的，没有将优先做将
            if (lackNum >= 2) {
                // 补坎子
                List<PenghzCard> count = getVals(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2) {
                    if (lack.isNeedDui()) {
                        // 没有将做将
                        lack.setNeedDui(false);
                        hasPais.removeAll(count);
                        return chaipai(lack, hasPais);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais);
                }

                // 做将
                if (lack.isNeedDui() && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setNeedDui(false);
                    hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais);
                }
            } else if (lackNum == 1) {
                // 做将
                if (lack.isNeedDui() && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setNeedDui(false);
                    hasPais.remove(minCard);
                    lack.addLack(minCard.getVal());
                    return chaipai(lack, hasPais);
                }

                List<PenghzCard> count = getVals(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<PenghzCard> count1 = getVals(hasPais, hasMajiangList.get(0).getVal());
                List<PenghzCard> count2 = getVals(hasPais, hasMajiangList.get(1).getVal());
                List<PenghzCard> count3 = getVals(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && count2.size() == 1 && count3.size() == 1) {
                    hasPais.removeAll(count1);
                    lack.changeHongzhong(-1);
                    lack.addLack(hasMajiangList.get(0).getVal());
                    chaipai(lack, hasPais);
                }
            }
            int huxi = 0;
            int action = PenghzDisAction.action_chi;
            if (isShun) {
                int minPai = minCard.getPai();
                if (minPai == 1 || minPai == 2) {
                    // 123 和 2710 加胡息
                    huxi = getShunHuxi(hasMajiangList);
                }
            } else {
                if (isSameCard(hasMajiangList)) {
                    // 如果是三个一模一样的
                    action = PenghzDisAction.action_peng;
                    if (lack.isSelfMo()) {
                        action = PenghzDisAction.action_zai;
                    }
                   // huxi = getOutCardHuxi(action, hasMajiangList);
                }
            }

            lack.addPhzHuCards(action, toPhzCardIds(hasMajiangList), huxi);
            lack.changeHuxi(huxi);
            hasPais.removeAll(hasMajiangList);
            return chaipai(lack, hasPais);
        }
        return chaipai(lack, hasPais);
    }

    /**
     * 去除重复后得到val的集合
     *
     * @param cards
     * @return
     */
    public static Map<Integer, Integer> getDistinctVal(List<PenghzCard> cards) {
        Map<Integer, Integer> valIds = new HashMap<>();
        if (cards == null) {
            return valIds;
        }
        for (PenghzCard phz : cards) {
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
     *
     * @param action
     * @param cards
     * @return
     */
    public static int getOutCardHuxi(int action, List<PenghzCard> cards) {
        int huxi = 0;
        if (action == 0) {
            return huxi;
        }
        if (action == PenghzDisAction.action_ti) {
            // 大的12分 小的9分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 4) {
                    continue;

                }
                if (entry.getKey() > 100) {
                    // 大的
                    huxi += 12;
                } else {
                    huxi += 9;
                }
            }

        } else if (action == PenghzDisAction.action_pao) {
            // 大的9分 小的6分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 4) {
                    continue;
                }
                if (entry.getKey() > 100) {
                    // 大的
                    huxi += 9;
                } else {
                    huxi += 6;
                }

            }
        } else if (action == PenghzDisAction.action_chi) {
            // 吃 只有123 和2710 大的6分 小的3分
            List<PenghzCard> copy = new ArrayList<>(cards);
            sortMin(copy);
            huxi = getShunHuxi(copy);
        } else if (action == PenghzDisAction.action_peng) {
            // 大的3分 小的1分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 3) {
                    continue;
                }
                if (entry.getKey() > 100) {
                    // 大的
                    huxi += 3;
                } else {
                    huxi += 1;
                }
            }

        } else if (action == PenghzDisAction.action_zai || action == PenghzDisAction.action_chouzai) {
            // 大的6分 小的3分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 3) {
                    continue;
                }
                if (entry.getKey() > 100) {
                    // 大的
                    huxi += 6;
                } else {
                    huxi += 3;
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
    private static int getShunHuxi(List<PenghzCard> hasMajiangList) {
        if (hasMajiangList.size() < 3) {
            return 0;
        }
        if (hasMajiangList.get(0).getPai() == hasMajiangList.get(1).getPai()) {
            //不是顺子
            return 0;
        }
        PenghzCard minCard = hasMajiangList.get(0);
        if (minCard.getPai() == 1) {
            if (minCard.isBig()) {
                return 6;
            } else {
                return 3;
            }
        } else if (minCard.getPai() == 2) {
            for (PenghzCard card : hasMajiangList) {
                if (!c2710List.contains(card.getPai())) {
                    return 0;
                }
            }
            if (minCard.isBig()) {
                return 6;
            } else {
                return 3;
            }
        }
        return 0;
    }

    /**
     * 是否胡牌
     */
    public static PenghuziHuLack isHu(PenghuziHandCard handCardBean, PenghzCard disCard, boolean isSelfMo, int outCardHuxi, boolean needDui, boolean isPaoHu) {
        PenghuziHuLack lack = new PenghuziHuLack(0);
        // lack.changeHuxi(outCardHuxi);
        lack.setSelfMo(isSelfMo);
        lack.setCheckCard(disCard);
        // int count = handCardBean.getHandCards().size();
        // if (count % 3 != 0) {
        lack.setNeedDui(needDui);
        // }

        PenghzCardIndexArr arr = handCardBean.getIndexArr();
        // 手上有3个的
        PenghuziIndex index2 = arr.getPaohzCardIndex(2);
        if (index2 != null) {
            List<Integer> list = index2.getValList();
            for (int val : list) {
                if (disCard != null && val == disCard.getVal()) {
                    // 抓到手上的牌可以不用强制组出3个
                    handCardBean.getOperateCards().addAll(index2.getPaohzValMap().get(val));
                    continue;
                }
                int huxi = 0;
                if (val > 100) {
                    // 大的6分
                    huxi = 6;
                } else {
                    // 小的3分
                    huxi = 3;
                }
                lack.changeHuxi(huxi);
                List<PenghzCard> cards = index2.getPaohzValMap().get(val);
//				lack.addPhzHuCards(PenghzDisAction.action_zai, toPhzCardIds(cards), huxi);
                lack.addPhzHuCards(PenghzDisAction.action_kan, toPhzCardIds(cards), huxi);
            }
        }

        PenghuziIndex index3 = arr.getPaohzCardIndex(3);
        if (index3 != null) {
            List<Integer> list = index3.getValList();
            for (int val : list) {
                int huxi = 0;
                boolean paoHu = false;
                int action = PenghzDisAction.action_pao;
                if (disCard != null && val == disCard.getVal()) {
                    if (!isPaoHu) {
                        action = PenghzDisAction.action_kan;
                        if (val > 100) {
                            // 坎大的6分
                            huxi = 6;
                        } else {
                            // 坎小的3分
                            huxi = 3;
                        }
                        handCardBean.getOperateCards().add(disCard);
                    } else if (isSelfMo) {
                        if (val > 100) {
                            // 大的12分
                            huxi = 12;
                        } else {
                            // 小的3分
                            huxi = 9;
                        }
                        action = PenghzDisAction.action_ti;
                    } else {
                        action = PenghzDisAction.action_pao;
                        paoHu = true;
                        if (val > 100) {
                            // 碰大的6分
                            huxi = 9;
                        } else {
                            // 小的3分
                            huxi = 6;
                        }
                        List<PenghzCard> cards = index3.getPaohzValMap().get(val);
                        // cards.remove(disCard);

                        lack.changeHuxi(huxi);
                        lack.addPhzHuCards(action, toPhzCardIds(cards), huxi);
                        // handCardBean.getOperateCards().add(disCard);
                    }
                } else {
                    //直接手上有4张
                    action = PenghzDisAction.action_ti;
                    if (val > 100) {
                        // 大的6分
                        huxi = 12;
                    } else {
                        // 小的3分
                        huxi = 9;
                    }
                    lack.setNeedDui(true);
                }

                if (!paoHu) {
                    lack.changeHuxi(huxi);
                    List<PenghzCard> cards = index3.getPaohzValMap().get(val);
                    if (!isPaoHu) {
                        cards.remove(disCard);
                    }
                    lack.addPhzHuCards(action, toPhzCardIds(cards), huxi);
                }

            }
        }
        // 需要对子才能胡牌
        if (lack.isNeedDui()) {
            List<List<PenghzCard>> duiziList = arr.getDuizis();
            List<PenghuziHuLack> lackList = new ArrayList<>();

            for (List<PenghzCard> list : duiziList) {
                List<PenghzCard> copy = new ArrayList<>(handCardBean.getOperateCards());
                // List<PenghzCard> list = valEntry.getValue();
                if (!copy.containsAll(list)) {
                    continue;
                }
                int i = 0;
                List<Integer> duizi = new ArrayList<>();
                for (PenghzCard phz : list) {
                    i++;
                    duizi.add(phz.getId());
                    copy.remove(phz);
                    if (i >= 2) {
                        break;
                    }
                }
                PenghuziHuLack lackCopy = new PenghuziHuLack(0);
                lackCopy.setSelfMo(isSelfMo);
                lackCopy.setCheckCard(disCard);
                lackCopy.changeHuxi(lack.getHuxi());
                if (lack.getPhzHuCards() != null) {
                    lackCopy.setPhzHuCards(new ArrayList<>(lack.getPhzHuCards()));

                }
                lackCopy.addPhzHuCards(0, duizi, 0);
                boolean hu = chaipai(lackCopy, copy);
                if (hu) {
                    lackCopy.setHu(hu);
                    lackList.add(lackCopy);
                }

            }
            if (!lackList.isEmpty()) {
                int maxHuxi = 0;
                PenghuziHuLack maxHuxiLack = null;
                for (PenghuziHuLack copy : lackList) {
                    if (maxHuxi == 0 || copy.getHuxi() > maxHuxi) {
                        maxHuxi = copy.getHuxi();
                        maxHuxiLack = copy;
                    }
                }
                return maxHuxiLack;

            }

        } else {
            boolean hu = chaipai(lack, handCardBean.getOperateCards());
            lack.setHu(hu);
        }
        return lack;
    }

    /**
     * 得到可以操作的牌 4张和3张是不能操作的
     */
    public static PenghuziHandCard getPaohuziHandCardBean(List<PenghzCard> handPais) {
        PenghuziHandCard card = new PenghuziHandCard();
        List<PenghzCard> copy = new ArrayList<>(handPais);
        card.setHandCards(new ArrayList<>(copy));

        PenghzCardIndexArr valArr = PenghuziTool.getMax(copy);
        card.setIndexArr(valArr);
        // 去掉4张和3张
        PenghuziIndex index3 = valArr.getPaohzCardIndex(3);
        if (index3 != null) {
            copy.removeAll(index3.getPaohzList());
        }
        PenghuziIndex index2 = valArr.getPaohzCardIndex(2);
        if (index2 != null) {
            copy.removeAll(index2.getPaohzList());
        }
        card.setOperateCards(copy);
        return card;
    }

    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @return String
     */
    public static List<PenghzCard> explodePhz(String str, String delimiter) {
        List<PenghzCard> list = new ArrayList<>();
        if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
            return list;
        String strArray[] = str.split(delimiter);

        for (String val : strArray) {
            PenghzCard phz = null;
            if (val.startsWith("mj")) {
                phz = PenghzCard.valueOf(PenghzCard.class, val);
            } else {
                phz = PenghzCard.getPaohzCard((Integer.valueOf(val)));
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
    public static String implodePhz(List<PenghzCard> array, String delimiter) {
        if (array == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        for (PenghzCard i : array) {
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
    public static PenghzCard autoDisCard(List<PenghzCard> handPais) {
        List<PenghzCard> copy = new ArrayList<>(handPais);
        PenghzCardIndexArr valArr = PenghuziTool.getMax(copy);
        PenghuziIndex index1 = valArr.getPaohzCardIndex(1);
        PenghuziIndex index0 = valArr.getPaohzCardIndex(0);
        //List<Integer> list2 = valArr.getPaohzCardIndex(1).getValList();
        //List<Integer> list1 = valArr.getPaohzCardIndex(0).getValList();
        int val = 0;
        if (index0 != null && !index0.getValList().isEmpty()) {
            //val = new Random().nextInt(index0.getValList().size());
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
            PenghzCard card = null;
            for (PenghzCard paohzCard : handPais) {
                if (paohzCard.getVal() == val) {
                    card = paohzCard;
                    break;
                }
            }
            return card;
        }
    }

    public static void main(String[] args) {
        int count=0;
        int count3=0;
        List<Integer> copy = new ArrayList<>(PenghuziConstant.cardList);
        for (int i = 0; i < 100000; i++) {
            int count1=0;
            List<List<PenghzCard>> list = fapai(copy, null, 2);
            for (int j = 0; j < 2; j++) {
                List<PenghzCard> cards = list.get(j);
                int nums[]=new int[20];
                for (PenghzCard card:cards) {
                    int val = card.getVal();
                    if(val<100){
                        nums[val-1]++;
                    }else {
                        nums[val-91]++;
                    }
                }
                for (int k = 0; k < nums.length; k++) {
                    if(nums[k]==4)
                        count1++;
                }
            }
            if(count1>=2)
                count++;
            if(count1==1)
                count3++;
        }

        System.out.println("双龙次数"+count);
        System.out.println("单龙次数"+count3);
    }

    public static List<PenghzCard> val2Card(List<Integer> valList) {
        List<PenghzCard> allCard = new ArrayList<>(toPhzCards(PenghuziConstant.cardList));
        List<PenghzCard> cardList = new ArrayList<>();
        for (Integer val : valList) {
            for (PenghzCard card : allCard) {
                if (card.getVal() == val && !cardList.contains(card)) {
                    cardList.add(card);
                    break;
                }
            }
        }
        return cardList;
    }

    public static PenghuziHuLack isHuNew(PenghuziHandCard handCardBean, PenghzCard disCard, boolean isSelfMo, int outCardHuxi, boolean needDui, boolean isPaoHu,boolean firstCard) {

        PenghuziHuLack lack = new PenghuziHuLack(0);
        lack.setSelfMo(isSelfMo);
        lack.setCheckCard(disCard);
        lack.setNeedDui(needDui);

        PenghzCardIndexArr arr = handCardBean.getIndexArr();
        
        PenghuziIndex index1 = arr.getPaohzCardIndex(1);
        PenghuziIndex index2 = arr.getPaohzCardIndex(2);
        PenghuziIndex index3 = arr.getPaohzCardIndex(3);
        if (index1 != null&&firstCard) {
        	 List<Integer> list = index1.getValList();
        	 if(list.size()==7||(list.size()==6&&index2!=null)||(list.size()==5&&index3!=null)){
        		 for (int val : list) {
        			 List<PenghzCard> cards = index1.getPaohzValMap().get(val);
                     lack.addPhzHuCards(0, toPhzCardIds(cards), 0);
        		 }
        		 lack.setHu(true);
        		 lack.setDaHu(PengHZMingTang.XIAO_QI_DUI);
        		 return lack;
        	 }
        	 
        }
        
        // 手上有3个的
       // PenghuziIndex index2 = arr.getPaohzCardIndex(2);
        if (index2 != null) {
            List<Integer> list = index2.getValList();
            for (int val : list) {
                if (disCard != null && val == disCard.getVal()) {
                    // 抓到手上的牌可以不用强制组出3个
                    handCardBean.getOperateCards().addAll(index2.getPaohzValMap().get(val));
                    continue;
                }
                int huxi = val > 100 ? 6 : 3;
                lack.changeHuxi(huxi);
                List<PenghzCard> cards = index2.getPaohzValMap().get(val);
                lack.addPhzHuCards(PenghzDisAction.action_kan, toPhzCardIds(cards), huxi);
            }
        }

//        PenghuziIndex index3 = arr.getPaohzCardIndex(3);
        if (index3 != null) {
            List<Integer> list = index3.getValList();
            for (int val : list) {
                int huxi = 0;
                boolean paoHu = false;
                int action = PenghzDisAction.action_pao;
                if (disCard != null && val == disCard.getVal()) {
                    if (!isPaoHu) {
                        action = PenghzDisAction.action_kan;
                        huxi = val > 100 ? 6 : 3;
                        handCardBean.getOperateCards().add(disCard);
                    } else if (isSelfMo) {
                        huxi = val > 100 ? 12 : 9;
                        action = PenghzDisAction.action_ti;
                    } else {
                        action = PenghzDisAction.action_pao;
                        paoHu = true;
                        huxi = val > 100 ? 9 : 6;
                        List<PenghzCard> cards = index3.getPaohzValMap().get(val);
                        lack.changeHuxi(huxi);
                        lack.addPhzHuCards(action, toPhzCardIds(cards), huxi);
                    }
                } else {
                    //直接手上有4张
                    action = PenghzDisAction.action_ti;
                    huxi = val > 100 ? 12 : 9;
                    lack.setNeedDui(true);
                }
                if (!paoHu) {
                    lack.changeHuxi(huxi);
                    List<PenghzCard> cards = index3.getPaohzValMap().get(val);
                    if (!isPaoHu) {
                        cards.remove(disCard);
                    }
                    lack.addPhzHuCards(action, toPhzCardIds(cards), huxi);
                }
            }
        }
        List<PenghuziHuLack> huList = new ArrayList<>();
        List<PenghzCard> handPais = handCardBean.getOperateCards();
        if (handPais.size() == 0) {
            lack.setHu(true);
        } else {
            sortMin(handPais);
            chaiPaiNew(huList, lack, handPais, disCard);
            if (huList.size() > 0) {
                return getMaxHuxi(huList);
            }
        }
        return lack;
    }

    public static PenghuziHuLack getMaxHuxi(List<PenghuziHuLack> huList) {
        if (huList != null && huList.size() > 0) {
            PenghuziHuLack maxHu = null;
            int maxHuxi = -99999;
            for (PenghuziHuLack hu : huList) {
                int huxi = hu.calcHuxi();
                hu.setHuxi(huxi);
                if (hu.calcHuxi() > maxHuxi) {
                    maxHuxi = huxi;
                    maxHu = hu;
                }
            }
            return maxHu;
        }
        return null;
    }

    public static void chaiPaiNew(List<PenghuziHuLack> huList, PenghuziHuLack lack, List<PenghzCard> handPais, PenghzCard disCard) {
        if (lack.isNeedDui()) {
            //拆对
            PenghuziHandCard bean = getPaohuziHandCardBean(handPais);
            PenghzCardIndexArr indexArr = bean.getIndexArr();
            PenghuziIndex duiZi = indexArr.getPaohzCardIndex(1);
            if (duiZi != null && duiZi.getPaohzValMap().size() > 0) {
                for (Integer val : duiZi.getPaohzValMap().keySet()) {
                    List<PenghzCard> handPaisCopy = new ArrayList<>(handPais);

                    List<PenghzCard> duiZiCardList = duiZi.getPaohzValMap().get(val);
                    handPaisCopy.removeAll(duiZiCardList);

                    List<Integer> duiZiIdList = new ArrayList<>();
                    for (PenghzCard card : duiZiCardList) {
                        duiZiIdList.add(card.getId());
                    }

                    PenghuziHuLack newLack = lack.clone();
                    newLack.setNeedDui(false);
                    newLack.addPhzHuCards(0, duiZiIdList, 0);
                    if (handPaisCopy.size() == 0) {
                        newLack.setHu(true);
                        huList.add(newLack);
                    } else {
                        if (disCard != null && disCard.getVal() == val) {
                            chaiPaiNew(huList, newLack, handPaisCopy, null);
                        } else {
                            chaiPaiNew(huList, newLack, handPaisCopy, disCard);
                        }
                    }
                }
            }
            PenghuziIndex sanZhang = indexArr.getPaohzCardIndex(2);
            if (sanZhang != null && sanZhang.getPaohzValMap().size() > 0) {
                for (Integer val : sanZhang.getPaohzValMap().keySet()) {
                    List<PenghzCard> handPaisCopy = new ArrayList<>(handPais);

                    List<PenghzCard> duiZiCardList = sanZhang.getPaohzValMap().get(val).subList(0, 2);
                    handPaisCopy.removeAll(duiZiCardList);

                    List<Integer> duiZiIdList = new ArrayList<>();
                    for (PenghzCard card : duiZiCardList) {
                        duiZiIdList.add(card.getId());
                    }

                    PenghuziHuLack newLack = lack.clone();
                    newLack.setNeedDui(false);
                    newLack.addPhzHuCards(0, duiZiIdList, 0);
                    if (handPaisCopy.size() == 0) {
                        newLack.setHu(true);
                        huList.add(newLack);
                    } else {
                        if (disCard != null && disCard.getVal() == val) {
                            chaiPaiNew(huList, newLack, handPaisCopy, null);
                        } else {
                            chaiPaiNew(huList, newLack, handPaisCopy, disCard);
                        }
                    }
                }
            }
        } else {
            if (handPais.size() < 3) {
                return;
            }
            //拆顺
            int val;
            if (disCard != null) {
                val = disCard.getVal();
            } else {
                val = handPais.get(0).getVal();
            }
            List<int[]> paiZus = PenghuziConstant.getPaiZu(val);
            List<PenghzCard> handPaisCopy;
            List<PenghzCard> rmList;
            PenghuziHuLack newLack;
            for (int[] paiZu : paiZus) {
                handPaisCopy = new ArrayList<>(handPais);
                rmList = removeVals(handPaisCopy, paiZu);
                if (rmList == null) {
                    continue;
                }

                newLack = lack.clone();
                if (disCard != null && disCard.getVal() == val && isSameCard(rmList)) {
                    // 三张一模一样的，碰牌分
                    newLack.addPhzHuCards(PenghzDisAction.action_peng, toPhzCardIds(rmList), disCard.isBig() ? 3 : 1);
                } else {
                    newLack.addPhzHuCards(0, toPhzCardIds(rmList), getShunHuxi(rmList));
                }
                if (handPaisCopy.size() == 0) {
                    newLack.setHu(true);
                    huList.add(newLack);
                } else {
                    chaiPaiNew(huList, newLack, handPaisCopy, null);
                }
            }
        }
    }

    public static List<PenghzCard> removeVals(List<PenghzCard> cards, int[] vals) {
        List<PenghzCard> rmList = new ArrayList<>(vals.length);
        for (Integer val : vals) {
            boolean hasVal = false;
            for (PenghzCard card : cards) {
                if (card.getVal() == val && !rmList.contains(card)) {
                    rmList.add(card);
                    hasVal = true;
                    break;
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

}
