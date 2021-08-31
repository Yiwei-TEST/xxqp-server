package com.sy599.game.qipai.xx2710.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.xx2710.bean.CardTypeHuxi;
import com.sy599.game.qipai.xx2710.bean.PaohuziHandCard;
import com.sy599.game.qipai.xx2710.bean.PaohzDisAction;
import com.sy599.game.qipai.xx2710.bean.Xx2710Player;
import com.sy599.game.qipai.xx2710.bean.Xx2710Table;
import com.sy599.game.qipai.xx2710.constant.PaohuziConstant;
import com.sy599.game.qipai.xx2710.constant.PaohzCard;
import com.sy599.game.qipai.xx2710.rule.PaohuziIndex;
import com.sy599.game.qipai.xx2710.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.xx2710.rule.PaohzCardIndexArr;

/**
 * 跑胡子
 *
 * @author wwj
 */
public class PaohuziTool {
    /**
     * 发牌
     *
     * @param copy
     * @param t
     * @return
     */
    public static List<Integer> c2710List = Arrays.asList(2, 7, 10);


    public static List<List<PaohzCard>> zuopai(List<Integer> copy, List<List<Integer>> t,int playerCount){
        if(t.size()<playerCount)
            return null;
        List<List<PaohzCard>> list = new ArrayList<>();
        for (List<Integer> zp : t) {
            list.add(find(copy, zp));
        }
        if(list.size()>playerCount){
            List<PaohzCard> l=list.get(playerCount);
            for (int i = 0; i < copy.size(); i++) {
                l.add(PaohzCard.getPaohzCard(copy.get(i)));
            }
            list.add(l);
        }else if(list.size()==playerCount){
            List<PaohzCard> l=new ArrayList<>();
            for (int i = 0; i < copy.size(); i++) {
                l.add(PaohzCard.getPaohzCard(copy.get(i)));
            }
            list.add(l);
        }
        return list;
    }

    
    public static synchronized List<List<PaohzCard>> fapai(List<Integer> copy, List<List<Integer>> t,int cardNum,int playerCount) {
        copy = copy.subList(0, copy.size());
        List<List<PaohzCard>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<PaohzCard> pai = new ArrayList<>();
        int j = 1;

        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            List<List<PaohzCard>> zuopai = zuopai(copy, t, playerCount);
            if (zuopai!=null)
                return zuopai;
        }

        int handCardNum=20;
        if(cardNum!=0){
            handCardNum=cardNum;
        }

        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = handCardNum * playerCount + 1 ;
        if (pai.size() >= handCardNum+1) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=21*4+1 正好第一个发牌的人14张其他人13张
            PaohzCard majiang = PaohzCard.getPaohzCard(copy.get(i));
            copy2.remove(copy.get(i));
            if (test) {
                if (i < j * handCardNum) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * handCardNum) {
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
        List<PaohzCard> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(PaohzCard.getPaohzCard(copy2.get((i))));
        }
        list.add(left);
        return list;
    }


    /**
     * 检查麻将是否有重复
     *
     * @param majiangs
     * @return
     */
    public static boolean isPaohuziRepeat(List<PaohzCard> majiangs) {
        if (majiangs == null) {
            return false;
        }

        Map<Integer, Integer> map = new HashMap<>();
        for (PaohzCard mj : majiangs) {
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
    public static List<PaohzCard> findPhzByVal(List<PaohzCard> copy, int val) {
        List<PaohzCard> list = new ArrayList<>();
        for (PaohzCard phz : copy) {
            if (phz.getVal() == val) {
                list.add(phz);
            }
        }
        return list;
    }

    private static List<PaohzCard> find(List<Integer> copy, List<Integer> valList) {
        List<PaohzCard> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    PaohzCard phz = PaohzCard.getPaohzCard(card);
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
    public static Map<Integer, Integer> toPhzValMap(List<PaohzCard> phzs) {
        Map<Integer, Integer> ids = new HashMap<>();
        if (phzs == null) {
            return ids;
        }
        for (PaohzCard phz : phzs) {

            if (ids.containsKey(phz.getVal())) {
                ids.put(phz.getVal(), ids.get(phz.getVal()) + 1);
            } else {
                ids.put(phz.getVal(), 1);
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
    public static List<PaohzCard> toPhzCards(List<Integer> phzIds) {
        List<PaohzCard> cards = new ArrayList<>();
        for (int id : phzIds) {
            cards.add(PaohzCard.getPaohzCard(id));
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
    public static List<Integer> toPhzCardIds(List<PaohzCard> phzs) {
        if (phzs == null) {
            return Collections.emptyList();
        }

        List<Integer> ids = new ArrayList<>(phzs.size());
        for (PaohzCard phz : phzs) {
            ids.add(phz.getId());
        }
        return ids;
    }
    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toPhzCardIds(List<PaohzCard> phzs,List<PaohzCard> outPhzs) {
    	if (phzs == null) {
    		return Collections.emptyList();
    	}
    	
    	List<Integer> ids = new ArrayList<>(phzs.size());
    	for (PaohzCard phz : phzs) {
    		if(outPhzs != null && outPhzs.contains(phz)){
    			ids.add(phz.getId()+1000);
    		}else{
    			ids.add(phz.getId());
    		}
    	}
    	return ids;
    }

    /**
     * 跑胡子转化为牌s
     */
    public static List<Integer> toPhzCardVals(List<PaohzCard> phzs, boolean matchCase) {
        List<Integer> majiangIds = new ArrayList<>();
        if (phzs == null) {
            return majiangIds;
        }
        for (PaohzCard card : phzs) {
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
    public static PaohzCardIndexArr getMax(List<PaohzCard> list) {
        PaohzCardIndexArr card_index = new PaohzCardIndexArr();
        Map<Integer, List<PaohzCard>> phzMap = new HashMap<>();
        for (PaohzCard phzCard : list) {
            List<PaohzCard> count;
            if (phzMap.containsKey(phzCard.getVal())) {
                count = phzMap.get(phzCard.getVal());
            } else {
                count = new ArrayList<>();
                phzMap.put(phzCard.getVal(), count);
            }
            count.add(phzCard);
        }
        for (int phzVal : phzMap.keySet()) {
            List<PaohzCard> phzList = phzMap.get(phzVal);
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

    public static List<PaohzCard> findPhzCards(List<PaohzCard> cards, List<Integer> vals) {
        List<PaohzCard> findList = new ArrayList<>();
        for (int chiVal : vals) {
            boolean find = false;
            for (PaohzCard card : cards) {
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
    public static List<PaohzCard> getSameCards(List<PaohzCard> handCards, PaohzCard disCard) {
        List<PaohzCard> list = findCountByVal(handCards, disCard, true);
        if (list != null) {
            return list;
        }
        return null;

    }

    /**
     * 删除牌
     *
     * @param handCards
     * @param cardVal
     * @return
     */
    public static void removePhzByVal(List<PaohzCard> handCards, int cardVal) {
        Iterator<PaohzCard> iterator = handCards.iterator();
        while (iterator.hasNext()) {
            PaohzCard paohzCard = iterator.next();
            if (paohzCard.getVal() == cardVal) {
                iterator.remove();
            }

        }
    }

    /**
     * @param phzList   牌List
     * @param o         值or牌
     * @param matchCase 是否为值（true取val，false取pai）
     * @return 返回与o的值相同的牌的集合
     */
    public static List<PaohzCard> findCountByVal(List<PaohzCard> phzList, Object o, boolean matchCase) {
        int val;
        if (o instanceof PaohzCard) {
            if (matchCase) {
                val = ((PaohzCard) o).getVal();

            } else {
                val = ((PaohzCard) o).getPai();

            }
        } else if (o instanceof Integer) {
            val = (int) o;
        } else {
            return null;
        }
        List<PaohzCard> result = new ArrayList<>();
        for (PaohzCard card : phzList) {
            if(o==card)
                continue;
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

    public static void sortMin(List<PaohzCard> hasPais) {
        Collections.sort(hasPais, new Comparator<PaohzCard>() {
            @Override
            public int compare(PaohzCard o1, PaohzCard o2) {
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
     * 得到可以操作的牌 4张和3张是不能操作的
     */
    public static PaohuziHandCard getPaohuziHandCardBean(List<PaohzCard> handPais) {
        PaohuziHandCard card = new PaohuziHandCard();
        List<PaohzCard> copy = new ArrayList<>(handPais);
        card.setHandCards(new ArrayList<>(copy));

        PaohzCardIndexArr valArr = PaohuziTool.getMax(copy);
        card.setIndexArr(valArr);
        // 去掉4张和3张
        PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
        if (index3 != null) {
            copy.removeAll(index3.getPaohzList());
        }
        PaohuziIndex index2 = valArr.getPaohzCardIndex(2);
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
    public static List<PaohzCard> explodePhz(String str, String delimiter) {
        List<PaohzCard> list = new ArrayList<>();
        if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
            return list;
        String[] strArray = str.split(delimiter);

        for (String val : strArray) {
            PaohzCard phz = null;
            if (val.startsWith("mj")) {
                phz = PaohzCard.valueOf(PaohzCard.class, val);
            } else {
                phz = PaohzCard.getPaohzCard((Integer.valueOf(val)));
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
    public static String implodePhz(List<PaohzCard> array, String delimiter) {
        if (array == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (PaohzCard i : array) {
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
    public static PaohzCard autoDisCard(List<PaohzCard> handPais) {
        List<PaohzCard> copy = new ArrayList<>(handPais);
        PaohzCardIndexArr valArr = PaohuziTool.getMax(copy);
        PaohuziIndex index1 = valArr.getPaohzCardIndex(1);
        PaohuziIndex index0 = valArr.getPaohzCardIndex(0);
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
            PaohzCard card = null;
            for (PaohzCard paohzCard : handPais) {
                if (paohzCard.getVal() == val) {
                    card = paohzCard;
                    break;
                }
            }
            return card;
        }
    }

    public static void main(String[] args) {
    	int i = 1;
    	del(i);
    	
    }

    public static void del(Integer i){
    	List<Integer> list=new ArrayList<>();
        list.add(6);
        list.add(5);
        list.add(4);
        list.add(3);
        list.add(2);
        list.add(1);
        list.remove(i);
        System.out.println(list);
    }

    //--------------------------------------------------------------------------------------------------------

    /**
     * 找出所有可能的七对
     * @param allCards
     * @return
     */
    public static List<PaohuziHuLack> getAllQiDui(List<Integer> allCards,List<Integer> mt){
        Map<Integer,List<Integer>> map=new HashMap<>();
        List<Integer> copy=new ArrayList<>(allCards);
        Iterator<Integer> it = copy.iterator();
        List<Integer> boss=new ArrayList<>();
        while(it.hasNext()){
            Integer id = it.next();
            PaohzCard card = PaohzCard.getPaohzCard(id);
            int val = card.getVal();
            if(val!=0){
                List<Integer> list = map.get(val);
                if(list==null||list.size()==0){
                    list=new ArrayList<>();
                    list.add(id);
                    map.put(val,list);
                }else {
                    list.add(id);
                }
                it.remove();
            }else {
                boss.add(id);
            }
        }
        if(map.size()>7)
            return null;
        int i=0;
        for (List<Integer> list : map.values()) {
            if (list.size()%2!=0){
                if(i<boss.size()){
                    list.add(boss.get(i));
                    i++;
                }else {
                    return null;
                }
            }
        }
        boss = boss.subList(i,boss.size());
        PaohuziHuLack hu=new PaohuziHuLack();
        for (Map.Entry<Integer,List<Integer>> entry:map.entrySet()) {
            List<Integer> ids=entry.getValue();
            for (int j = 0; j < ids.size() / 2; j++) {
                Map<Integer,Integer> idAndVal=new HashMap<>();
                idAndVal.put(ids.get(j*2),entry.getKey());
                idAndVal.put(ids.get(j*2+1),entry.getKey());
                List<Integer> duizi=ids.subList(j*2,j*2+2);
                hu.addPhzHuCards(0,PaohuziTool.toPhzCards(duizi),idAndVal);
            }
        }
        List<PaohuziHuLack> huList=new ArrayList<>();
//        splitQiDui(huList,hu,boss,mt);
        return huList;
    }


//    public static void splitQiDui(List<PaohuziHuLack> huList,PaohuziHuLack lack,List<Integer> boss,List<Integer> mt){
//        if(boss.size()%2!=0)
//            return;
//        if(boss.size()==0){
//            List<Integer> mingtang=new ArrayList<>(mt);
//            //新的牌型可能不符合板板胡，需要重新检测。
//            mingtang.remove((Object)PaohuziMingTangRule.LOUDI_MINGTANG_BANBANHU);
//            lack.setHu(true);
//            lack.setMingTang(mingtang);
//            huList.add(lack);
//            return;
//        }else {
//            for (int i = 0; i < 20; i++) {
//                int addVal=0;
//                if(i>10)
//                    addVal=100;
//                PaohuziHuLack clone = lack.clone();
//                List<Integer> duizi=new ArrayList<>();
//                duizi.add(boss.get(0));
//                duizi.add(boss.get(1));
//                Map<Integer,Integer> idAndVal=new HashMap<>();
//                idAndVal.put(boss.get(0),i+1+addVal);
//                idAndVal.put(boss.get(1),i+1+addVal);
//                clone.addPhzHuCards(0,PaohuziTool.toPhzCards(duizi),idAndVal);
//                List<Integer> newBoss=boss.subList(2,boss.size());
//                splitQiDui(huList,clone,newBoss,mt);
//            }
//        }
//    }


    //--------------------------------------------------------------------------------------------------------

    /**
     * 该方法找出所有胡牌的牌型
     * @param handCards
     * @param disCard
     * @return
     */
    public static List<PaohuziHuLack> getAllHu(List<Integer> handCards, PaohzCard disCard, Xx2710Player player,Xx2710Table table){
        List<Integer> allCards=new ArrayList<>();
        allCards.addAll(handCards);
        if(disCard!=null&&!allCards.contains(disCard.getId()))
            allCards.add(disCard.getId());
        List<PaohuziHuLack> huList=new ArrayList<>();
        List<Integer> mt = player.getMt();
        mt.clear();
        //板板胡和七对都受牌型影响，需要检测是否和平湖牌型冲突，是否同时具有该名堂，才能胡该名堂，否则只取最大值
//        if(PaohuziMingTangRule.isCanBbh(player,table,allCards))
//            mt.add(PaohuziMingTangRule.LOUDI_MINGTANG_BANBANHU);
//        if(PaohuziMingTangRule.isCanQiDui(allCards))
//            mt.add(PaohuziMingTangRule.LOUDI_MINGTANG_QIDUI);
//        if(table.getHuDieFei()==1&&PaohuziMingTangRule.isHuDieFei(allCards))
//            mt.add(PaohuziMingTangRule.LOUDI_MINGTANG_HUDIEFEI);
        List<PaohuziHuLack> allPingHu = getAllPingHu(allCards,player,table);
        if(allPingHu!=null){
            huList.addAll(allPingHu);
        }
        //七对需要特殊处理
//        if(mt.contains(PaohuziMingTangRule.LOUDI_MINGTANG_QIDUI)){
//            List<PaohuziHuLack> qiDui = getAllQiDui(allCards,mt);
//            for (PaohuziHuLack hu:qiDui) {
//                hu.setMingTang(PaohuziMingTangRule.calcMingTangQiDui(player, hu, table));
//            }
//            huList.addAll(qiDui);
//        }
        //如果只能胡板板胡和蝴蝶飞，则随机组为乱序牌发给前端
//        if(isBbhOrHdf(mt)){
//            randomCards(huList,mt,allCards);
//        }
        return huList;
    }



    public static List<PaohuziHuLack> getAllPingHu(List<Integer> allCarcds,Xx2710Player player,Xx2710Table table){
        if(allCarcds.size()%3!=2)
            return null;
        List<Integer> normal=new ArrayList<>();
        List<Integer> boss=new ArrayList<>();
        for (int i = 0; i < allCarcds.size(); i++) {
            Integer id = allCarcds.get(i);
            if (id<=80)
                normal.add(id);
            else
                boss.add(id);
        }
        List<PaohuziHuLack> huList=new ArrayList<>();
        PaohzCard nowDisCard = table.getNowDisCardIds() == null || table.getNowDisCardIds().size() <= 0?null:table.getNowDisCardIds().get(0);
        splitCardsGetAll(huList,new PaohuziHuLack(),toPhzCards(normal),toPhzCards(boss),boss.size(),nowDisCard,player,0);
        filtratePingHu(huList, player,table);
        return huList;
    }

    public static void splitCardsGetAll(List<PaohuziHuLack> huList,PaohuziHuLack lack,List<PaohzCard> normal,
    		List<PaohzCard> boss,int bossNum,PaohzCard nowDisCard,Xx2710Player player,int passCount){
        if((normal.size()+boss.size())%3==2)
            boss.add(PaohzCard.getPaohzCard(85));
        if(normal==null||normal.size()==0){
            addAllReplaceBoss(lack,huList,boss,bossNum,nowDisCard,player);
            return;
        }
        int val=normal.get(0).getVal();
        List<int[]> paiZus = PaohuziConstant.getPaiZu(val);
        for (int[] paiZu : paiZus) {
        	
        	if(paiZu[0] == paiZu[1]){//碰牌  看看是不是过碰的，过碰不能胡
        		List<Integer> passPeng = player.getPassPeng();
        		if(passPeng!=null && nowDisCard!= null && passPeng.contains(nowDisCard.getVal())){
        			continue;
        		}
        	}
        	//过掉的牌组不能用来再组合
        	boolean isPassMenzi = false;
        	for (int paizuVal : paiZu) {
				if(nowDisCard!= null &&nowDisCard.getVal() == paizuVal){
					if(player.getPassChiMenzi() != null && player.getPassChiMenzi().size() > 0){
		        		for (List<Integer> menzi : player.getPassChiMenzi()) {
							int count = 0;
		        			for (int paizuVal2 : paiZu) {
								if(paizuVal2 != nowDisCard.getVal() && menzi.contains(paizuVal2)){
									count++;
								}
							}
		        			if(count == 2){
//		        			if(paizuList.size() > 0 && menzi.containsAll(paizuList)){
		        				passCount++;
		        				isPassMenzi = true;
		        				break;
		        			}
						}
		        	}
				}
			}
        	
        	if(isPassMenzi){
        		int count = player.getXCardCount(nowDisCard.getVal());
        		if(passCount > count){
        			continue;
        		}
        	}
            List<PaohzCard> normalCopy = new ArrayList<>(normal);
            List<PaohzCard> bossCopy = new ArrayList<>(boss);
            PaohuziHuLack newLack=lack.clone();
            if (!removeVals(normalCopy, paiZu, bossCopy,newLack)) {
                continue;
            }
            if (normalCopy.size()==0&&bossCopy.size()==3){
                //当最后剩3张王牌的时候需要组成所有的牌型
                addAllReplaceBoss(newLack,huList,bossCopy,bossNum,nowDisCard,player);
            }else if (normalCopy.size() +bossCopy.size()== 0) {
                //牌已组合完毕，需要删除最初加入的王牌,同时检测是否胡牌
                deleteBoss85All(newLack,huList,bossNum,nowDisCard,player);
            } else {
                splitCardsGetAll(huList, newLack, normalCopy, bossCopy,bossNum,nowDisCard,player,passCount);
            }
        }
    }

    private static void addAllReplaceBoss(PaohuziHuLack lack,List<PaohuziHuLack> huList,List<PaohzCard> boss,int bossNum,PaohzCard nowDisCard,Xx2710Player player) {
        Map<Integer, List<int[]>> paiZuMap = PaohuziConstant.getPaiZuMap();
        for (Map.Entry<Integer,List<int[]>> entry:paiZuMap.entrySet()) {
            List<int[]> aCardGroup = entry.getValue();
            for (int[] aGroup:aCardGroup) {
                PaohuziHuLack clone = lack.clone();
                clone.addPhzHuCards(isSameCard(aGroup)?PaohzDisAction.action_peng:0,new ArrayList<>(boss),getNewMap(aGroup,boss));
                deleteBoss85All(clone,huList,bossNum,nowDisCard,player);
            }
        }
    }

    private static Map<Integer,Integer> getNewMap(int[] cards,List<PaohzCard> boss) {
        Map<Integer,Integer> bossIdVal=new HashMap<>();
        for (int i = 0; i < cards.length; i++) {
            bossIdVal.put(boss.get(i).getId(),cards[i]);
        }
        return bossIdVal;
    }


    public static List<Integer> cardIdConvertToCardVal(List<Integer> cardIds){
    	if(cardIds.isEmpty()){
    		return null;
    	}
    	List<Integer> list = new ArrayList<>(); 
    	for (Integer cardId : cardIds) {
        	PaohzCard card = PaohzCard.getPaohzCard(cardId);
        	if(card != null){
        		list.add(card.getVal());
        	}
        }
    	return list;
    }
    
    private static int getNumByCardVal(List<CardTypeHuxi> huTypes,int val){
    	if(huTypes == null || huTypes.size() <= 0){
    		return 0;
    	}
    	int count = 0;
    	for (int i = 0; i < huTypes.size(); i++) {
            CardTypeHuxi cardType = huTypes.get(i);
            for (int j = 0; j < cardType.getCardIds().size(); j++) {
            	PaohzCard card = PaohzCard.getPaohzCard(cardType.getCardIds().get(j));
            	if(card.getVal() == val){
            		count ++;
            	}
            }

        }
    	return count;
    }
    private static List<List<Integer>> getCardTypeHuxisCardVal(List<CardTypeHuxi> huTypes,Integer val){
    	if(huTypes == null || huTypes.size() <= 0){
    		return null;
    	}
    	List<List<Integer>> list = new ArrayList<>();
    	for (int i = 0; i < huTypes.size(); i++) {
    		CardTypeHuxi cardType = huTypes.get(i);
    		for (int j = 0; j < cardType.getCardIds().size(); j++) {
    			if( cardType.getAction() != PaohzDisAction.action_peng){
    				List<Integer> valList = cardIdConvertToCardVal(cardType.getCardIds());
    				if(valList.contains(val)){
    					valList.remove(val);
    					list.add(valList);
    				}
    			}
    		}
    	}
    	return list;
    }
    
    private static void deleteBoss85All(PaohuziHuLack lack,List<PaohuziHuLack> huList,int bossNum,PaohzCard nowDiscard,Xx2710Player player) {
        List<CardTypeHuxi> huTypes = lack.getPhzHuCards();
//        if(bossNum==0){
            for (int i = 0; i < huTypes.size(); i++) {
                CardTypeHuxi cardType = huTypes.get(i);
                Integer bosId = isContainBoss(cardType.getCardIds());
                if(cardType.getAction()!=PaohzDisAction.action_peng||bosId==0)
                    continue;
                if(nowDiscard != null ){
                	PaohzCard card = PaohzCard.getPaohzCard(cardType.getCardIds().get(0));
                	if(card.getVal() == nowDiscard.getVal()){
                		int count = getNumByCardVal(huTypes, card.getVal());
                		if(count == 2){
                			continue;
                		}else{
                			//判断跟这张牌组成牌型的牌组是不是被过掉的门子
                			List<List<Integer>> menziList = getCardTypeHuxisCardVal(huTypes, card.getVal());
                			if(!menziList.isEmpty()){
                				int num = 0;
                				for (List<Integer> mzList : menziList) {
                					if(player.getPassChiMenzi().contains(mzList)){
                						num++;
                					}
                				}
                				if(count - num <= 2){
                					continue;
                				}
                			}
                		}
                	}
                }
                removeBoss(cardType,bosId);
                lack.setHu(true);
                huList.add(lack);
            }
//        }else {
//            for (int i = 0; i < huTypes.size(); i++) {
//                CardTypeHuxi cardType = huTypes.get(i);
//                Integer bosId = isContainBoss(cardType.getCardIds());
//                if(cardType.getAction()!=PaohzDisAction.action_peng||bosId==0)
//                    continue;
//                PaohuziHuLack newLack=lack.clone();
//                removeBoss(newLack.getPhzHuCards().get(i),bosId);
//                replaceBoss85(newLack,bosId);
//                newLack.setHu(true);
//                huList.add(newLack);
//            }
//        }
    }

    //--------------------------------------------------------------------------------------------------------

    /**
     * 该方法不需要找出所有胡牌方式，只需要找出是否能胡
     * @param handCards
     * @param disCard
     * @return
     */
    public static boolean isHu(List<Integer> handCards, PaohzCard disCard, Xx2710Player player,Xx2710Table table,boolean canBbh,boolean isCheckTing){
        List<Integer> allCards=new ArrayList<>();
        allCards.addAll(handCards);
        if(disCard!=null&&!allCards.contains(disCard.getId()))
            allCards.add(disCard.getId());
//        if(PaohuziMingTangRule.isCanBbh(player,table,allCards)&&canBbh)
//            return true;
//        if(PaohuziMingTangRule.isCanQiDui(allCards))
//            return true;
//        if(table.getHuDieFei()==1&&PaohuziMingTangRule.isHuDieFei(allCards)){
//            if(!isCheckTing)
//                table.setHuDieSeat(player.getSeat());
//            return true;
//        }
        return isPingHu(allCards,player,table,isCheckTing,disCard);
    }

    public static boolean isPingHu(List<Integer> allCarcds,Xx2710Player player,Xx2710Table table,boolean isCheckTing,PaohzCard disCard){
        if(allCarcds.size()%3!=2)
            return false;
        List<Integer> normal=new ArrayList<>();
        List<Integer> boss=new ArrayList<>();
        for (int i = 0; i < allCarcds.size(); i++) {
            Integer id = allCarcds.get(i);
            if (id<=80)
                normal.add(id);
            else
                boss.add(id);
        }
        return splitCards(new PaohuziHuLack(),toPhzCards(normal),toPhzCards(boss),boss.size(),player,table,isCheckTing,disCard,0);
    }

    public static boolean splitCards(PaohuziHuLack lack,List<PaohzCard> normal,List<PaohzCard> boss,int bossNum,
    		Xx2710Player player,Xx2710Table table,boolean isCheckTing,PaohzCard disCard,int passCount){
        if((normal.size()+boss.size())%3==2)
            boss.add(PaohzCard.getPaohzCard(85));
        if(normal.size()==0)
            return true;
        int val=normal.get(0).getVal();
        List<int[]> paiZus = PaohuziConstant.getPaiZu(val);
        for (int[] paiZu : paiZus) {
        	
        	if(paiZu[0] == paiZu[1]){//碰牌  看看是不是过碰的，过碰不能胡
        		List<Integer> passPeng = player.getPassPeng();
        		if(passPeng!=null && disCard!= null &&  passPeng.contains(disCard.getVal())){
        			continue;
        		}
        	}
        	
        	
        	//过掉的牌组不能用来再组合
        	boolean isPassMenzi = false;
        	for (int paizuVal : paiZu) {
				if(disCard!= null &&disCard.getVal() == paizuVal){
					if(player.getPassChiMenzi() != null && player.getPassChiMenzi().size() > 0){
		        		for (List<Integer> menzi : player.getPassChiMenzi()) {
							int count = 0;
		        			for (int paizuVal2 : paiZu) {
								if(paizuVal2 != disCard.getVal() && menzi.contains(paizuVal2)){
									count++;
								}
							}
		        			if(count == 2){
//		        			if(paizuList.size() > 0 && menzi.containsAll(paizuList)){
		        				passCount++;
		        				isPassMenzi = true;
		        				break;
		        			}
						}
		        	}
				}
			}
        	
        	if(isPassMenzi){
        		int count = player.getXCardCount(disCard.getVal());
        		if(passCount > count){
        			continue;
        		}
        	}
            List<PaohzCard> normalCopy = new ArrayList<>(normal);
            List<PaohzCard> bossCopy = new ArrayList<>(boss);
            PaohuziHuLack newLack=lack.clone();
            if (!removeVals(normalCopy, paiZu, bossCopy,newLack)) {
                continue;
            }
            if (normalCopy.size() +bossCopy.size()== 0) {
                if(deleteBoss85(newLack,bossNum,player,table,isCheckTing,disCard))
                    return true;
            } else {
                if(splitCards(newLack, normalCopy, bossCopy,bossNum,player,table,isCheckTing,disCard,passCount))
                    return true;
            }
        }
        return false;
    }

    private static boolean deleteBoss85(PaohuziHuLack lack,int bossNum,Xx2710Player player,Xx2710Table table,boolean isCheckTing,PaohzCard disCard) {
        List<CardTypeHuxi> huTypes = lack.getPhzHuCards();
        List<PaohuziHuLack> huList= new ArrayList<>();
        List<PaohzCard> nowDisCardIds = isCheckTing ? Arrays.asList(disCard)  : table.getNowDisCardIds();
//        if(bossNum==0){
            for (int i = 0; i < huTypes.size(); i++) {
                CardTypeHuxi cardType = huTypes.get(i);
                Integer bosId = isContainBoss(cardType.getCardIds());
                if(cardType.getAction()!=PaohzDisAction.action_peng||bosId==0)
                    continue;
                
                if(nowDisCardIds != null && nowDisCardIds.size() > 0){
                	PaohzCard nowDiscard = nowDisCardIds.get(0);
                	PaohzCard card = PaohzCard.getPaohzCard(cardType.getCardIds().get(0));
                	if(card.getVal() == nowDiscard.getVal()){
                		int count = getNumByCardVal(huTypes, card.getVal());
                		if(count == 2){//手里一张，胡这张组成一对，单吊不能胡牌
                			continue;
                		}else{
                			//判断跟这张牌组成牌型的牌组是不是被过掉的门子
                			List<List<Integer>> menziList = getCardTypeHuxisCardVal(huTypes, card.getVal());
                			if(!menziList.isEmpty()){
                				int num = 0;
                				for (List<Integer> mzList : menziList) {
                					if(player.getPassChiMenzi().contains(mzList)){
                						num++;
                					}
                				}
                				if(count - num <= 2){
                					continue;
                				}
                			}
                		}
                		
                	}
                }
                
                removeBoss(cardType,bosId);
                lack.setHu(true);
                huList.add(lack);
            }
//        }else {
//            for (int i = 0; i < huTypes.size(); i++) {
//                CardTypeHuxi cardType = huTypes.get(i);
//                Integer bosId = isContainBoss(cardType.getCardIds());
//                if(cardType.getAction()!=PaohzDisAction.action_peng||bosId==0)
//                    continue;
//                //需要判断是否全为王，是的话10种砍可能，都需要加入huList,找出最大得分。
//                PaohuziHuLack newLack=lack.clone();
//                removeBoss(newLack.getPhzHuCards().get(i),bosId);
//                replaceBoss85(newLack,bosId);
//                newLack.setHu(true);
//                huList.add(newLack);
//            }
//        }
        filtratePingHu(huList,player,table);
        return huList.size() != 0;
    }

    private static void removeBoss(CardTypeHuxi cardType,Integer bossId){
        cardType.getCardIds().remove(bossId);
        cardType.setAction(0);
        cardType.getIdAndVals().remove(bossId);
    }

    private static void replaceBoss85(PaohuziHuLack newLack, Integer bosId) {
        for (CardTypeHuxi cardType:newLack.getPhzHuCards()) {
            List<Integer> cardIds = cardType.getCardIds();
            for (int i = 0; i < cardIds.size(); i++) {
                if(cardIds.get(i)==85){
                    cardIds.set(i,bosId);
                    Map<Integer, Integer> idAndVals = cardType.getIdAndVals();
                    int replaceVal = idAndVals.get(85);
                    idAndVals.remove(85);
                    idAndVals.put(bosId,replaceVal);
                }
            }
        }
    }


    private static List<PaohuziHuLack> filtratePingHu(List<PaohuziHuLack> huList,Xx2710Player player,Xx2710Table table){
        Iterator<PaohuziHuLack> it = huList.iterator();
        while (it.hasNext()){
            PaohuziHuLack hu=it.next();
            List<Integer> mt = PaohuziMingTangRule.calcMingTang(player, hu, table);
            boolean isHu=false;
            if(mt!=null&&mt.size()!=0){
                isHu=true;
                hu.setMingTang(mt);
            }else {
                int count=0;
                if(player.getCardTypes()!=null){
                    for (CardTypeHuxi huType: player.getCardTypes()) {
                        for (Integer id: huType.getCardIds()) {
                            if(c2710List.contains(PaohzCard.getPaohzCard(id).getVal()%100))
                                count++;
                        }
                    }
                }
                for (CardTypeHuxi huType: hu.getPhzHuCards()) {
                    for (Integer id: huType.getCardIds()) {
                        if(c2710List.contains(PaohzCard.getPaohzCard(id).getVal()%100))
                            count++;
                    }
                    for (Integer val:huType.getIdAndVals().values()) {
                        if(c2710List.contains(val%100))
                            count++;
                    }
                }
//                if(count>=table.getQihu()){
                    isHu=true;
                    hu.setFen(count);
//                }

            }
            if(!isHu){
                it.remove();
            }else {
                hu.setHu(true);
            }

        }
        return huList;
    }

    private static Integer isContainBoss(List<Integer> cardIds){
        for (int i = 0; i < cardIds.size(); i++) {
            if (cardIds.get(i)>80)
                return cardIds.get(i);
        }
        return 0;
    }

    public static boolean removeVals(List<PaohzCard> normal, int[] vals, List<PaohzCard> boss,PaohuziHuLack lack) {
        List<PaohzCard> copyN=new ArrayList<>(normal);
        List<PaohzCard> copyB=new ArrayList<>(boss);
        List<PaohzCard> rmList = new ArrayList<>(vals.length);
        Map<Integer,Integer> bossIdVal=new HashMap<>();
        for (Integer val : vals) {
            for (PaohzCard card : copyN) {
                if (card.getVal() == val && !rmList.contains(card)) {
                    rmList.add(card);
                    break;
                }
            }
        }
        if (rmList.size()+copyB.size()< vals.length) {
            return false;
        }
        copyN.removeAll(rmList);
        int rmSize=rmList.size();
        if(isSameCard(vals)){
            for (int i = 0; i < 3-rmSize; i++) {
                PaohzCard b = copyB.get(0);
                rmList.add(b);
                bossIdVal.put(b.getId(),vals[0]);
                copyB.remove(b);
            }

        }else {
            List<Integer> rmVals = toPhzCardVals(rmList, true);
            for (Integer val : vals) {
                if(!rmVals.contains(val)){
                    PaohzCard b = copyB.get(0);
                    rmList.add(b);
                    bossIdVal.put(b.getId(),val);
                    copyB.remove(b);
                }
                if(rmList.size()==3)
                    break;
            }
        }
        lack.addPhzHuCards(isSameCard(vals)?PaohzDisAction.action_peng:0,rmList,bossIdVal);
        normal.clear();
        normal.addAll(copyN);
        boss.clear();
        boss.addAll(copyB);
        return true;
    }

    /**
     * 是否一样的牌
     */
    public static boolean isSameCard(List<PaohzCard> handCards) {
        int val = -1;
        for (PaohzCard card : handCards) {
            if (val == -1) {
                val = card.getVal();
                continue;
            }
            if (val != card.getVal()&&card.getVal()!=0) {
                return false;
            }
        }
        return true;
    }
    public static boolean isSameCard(int[] ids) {
        int val = 0;
        for (int i = 0; i < ids.length; i++) {
            if (val == 0) {
                val = ids[i];
                continue;
            }
            if (val != ids[i]) {
                return false;
            }
        }
        return true;
    }


//    private static boolean isBbhOrHdf(List<Integer> mts){
//        if(mts==null||mts.size()==0)
//            return false;
//        for (Integer mt:mts) {
//            if(mt!=PaohuziMingTangRule.LOUDI_MINGTANG_BANBANHU&&mt!=PaohuziMingTangRule.LOUDI_MINGTANG_HUDIEFEI)
//                return false;
//        }
//        return true;
//    }

    private static void randomCards(List<PaohuziHuLack> huList, List<Integer> mt,List<Integer> handCards) {
        PaohuziHuLack hu=new PaohuziHuLack();
        List<Integer> boss=new ArrayList<>();
        List<Integer> normal=new ArrayList<>();
        for (int i = 0; i < handCards.size(); i++) {
            Integer id = handCards.get(i);
            if(id>80){
                boss.add(id);
            }else {
                normal.add(id);
            }
        }
        List<PaohzCard> bos=PaohuziTool.toPhzCards(boss);
        hu.addPhzHuCards(0,bos,null);

        int c=normal.size()/3;
        if(normal.size()%3!=0){
           c++;
        }
        for (int i = 0; i < c; i++) {
            List<PaohzCard> cards;
            if(i<c-1){
                cards = PaohuziTool.toPhzCards(normal.subList(i*3,i*3+3));
            }else {
                cards = PaohuziTool.toPhzCards(normal.subList(i*3,normal.size()));
            }
            hu.addPhzHuCards(0,cards,null);
        }
        hu.setHu(true);
        hu.setMingTang(mt);
        huList.add(hu);
    }

    //-------------------------------------------------------------------------------------------------

    public static Map<Integer,List<Integer>> checkDisTing(List<Integer> handCards, Xx2710Player player,Xx2710Table table){

        List<Integer> residue = addResidueCards(table, handCards);
        //disVal-tingVals
        Map<Integer,List<Integer>> disAndTingVals=new HashMap<>();

        for (int i = 0; i <handCards.size(); i++) {
            if(handCards.get(i)>80){
                continue;
            }
            Integer disCard = handCards.get(i);
            if(player.getForbidDis().contains(disCard)){
            	continue;
            }
            List<Integer> tingVals = disAndTingVals.get(PaohzCard.getPaohzCard(disCard).getVal());
            if(tingVals!=null&&tingVals.size()>0){
                continue;
            }

            tingVals=new ArrayList<>();
            List<Integer> copyHand=new ArrayList<>(handCards);
            copyHand.remove(i);
            for (int j = 0; j < residue.size(); j++) {
                PaohzCard falseCard = PaohzCard.getPaohzCard(residue.get(j));
                if(PaohzCard.getPaohzCard(disCard).getVal() == falseCard.getVal()){
                	continue;
                }
                if(tingVals.contains(falseCard.getVal()))
                    continue;

                if(isHu(copyHand,falseCard,player,table,false,true)){
                    tingVals.add(falseCard.getVal());
                    disAndTingVals.put(PaohzCard.getPaohzCard(disCard).getVal(),tingVals);
                }


            }
        }

        return valMapToIdMap(disAndTingVals,handCards);
    }

    public static List<Integer> checkTing(List<Integer> handCards, Xx2710Player player,Xx2710Table table){

        List<Integer> residue = addResidueCards(table, handCards);
        List<Integer> tingVals=new ArrayList<>();
        List<Integer> copyHand=new ArrayList<>(handCards);
        for (int j = 0; j < residue.size(); j++) {

            PaohzCard falseCard = PaohzCard.getPaohzCard(residue.get(j));
            if(tingVals.contains(falseCard.getVal()))
                continue;

            if(isHu(copyHand,falseCard,player,table,false,true)){
                tingVals.add(falseCard.getVal());

            }
        }
        return listValToRId(tingVals);
    }


    public static List<Integer> addResidueCards(Xx2710Table table,List<Integer> handCards){
        List<Integer> residue =new ArrayList<>(PaohuziConstant.cardList);
        residue = residue.subList(0, residue.size());
        residue.removeAll(handCards);
        residue.removeAll(table.getOutCards());
        return residue;
    }

    public static Map<Integer,List<Integer>> valMapToIdMap(Map<Integer,List<Integer>> disAndTingVals,List<Integer> handCards){
        Map<Integer,List<Integer>> idAndTingId=new HashMap<>();
        for (int i = 0; i < handCards.size(); i++) {
            PaohzCard card = PaohzCard.getPaohzCard(handCards.get(i));
            List<Integer> list = disAndTingVals.get(card.getVal());
            if(list!=null&&list.size()!=0){
                idAndTingId.put(card.getId(),listValToRId(list));
            }
        }
        return idAndTingId;
    }

    /**
     * 随机（默认取得第一个）cardId传给前端用于听牌提示。
     * @param vals
     * @return
     */
    private static List<Integer> listValToRId(List<Integer> vals){
        if(vals==null)
            return new ArrayList<>();
        List<Integer> ids=new ArrayList<>();
        for (int i = 0; i < vals.size(); i++) {
            Integer val = vals.get(i);
            ids.add(PaohzCard.getPaohzCardsByVal(val).get(0).getId());
        }
        return ids;
    }
}
