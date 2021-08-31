package com.sy599.game.qipai.yywhz.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.msg.serverPacket.TableGhzResMsg.ClosingGhzInfoRes;
import com.sy599.game.msg.serverPacket.TableGhzResMsg.ClosingGhzPlayerInfoRes;
import com.sy599.game.qipai.yywhz.bean.CardTypeHuxi;
import com.sy599.game.qipai.yywhz.bean.WaihuziHuBean;
import com.sy599.game.qipai.yywhz.bean.WaihuziPlayer;
import com.sy599.game.qipai.yywhz.bean.WaihuziTable;
import com.sy599.game.qipai.yywhz.bean.WaihzDisAction;
import com.sy599.game.qipai.yywhz.constant.GuihzCard;
import com.sy599.game.qipai.yywhz.constant.WaihuziConstant;
import com.sy599.game.qipai.yywhz.rule.GuihuziIndex;
import com.sy599.game.qipai.yywhz.rule.GuihuziMenzi;
import com.sy599.game.qipai.yywhz.rule.GuihuziMingTangRule;
import com.sy599.game.qipai.yywhz.rule.GuihzCardIndexArr;
import com.sy599.game.util.JacksonUtil;

/**
 * 鬼胡子
 * 
 * @author lc
 */
public class GuihuziTool {
	/**
	 * 发牌
	 * 
	 * @param copy
	 * @param t
	 * @return
	 */
	public static List<Integer> c2710List = Arrays.asList(2, 7, 10);

	/**
	 * 发牌
	 * @param copy
	 * @param t 做的牌
	 * @return
	 */
	public static synchronized List<List<GuihzCard>> fapai(List<Integer> copy, List<List<Integer>> t) {
		List<List<GuihzCard>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<GuihzCard> pai = new ArrayList<>();
		int j = 1;
		int testcount = 0;
		if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
			for (List<Integer> zp : t) {
				list.add(find(copy, zp));
				testcount += zp.size();
			}
			if (list.size() == 3) {
				list.add(toGhzCards(copy));
				return list;
			} else if (list.size() == 4) {
				return list;
			}
		}
		List<Integer> copy2 = new ArrayList<>(copy);
		int fapaiCount = 19 * 3 + 1 - testcount;// 庄家20张 闲家19张
		if (pai.size() >= 20) {
			list.add(pai);
			pai = new ArrayList<>();
		}
		boolean test = false;
		if (list.size() > 0) {
			test = true;
		}
		for (int i = 0; i < fapaiCount; i++) {
			GuihzCard majiang = GuihzCard.getPaohzCard(copy.get(i));
			copy2.remove((Object) copy.get(i));
			if (test) {
				if (i < j * 19) {
					pai.add(majiang);
				} else {
					list.add(pai);
					pai = new ArrayList<>();
					pai.add(majiang);
					j++;
				}
			} else {
				if (i <= j * 19) {
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
		List<GuihzCard> left = new ArrayList<>();
		for (int i = 0; i < copy2.size(); i++) {
			left.add(GuihzCard.getPaohzCard(copy2.get((i))));
		}
		list.add(left);// 剩下的牌
		return list;
	}
	
	/**
	 * 检查牌是否有重复
	 * 
	 * @param majiangs
	 * @return
	 */
	public static boolean isGuihuziRepeat(List<GuihzCard> majiangs) {
		if (majiangs == null) {
			return false;
		}
		Map<Integer, Integer> map = new HashMap<>();
		for (GuihzCard mj : majiangs) {
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
	 * @param copy
	 * @return
	 */
	public static List<GuihzCard> findRedGhzs(List<GuihzCard> copy) {
		List<GuihzCard> find = new ArrayList<>();
		for (GuihzCard card : copy) {
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
	public static List<GuihzCard> findSmallGhzs(List<GuihzCard> copy) {
		List<GuihzCard> find = new ArrayList<>();
		for (GuihzCard card : copy) {
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
	public static List<GuihzCard> findGhzByVal(List<GuihzCard> copy, int val) {
		List<GuihzCard> list = new ArrayList<>();
		for (GuihzCard phz : copy) {
			if (phz.getVal() == val) {
				list.add(phz);
			}
		}
		return list;
	}

	private static List<GuihzCard> find(List<Integer> copy, List<Integer> valList) {
		List<GuihzCard> pai = new ArrayList<>();
		if (!valList.isEmpty()) {
			for (int zpId : valList) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					GuihzCard phz = GuihzCard.getPaohzCard(card);
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
	public static Map<Integer, Integer> toGhzValMap(List<GuihzCard> phzs) {
		Map<Integer, Integer> ids = new HashMap<>();
		if (phzs == null) {
			return ids;
		}
		for (GuihzCard phz : phzs) {

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
	 * 
	 */
	public static List<Integer> toGhzRepeatVals(List<GuihzCard> phzs) {
		List<Integer> ids = new ArrayList<>();
		if (phzs == null) {
			return ids;
		}
		for (GuihzCard phz : phzs) {
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
	public static List<GuihzCard> toGhzCards(List<Integer> phzIds) {
		List<GuihzCard> cards = new ArrayList<>();
		for (int id : phzIds) {
			cards.add(GuihzCard.getPaohzCard(id));
		}
		return cards;
	}

	/**
	 * 牌转化为Id
	 * 
	 * @param phzs
	 * @return
	 */
	public static List<Integer> toGhzCardZeroIds(List<?> phzs) {
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
	public static List<Integer> toGhzCardIds(List<GuihzCard> phzs) {
		List<Integer> ids = new ArrayList<>();
		if (phzs == null) {
			return ids;
		}
		for (GuihzCard phz : phzs) {
			ids.add(phz.getId());
		}
		return ids;
	}
	
	/**
	 * 牌转化为门子Id
	 * @param phzs
	 * @return
	 */
	public static List<String> toGhzMenziIds(List<GuihuziMenzi> ghzs, String delimiter) {
		List<String> ids = new ArrayList<>();
		if (ghzs == null) {
			return ids;
		}
		for (GuihuziMenzi ghz : ghzs) {
			ids.add(ghz.getMenzi().get(0) + delimiter + ghz.getMenzi().get(1));
		}
		return ids;
	}

	/**
	 * 胡子转化为牌
	 */
	public static List<Integer> toGhzCardVals(List<GuihzCard> phzs, boolean matchCase) {
		List<Integer> majiangIds = new ArrayList<>();
		if (phzs == null) {
			return majiangIds;
		}
		for (GuihzCard card : phzs) {
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
	public static GuihzCardIndexArr getMax(List<GuihzCard> list) {
		GuihzCardIndexArr card_index = new GuihzCardIndexArr();
		Map<Integer, List<GuihzCard>> phzMap = new HashMap<>();
        for (GuihzCard phzCard : list) {
            List<GuihzCard> count;
            if (phzMap.containsKey(phzCard.getVal())) {
                count = phzMap.get(phzCard.getVal());
            } else {
                count = new ArrayList<>();
                phzMap.put(phzCard.getVal(), count);
            }
            count.add(phzCard);
        }
		for (int phzVal : phzMap.keySet()) {
			List<GuihzCard> phzList = phzMap.get(phzVal);
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
     * 自动出牌
     */
    public static GuihzCard autoDisCard(List<GuihzCard> handPais,WaihuziPlayer player) {
        List<GuihzCard> copy = new ArrayList<>(handPais);
        GuihzCardIndexArr valArr = getMax(copy);
        GuihuziIndex index1 = valArr.getPaohzCardIndex(1);
        GuihuziIndex index0 = valArr.getPaohzCardIndex(0);
        
        GuihuziIndex index2 = valArr.getPaohzCardIndex(2);
        List<Integer> values= new ArrayList<>();
        
        if(index0!=null) {
        	values.addAll(index0.getValList());
        }
        if(index1!=null) {
        	values.addAll(index1.getValList());
        }
        if(index2!=null) {
        	values.addAll(index2.getValList());
        }
        
        //List<Integer> list2 = valArr.getPaohzCardIndex(1).getValList();
        //List<Integer> list1 = valArr.getPaohzCardIndex(0).getValList();
        GuihzCard disC = null;
        
        for(Integer value : values){
        	
        	GuihzCard card = getdiscards(handPais, value);
        	if(card == null) {
        		continue;
        	}
    		if(player.getHasPengOrWeiPais(null).contains(card.getVal())) {
    			continue;
    		}
    		if(player.getHasChiMenzi(null, card)) {
    			continue;
    		}
    		disC = card;
    		break;
        	
        }
/*        int val = 0;
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
//        if (val == 0) {
//            return null;
//        } else {
        	GuihzCard card = null;
            for (GuihzCard paohzCard : handPais) {
                if (paohzCard.getVal() == val) {
                    card = paohzCard;
                    break;
                }
            }*/
            return disC;
//        }
    }
    
    
    
    
    
    
    public  static GuihzCard getdiscards (List<GuihzCard> handPais,int val) {
    	GuihzCard card = null;
    	 for (GuihzCard paohzCard : handPais) {
    	        if (paohzCard.getVal() == val) {
    	            card = paohzCard;
    	            break;
    	        }
    	    }
    	 return card;
    }

	/**
	 * 是否能吃
	 * 
	 * @param handCards
	 * @param disCard
	 * @return
	 */
	public static List<GuihzCard> checkChi(List<GuihzCard> handCards, GuihzCard disCard) {
		int disVal = disCard.getVal();
//		int otherVal = disCard.getOtherVal();

		//List<Integer> chi0 = new ArrayList<>(Arrays.asList(disVal, otherVal));
		//List<Integer> chi4 = new ArrayList<>(Arrays.asList(otherVal, otherVal));
		List<Integer> chi1 = new ArrayList<>(Arrays.asList(disVal - 2, disVal - 1));
		List<Integer> chi2 = new ArrayList<>(Arrays.asList(disVal - 1, disVal + 1));
		List<Integer> chi3 = new ArrayList<>(Arrays.asList(disVal + 1, disVal + 2));
		List<List<Integer>> chiList = new ArrayList<>();
		//chiList.add(chi0);
		//chiList.add(chi4);
		chiList.add(chi1);
		chiList.add(chi2);
		chiList.add(chi3);

		// // 不区分大小写 找到值
		// List<PaohzCard> vals = findCountByVal(handCards, disCard, false);
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
		// return new ArrayList<PaohzCard>();
		// }
		// return vals;
		//
		// } else if (vals.size() > needCards) {
		// List<PaohzCard> list = new ArrayList<>();
		// if (needCards == 3) {
		// list.add(disCard);
		// needCards = 2;
		// }
		// int k = 0;
		// int s = 1;
		// for (int i = 0; i < vals.size(); i++) {
		// PaohzCard card = vals.get(i);
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
		// return new ArrayList<PaohzCard>();
		// }
		// return list;
		// }
		// return new ArrayList<PaohzCard>();
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

		List<GuihzCard> copy = new ArrayList<>(handCards);
		copy.remove(disCard);

		for (List<Integer> chi : chiList) {
			List<GuihzCard> findList = findGhzCards(copy, chi);
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

		return new ArrayList<GuihzCard>();
	}
	
	

	public static List<GuihzCard> findGhzCards(List<GuihzCard> cards, List<Integer> vals) {
		List<GuihzCard> findList = new ArrayList<>();
		for (int chiVal : vals) {
			boolean find = false;
			for (GuihzCard card : cards) {
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
	public static List<GuihzCard> getSameCards(List<GuihzCard> handCards, GuihzCard disCard) {
		List<GuihzCard> list = findCountByVal(handCards, disCard, true);
		if (list != null) {
			return list;
		}
		return null;

	}

	/**
	 * 是否一样的牌
	 */
	public static boolean isSameCard(List<GuihzCard> handCards) {
		int val = 0;
		for (GuihzCard card : handCards) {
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
	public static void removePhzByVal(List<GuihzCard> handCards, int cardVal) {
		Iterator<GuihzCard> iterator = handCards.iterator();
		while (iterator.hasNext()) {
			GuihzCard paohzCard = iterator.next();
			if (paohzCard.getVal() == cardVal) {
				iterator.remove();
			}

		}
	}

	/**
	 * 是否有这张相同的牌
	 */
	public static boolean isHasCardVal(List<GuihzCard> handCards, int cardVal) {
		for (GuihzCard card : handCards) {
			if (cardVal == card.getVal()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param phzList
	 *            牌List
	 * @param o
	 *            值or牌
	 * @param matchCase
	 *            是否为值（true取val，false取pai）
     * @return 返回与o的值相同的牌的集合
	 */
	public static List<GuihzCard> findCountByVal(List<GuihzCard> phzList, Object o, boolean matchCase) {
		int val;
		if (o instanceof GuihzCard) {
			if (matchCase) {
				val = ((GuihzCard) o).getVal();
			} else {
				val = ((GuihzCard) o).getPai();
			}
		} else if (o instanceof Integer) {
			val = (int) o;
		} else {
			return null;
		}
		List<GuihzCard> result = new ArrayList<>();
		for (GuihzCard card : phzList) {
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

	public static List<GuihzCard> findByVals(List<GuihzCard> majiangs, List<Integer> vals) {
		List<GuihzCard> result = new ArrayList<>();
		for (int val : vals) {
			for (GuihzCard majiang : majiangs) {
				if (majiang.getVal() == val) {
					result.add(majiang);
					break;
				}
			}
		}

		return result;
	}

	public static WaihuziHuBean checkHu(List<GuihzCard> handCard) {
		if (handCard == null || handCard.isEmpty()) {
			return null;
		}
		WaihuziHuBean bean = new WaihuziHuBean();

		List<GuihzCard> copy = new ArrayList<>(handCard);
		bean.setHandCards(new ArrayList<>(copy));

		GuihzCardIndexArr valArr = GuihuziTool.getMax(copy);
		bean.setValArr(valArr);

		// 去掉3张和4张一样的牌
		GuihuziIndex index3 = valArr.getPaohzCardIndex(3);
		if (index3 != null) {
			copy.removeAll(index3.getPaohzList());
		}
		GuihuziIndex index2 = valArr.getPaohzCardIndex(2);
		if (index2 != null) {
			copy.removeAll(index2.getPaohzList());
		}
		bean.setOperateCards(copy);

		return bean;
	}

	// public static void chaiPai(PaohuziHuBean bean) {
	// 不管大2小2先找出来
	// List<PaohzCard> find2 = findCountByVal(bean.getOperateCards(), 2,
	// false);
	// for (PaohzCard card2 : find2) {
	// 是否有123 或者2710可以配对

	// }

	// }

	/**
	 * 得到某个值的麻将
	 * 
	 * @param copy
	 * @return
	 */
	public static List<GuihzCard> getVals(List<GuihzCard> copy, int val, GuihzCard... exceptCards) {
		List<GuihzCard> list = new ArrayList<>();
		Iterator<GuihzCard> iterator = copy.iterator();
		while (iterator.hasNext()) {
			GuihzCard phz = iterator.next();
			if (exceptCards != null) {
				// 有某些除外的牌不能算在内
				boolean findExcept = false;
				for (GuihzCard except : exceptCards) {
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
	 * 得到某个值的鬼胡子牌
	 * @param copy 手牌
	 */
	public static GuihzCard getVal(List<GuihzCard> copy, int val, GuihzCard... exceptCards) {
        for (GuihzCard phz : copy) {
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (GuihzCard except : exceptCards) {
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

	public static void sortMin(List<GuihzCard> hasPais) {
		Collections.sort(hasPais, new Comparator<GuihzCard>() {

			@Override
			public int compare(GuihzCard o1, GuihzCard o2) {
				if (o1.getPai() < o2.getPai()) {
					return -1;
				}
				if (o1.getPai() > o2.getPai()) {
					return 1;
				}
				return 0;
			}

		});
	}

	// 拆牌
	public static boolean chaipai(GuihuziHuLack lack, List<GuihzCard> hasPais) {
		if (hasPais.isEmpty()) {
			return true;
		}
		boolean hu = chaishun(lack, hasPais);
		if (hu)
			return true;
		return false;
	}

	public static boolean chaiSame(GuihuziHuLack lack, List<GuihzCard> hasPais, GuihzCard minCard, List<GuihzCard> sameList) {
		if (sameList.size() < 3) {// 小于3张牌没法拆
			return false;
		} else if (sameList.size() == 3) {// 大小加一起正好3张牌
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
				GuihuziHuLack copyLack = lack.clone();
				List<GuihzCard> copyHasPais = new ArrayList<>(hasPais);
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
	 * @param hasPais canoperateHandPais
	 */
	private static boolean chaikan(GuihuziHuLack lack, List<GuihzCard> hasPais, GuihzCard minCard) {
		List<GuihzCard> sameValList = findCountByVal(hasPais, minCard, true);
		if (sameValList.size() == 3) {
			boolean chaiSame = chaishun3(lack, hasPais, minCard, false, false, sameValList.get(0).getVal(), sameValList.get(1).getVal(), sameValList.get(2).getVal());
			if (chaiSame) {
				return true;
			}
		}
		return false;
	}

	private static boolean chaiSame0(GuihuziHuLack lack, List<GuihzCard> hasPais, GuihzCard minCard) {
		List<GuihzCard> sameList = findCountByVal(hasPais, minCard, true);
		if (sameList.size() < 3) {
			return false;
		}
		// 拆相同
		GuihuziHuLack copyLack = lack.clone();
		List<GuihzCard> copyHasPais = new ArrayList<>(hasPais);
		if (chaikan(copyLack, copyHasPais, minCard)) {
			lack.copy(copyLack);
			return true;
		}
// 		拆相同2
//		boolean chaiSame = chaiSame(copyLack, copyHasPais, minCard, sameList);
//		if (chaiSame) {
//			lack.copy(copyLack);
//			return true;
//		}
		return false;
	}

	/**
	 * 拆顺
	 */
	public static boolean chaishun(GuihuziHuLack lack, List<GuihzCard> hasPais) {
		if (hasPais.isEmpty()) {
			return true;
		}
		sortMin(hasPais);
		GuihzCard minCard = hasPais.get(0);
		int minVal = minCard.getVal();
		int minPai = minCard.getPai();
		boolean isTryChaiSame = false;
		GuihuziHuLack copyLack = null;
		List<GuihzCard> copyHasPais = null;
		// 优先拆坎子
		if (minPai != 2) {// 如果不是2 尝试拆坎子
			isTryChaiSame = true;
			boolean isHu = chaiSame0(lack, hasPais, minCard);
			if (isHu) {
				return true;
			}
		} else {// 为 2 7 10 则拆顺
			isTryChaiSame = true;
			boolean isHu = chaiSame0(lack, hasPais, minCard);
			if (isHu) {
				return true;
			} else {
				copyLack = lack.clone();
				copyHasPais = new ArrayList<>(hasPais);
			}
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
			GuihuziHuLack copyLack2 = lack.clone();
			List<GuihzCard> copyHasPais2 = new ArrayList<>(hasPais);
			chaishun = chaiShun(copyLack2, copyHasPais2, minCard, true, check2710, pai1, pai7, pai10);
			if (chaishun) {
				lack.copy(copyLack2);
			} else {// 拆2 7 10组合失败
				chaishun = chaiShun(lack, hasPais, minCard, true, false, pai1, pai2, pai3);
			}
		} else {// 拆顺
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

	public static boolean chaiShun(GuihuziHuLack lack, List<GuihzCard> hasPais, GuihzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3) {
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
	 * 
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
	
	private static boolean chaiWuXiPingShun(GuihuziHuLack lack, List<GuihzCard> hasPais, GuihzCard minCard, int pai1, int pai2, int pai3) {
		List<Integer> lackList = new ArrayList<>();
		GuihzCard num1 = getVal(hasPais, pai1);
		GuihzCard num2 = getVal(hasPais, pai2, num1);
		GuihzCard num3 = getVal(hasPais, pai3, num1, num2);
		// 找到一句话的
		List<GuihzCard> hasMajiangList = new ArrayList<>();
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
		if (lackNum > 0) {// 一句话缺牌个数
			return false;
		} else {// 可以一句话
			int huxi = 0;
			int action = WaihzDisAction.action_shun;
			lack.addPhzHuCards(action, toGhzCardIds(hasMajiangList), huxi);
			lack.changeHuxi(huxi);
			hasPais.removeAll(hasMajiangList);
			return wuPingXiChaiShun(lack, hasPais);
		}
	}

	private static boolean chaishun3(GuihuziHuLack lack, List<GuihzCard> hasPais, GuihzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3) {
		int minVal = minCard.getVal();
		List<Integer> lackList = new ArrayList<>();
		GuihzCard num1 = getVal(hasPais, pai1);
		GuihzCard num2 = getVal(hasPais, pai2, num1);
		GuihzCard num3 = getVal(hasPais, pai3, num1, num2);
		// 找到一句话的
		List<GuihzCard> hasMajiangList = new ArrayList<>();
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
		if (lackNum > 0) {// 一句话缺牌个数
			if (lackNum >= 2) {
				List<GuihzCard> count = getVals(hasPais, hasMajiangList.get(0).getVal());
				if (count.size() == 3) {// 直接做坎子
					lack.addLack(count.get(0).getVal());
					hasPais.removeAll(count);				
					lack.addPhzHuCards(WaihzDisAction.action_kang, toGhzCardIds(count), 1);
					lack.changeHuxi(1);
					return chaipai(lack, hasPais);
				}
			} 
//			else if (lackNum == 1) {
//				if (check2710) {
//					lack.addFail2710Val(minVal);
//					return chaipai(lack, hasPais);
//				}
//			}
			if (check2710) {
				lack.addFail2710Val(minVal);
				return chaipai(lack, hasPais);
			} 
			return false;
		} else {// 可以一句话
			int huxi = 0;
			int action = WaihzDisAction.action_shun;
			if (isShun) {
				int minPai = minCard.getPai();
				if (minPai == 2) {// 2710 加胡息
					huxi = getShunHuxi(hasMajiangList);
				}
			} else {
				if (isSameCard(hasMajiangList)) {
					// 如果是三个一模一样的
					action = WaihzDisAction.action_kang;
//					if (lack.isSelfMo()) {
//						action = WaihzDisAction.action_kang;
//					}
					huxi = getYingHuxi(action, hasMajiangList);
				}
			}
			lack.addPhzHuCards(action, toGhzCardIds(hasMajiangList), huxi);
			lack.changeHuxi(huxi);
			hasPais.removeAll(hasMajiangList);
			return chaipai(lack, hasPais);
		}
	}

	/**
	 * 去除重复后得到val的集合
	 * 
	 * @param cards
	 * @return
	 */
	public static Map<Integer, Integer> getDistinctVal(List<GuihzCard> cards) {
		Map<Integer, Integer> valIds = new HashMap<>();
		if (cards == null) {
			return valIds;
		}
		for (GuihzCard phz : cards) {
			if (valIds.containsKey(phz.getVal())) {
				valIds.put(phz.getVal(), valIds.get(phz.getVal()) + 1);
			} else {
				valIds.put(phz.getVal(), 1);
			}
		}
		return valIds;
	}
	
//	/**
//	 * 获取牌型息数
//	 * @param action
//	 * @param cards
//	 * @return
//	 */
//	public static int getHuxi(int action, List<GuihzCard> cards) {
//		int huxi = 0;
//		if (action == 0) {
//			return huxi;
//		}
//		if (action == WaihzDisAction.action_liu) {
//			Map<Integer, Integer> valmap = getDistinctVal(cards);
//			for (Entry<Integer, Integer> entry : valmap.entrySet()) {
//				if (entry.getValue() != 4) {
//					continue;
//				}
//				huxi += 1;
//			}
//		} else if (action == WaihzDisAction.action_chi) {// 吃 只有2710 算1息
//			List<GuihzCard> copy = new ArrayList<>(cards);
//			sortMin(copy);
//			huxi = getShunHuxi(copy);
//		} else if (action == WaihzDisAction.action_peng) {
//			Map<Integer, Integer> valmap = getDistinctVal(cards);
//			for (Entry<Integer, Integer> entry : valmap.entrySet()) {
//				if (entry.getValue() != 3) {
//					continue;
//				}
//				huxi += 1;
//			}
//		} else if (action == WaihzDisAction.action_wei) {
//			Map<Integer, Integer> valmap = getDistinctVal(cards);
//			for (Entry<Integer, Integer> entry : valmap.entrySet()) {
//				if (entry.getValue() != 3) {
//					continue;
//				}
//				huxi += 1;
//			}
//
//		}
//		return huxi;
//	}

	/**
	 * 算硬息
	 * @param action
	 * 溜（四张）：1息
	 * 飘（四张）：1息
	 * 偎（三张）：1息
	 * 坎（三张）：1息
	 * 碰（三张）：1息
	 * @param cards
	 * @return
	 */
	public static int getYingHuxi(int action, List<GuihzCard> cards) {
		int huxi = 0;
		if (action == 0) {
			return huxi;
		}
		if (action == WaihzDisAction.action_liu || action == WaihzDisAction.action_piao || action == WaihzDisAction.action_weiHouLiu|| action == WaihzDisAction.action_qishouLiu) {
			Map<Integer, Integer> valmap = getDistinctVal(cards);
			for (Entry<Integer, Integer> entry : valmap.entrySet()) {
				if (entry.getValue() != 4) {
					continue;
				}
				huxi += 5;
			}
		} else if (action == WaihzDisAction.action_chi || action == WaihzDisAction.action_shun) {// 吃 只有2710 算1息
			List<GuihzCard> copy = new ArrayList<>(cards);
			sortMin(copy);
			huxi = getShunHuxi(copy);
		} else if (action == WaihzDisAction.action_peng || action == WaihzDisAction.action_wei || action == WaihzDisAction.action_kang) {
			Map<Integer, Integer> valmap = getDistinctVal(cards);
			for (Entry<Integer, Integer> entry : valmap.entrySet()) {
				if (entry.getValue() != 3) {
					continue;
				}
				if (action == WaihzDisAction.action_peng){
					huxi += 1;
				}else if (action == WaihzDisAction.action_wei){
					huxi += 4;
				}if (action == WaihzDisAction.action_kang){
					huxi += 3;
				}
			}
		}
		return huxi;
	}

	/**
	 * 算顺子的胡息 2710  1息
	 * @param hasMajiangList
	 * @return
	 */
	private static int getShunHuxi(List<GuihzCard> hasMajiangList) {
		boolean isSamePai = true;
		int minPai = 0;
		for (GuihzCard card : hasMajiangList) {
			if (minPai == 0) {
				minPai = card.getPai();
				continue;
			}
			if (minPai != card.getPai()) {
				isSamePai = false;
				break;
			}

		}
		if (isSamePai) {
			return 0;
		}
		GuihzCard minCard = hasMajiangList.get(0);
		if (minCard.getPai() == 2) {
			for (GuihzCard card : hasMajiangList) {
				if (!c2710List.contains(card.getPai())) {
					return 0;
				}
			}
			return 1;
		}
		return 0;
	}
	
	public static void addHuLackMenzi(GuihuziMenzi menzi, List<Integer> menziIds, GuihuziHuLack lack) {
		if(menzi.getType() == 1) {
			if(menziIds.size()==2) {
				lack.addPhzHuCards(WaihzDisAction.action_jiang, menziIds, 1);
			}else {
				lack.addPhzHuCards(WaihzDisAction.action_kang, menziIds, 1);
			}
			lack.changeHuxi(1);
		} else if(menzi.getType() == 2) {
			if(menziIds.size()==2) {
				lack.addPhzHuCards(WaihzDisAction.action_men, menziIds, 1);
			}else{
				lack.addPhzHuCards(WaihzDisAction.action_shun, menziIds, 1);
			}
			lack.changeHuxi(1);
		} else {
			
			if(menziIds.size()==2) {
				lack.addPhzHuCards(WaihzDisAction.action_men, menziIds, 0);
			}else if(menziIds.size()==1){
				lack.addPhzHuCards(WaihzDisAction.action_dan, menziIds, 0);
			}else {
				lack.addPhzHuCards(WaihzDisAction.action_shun, menziIds, 0);
			}
		}
	}

	/**
	 * 5硬胡息（5砍 5偎） 无息平(起手全是顺子 加伴张) 九对半 项项息(七息)
	 * 是否胡牌  判断胡牌时只算硬息
	 * @param handCards 当前手上的牌
	 * @param disCard  胡的牌
	 * @param isSelfMo 是否自己摸
	 * @param outYingXiCount 当前出的牌硬息数
	 * @param hasWei 是否偎过
	 * @return
	 */
	public static GuihuziHuLack isHu(WaihuziTable table, List<CardTypeHuxi> cardTypes, List<GuihzCard> handCards, GuihzCard disCard, boolean isSelfMo, int outYingXiCount, boolean hasWei, boolean isBegin) {
		GuihuziHuLack lack = new GuihuziHuLack(0);
		lack.setSelfMo(isSelfMo);
		lack.setCheckCard(disCard);
		lack.setHu(false);
		lack.setHuxi(outYingXiCount);
		List<GuihzCard> handCardsCopy = new ArrayList<>(handCards);// 手牌
		if(disCard != null && handCardsCopy.size() % 3 != 2)// 胡的牌
			handCardsCopy.add(0, disCard);
		if(handCardsCopy.size() % 3 != 2) {// 手牌不是模3余2 表示手牌数量有误
			//System.err.println("手牌数量有误  当前数量为:" + handCards.size());
			return lack;
		}
		GuihzCardIndexArr arr = getGuihzCardIndexArr(handCardsCopy);
		
		// 项项息(7息)  或者  硬5息(有砍或者有偎)
		// 计算手牌是否能胡 并且是否满足息数
		List<GuihuziHuLack> lackList = new ArrayList<>();
		List<GuihuziMenzi> menzis = arr.getMenzis(true);
		for(GuihuziMenzi menzi : menzis) {// 拆门子
			List<GuihzCard> copy = new ArrayList<>(handCardsCopy);
			int i = 0;
			List<Integer> menziIds = new ArrayList<>();
			Iterator<GuihzCard> iterator = copy.iterator();
			while(iterator.hasNext()) {
				GuihzCard card = iterator.next();
				if(menzi.getMenzi().contains(card.getVal())) {
					i++;
					menziIds.add(card.getId());
					menzi.getMenzi().remove(new Integer(card.getVal()));
					iterator.remove();
				}
				if (i >= 2) {
					break;
				}
			}
			GuihuziHuLack lackCopy = new GuihuziHuLack(0);
			lackCopy.setSelfMo(isSelfMo);
			lackCopy.setCheckCard(disCard);
			lackCopy.changeHuxi(lack.getHuxi());
			if (lack.getGhzHuCards() != null) {
				lackCopy.setPhzHuCards(new ArrayList<CardTypeHuxi>());
			}
			GuihuziHuLack lackCopy1 = new GuihuziHuLack(0);
			lackCopy1.setSelfMo(isSelfMo);
			lackCopy1.setCheckCard(disCard);
			lackCopy1.changeHuxi(lack.getHuxi());
			if (lack.getGhzHuCards() != null) {
				lackCopy1.setPhzHuCards(new ArrayList<CardTypeHuxi>());
			}
			List<GuihzCard> copy1 = new ArrayList<>(copy);
			boolean hu = chaipai(lackCopy, copy);
			if(disCard != null && menziIds.contains(disCard.getId())) {
				menziIds.remove((Integer)disCard.getId());
				menziIds.add(0, disCard.getId());
			}
			addHuLackMenzi(menzi, menziIds, lackCopy);
			addHuLackMenzi(menzi, menziIds, lackCopy1);
			boolean haswupingxi = wuPingXiChaiShun(lackCopy1, copy1);
			if (hu) {
				lackCopy.setHu(hu);
				lackList.add(lackCopy);
			}
			if(haswupingxi && lackCopy1.getHuxi() == 0) {// 如果有无息平胡
				lackCopy1.setHu(haswupingxi);
				lackCopy1.setHuxi(0);
				lackList.add(lackCopy1);
			}
		}
		boolean hasWupingXi = false;
		GuihuziHuLack wupingxiHuLack = null;
		if (!lackList.isEmpty()) {
			int maxHuxi = 0;
			GuihuziHuLack maxHuxiLack = null;
			for (GuihuziHuLack copy : lackList) {
				if(copy.getHuxi() == 0) {
					hasWupingXi = true;
					wupingxiHuLack = copy;
				}
				System.out.println("huxi:" + copy.getHuxi() + "---" + JacksonUtil.writeValueAsString(copy.getGhzHuCards()));
				if (maxHuxi == 0 || copy.getHuxi() > maxHuxi || (maxHuxiLack != null && copy.getHuxi() == maxHuxi && copy.getKangNum() > maxHuxiLack.getKangNum())) {
					maxHuxi = copy.getHuxi();
					maxHuxiLack = copy;
				}
			}
			maxHuxiLack.refreshWeiHuCard();
			boolean hasWaiYuan = hasWaiyuanOrDahu(table, cardTypes, maxHuxiLack, handCardsCopy);
			boolean hangxi = true;
			int allHxi = 0;
			for(CardTypeHuxi hux : maxHuxiLack.getGhzHuCards()) {
				allHxi+=hux.getHux();
				if(hux.getHux()==0) {
					hangxi =false;
				}
			}
			if(cardTypes!=null && cardTypes.size()>0) {
				for(CardTypeHuxi hux : cardTypes) {
					allHxi+=hux.getHux();
					if(hux.getHux()==0) {
						hangxi =false;
					}
				}
			}
			
			if(allHxi >= 7 ||hangxi) {// 
//				if(maxHuxiLack.contains2710() || (maxHuxiLack.isAllDuizi())) {// 对子胡
					maxHuxiLack.setHu(true);
//					if(hasWupingXi == true && wupingxi)
//						maxHuxiLack.setHasWupingXi(true);// 包含无平息
					maxHuxiLack.refreshSelfMoCard(isSelfMo, disCard);
					return maxHuxiLack;
//				}
			} 
			
//			else if(maxHuxiLack.getHuxi() >= 5 && maxHuxiLack.getHuxi() < 7) {//  硬5息
//				if(maxHuxiLack.getKangNum() > 0 || hasWei || hasWaiYuan) {
//					maxHuxiLack.setHu(true);
//					if(hasWupingXi == true && wupingxi)// 包含无平息
//						maxHuxiLack.setHasWupingXi(true);
//					maxHuxiLack.refreshSelfMoCard(isSelfMo, disCard);
//					return maxHuxiLack;
//				}
//			}
//			if(hasWupingXi == true  && handCardsCopy.size() == 20) {// 只胡无息平
//				wupingxiHuLack.setHasWupingXi(true);
//				wupingxiHuLack.setHu(true);
//				return wupingxiHuLack;
//			}
		}
		return lack;
	}
	
	public static boolean wuPingXiChaiShun(GuihuziHuLack huLack, List<GuihzCard> hasPais) {
		if (hasPais.isEmpty()) {
			return true;
		}
		sortMin(hasPais);
		GuihzCard minCard = hasPais.get(0);
		int minVal = minCard.getVal();
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
		boolean chaishun = false;
		chaishun = chaiWuXiPingShun(huLack, hasPais, minCard, pai1, pai2, pai3);
		return chaishun;
	}
	
	/**
	 * 是否有外圆 或者 大胡
	 * @param cardTypes
	 * @param hu
	 * @param handCards
	 * @return
	 */
	private static boolean hasWaiyuanOrDahu(WaihuziTable table, List<CardTypeHuxi> cardTypes, GuihuziHuLack hu, List<GuihzCard> handCards) {
		List<GuihzCard> allCards = new ArrayList<>();
//		List<CardTypeHuxi> allHuxiCards = new ArrayList<>();
//		for (CardTypeHuxi type : cardTypes) {
//			allCards.addAll(GuihuziTool.toGhzCards(type.getCardIds()));
//			allHuxiCards.add(type);
//		}
//		if (hu != null && hu.getGhzHuCards() != null) {
//			allHuxiCards.addAll(hu.getGhzHuCards());
//			for (CardTypeHuxi type : hu.getGhzHuCards()) {
//				allCards.addAll(GuihuziTool.toGhzCards(type.getCardIds()));
//			}
//		}
//		boolean hasWaiYuan = GuihuziMingTangRule.hasWaiYuan(allCards, allHuxiCards, handCards, hu.getCheckCard());
//		if(hasWaiYuan)
//			return true;
		boolean hasDahu = GuihuziMingTangRule.hasDahu(handCards, table, allCards);
		if(hasDahu)
			return true;
		return false;
	}
	
	/**
	 * 获取手牌里面各个牌的个数
	 * @param handPais
	 * @return
	 */
	public static GuihzCardIndexArr getGuihzCardIndexArr(List<GuihzCard> handPais) {
		List<GuihzCard> copy = new ArrayList<>(handPais);
		GuihzCardIndexArr valArr = GuihuziTool.getMax(copy);
		return valArr;
	}

	
	
	
	public static List<GuihzCard>  getTingZps(List<GuihzCard> handPais,WaihuziPlayer player) {
		
		boolean hasWei = false;
		if(player.getWei() != null && player.getWei().size() > 0)
			hasWei = true;
		int outYingxiCount = player.getOutYingXiCount();		
		WaihuziTable table = player.getPlayingTable(WaihuziTable.class);
		List<GuihzCard>  huCards = new ArrayList<GuihzCard> ();
		if(table==null){
			return huCards;
		}
		
		List<Integer> copy = new ArrayList<>(WaihuziConstant.cardList);
	
		for(Integer id : copy) {
			GuihzCard card =  GuihzCard.getPaohzCard(id);
			if(!checkHuCard(huCards, card)) {
				continue;
			}
			GuihuziHuLack lack = GuihuziTool.isHu(table, player.getCardTypes(), handPais, card, true, outYingxiCount, hasWei, table.getNowDisCardIds()==null);
			if(lack.isHu()){
				huCards.add(card);
			}
		}
		
	//	
		return huCards;
	}
	
	private static boolean checkHuCard(List<GuihzCard>  huCards,GuihzCard card){
		if(huCards.size() ==0) {
			return true;
		}
		for(GuihzCard huC : huCards) {
			if(huC.getVal() == card.getVal()) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * 将array组合成用delimiter分隔的字符串
	 * @return String
	 */
	public static List<GuihzCard> explodeGhz(String str, String delimiter) {
		List<GuihzCard> list = new ArrayList<>();
		if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
			return list;
		String strArray[] = str.split(delimiter);

		for (String val : strArray) {
			GuihzCard phz = null;
			if (val.startsWith("mj")) {
				phz = GuihzCard.valueOf(GuihzCard.class, val);
			} else {
				phz = GuihzCard.getPaohzCard((Integer.valueOf(val)));
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
	public static String implodeGhz(List<GuihzCard> array, String delimiter) {
		if (array == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder("");
		for (GuihzCard i : array) {
			sb.append(i.getId());
			sb.append(delimiter);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> buildClosingInfoResLog(ClosingGhzInfoRes res) {
		List<String> list = new ArrayList<>();
		for (ClosingGhzPlayerInfoRes info : res.getClosingPlayersList()) {
			Map<String, Object> map = new HashMap<>();
			for (Entry<FieldDescriptor, Object> entry : info.getAllFields().entrySet()) {
				if (entry.getValue() instanceof List) {
					List<Object> l = new ArrayList<>();
					for (Object o : (List<?>) entry.getValue()) {
						if (o instanceof String) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof Integer) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof Long) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof GeneratedMessage) {
							l.add(buildGhzClosingInfoResOtherLog((GeneratedMessage) o));
						}
					}
					map.put(entry.getKey().getName(), l);
				} else {
					map.put(entry.getKey().getName(), entry.getValue());
				}
			}
			list.add(JacksonUtil.writeValueAsString(map));
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> buildGhzClosingInfoResOtherLog(GeneratedMessage res) {
		Map<String, Object> map = new HashMap<>();
		for (Entry<FieldDescriptor, Object> entry : res.getAllFields().entrySet()) {
			String name = entry.getKey().getName();
			if (!name.equals("closingPlayers")) {
				if (entry.getValue() instanceof List) {
					List<Object> l = new ArrayList<>();
					for (Object o : (List<?>) entry.getValue()) {
						if (o instanceof String) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof Integer) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof Long) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof GeneratedMessage) {
							l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
						}
					}
					map.put(entry.getKey().getName(), l);

				} else {
					map.put(entry.getKey().getName(), entry.getValue());
				}
			}
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> buildClosingInfoResOtherLog(GeneratedMessage res) {
		Map<String, Object> map = new HashMap<>();
		for (Entry<FieldDescriptor, Object> entry : res.getAllFields().entrySet()) {
			String name = entry.getKey().getName();
			if (!name.equals("closingPlayers")) {
				if (entry.getValue() instanceof List<?>) {
					List<Object> l = new ArrayList<>();
					for (Object o : (List<?>) entry.getValue()) {
						if (o instanceof String) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof Integer) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof Long) {
							l = (List<Object>) entry.getValue();
							break;
						} else if (o instanceof GeneratedMessage) {
							l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
						}
					}
					map.put(entry.getKey().getName(), l);
				} else {
					map.put(entry.getKey().getName(), entry.getValue());
				}
			}
		}
		return map;
	}
	
	public static boolean contains2710(List<CardTypeHuxi> ghzHuCards) {
		for(CardTypeHuxi cardType : ghzHuCards) {
			List<GuihzCard> cards = GuihuziTool.toGhzCards(cardType.getCardIds());
			List<Integer> cardPais = GuihuziTool.toGhzCardVals(cards, false);
			if(cardPais.contains(2) && cardPais.contains(7) && cardPais.contains(10)) {
				return true;
			} else {
				if(!GuihuziTool.isSameCard(cards) && GuihuziTool.c2710List.containsAll(cardPais)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		//1,1,1,2,2,2,3,3,3,4,5,6,7,8,8,8,9,9,9,101;
		List<Integer> vals = Arrays.asList(1,1,1,1,2,2,2,2,3,3,3,3,107,102,110);//102,103,

//		List<GuihzCard> test_chi1 = toGhzCards(new ArrayList<>(Arrays.asList(101,102,102,103,103,107,108,109,2,3,4,7,7,7,8,8,8,9,9,9)));//61, 51, 71, 1, 11, 21, 21, 2, 12, 52, 62, 72, 56, 66, 76
		List<GuihzCard> cards = new ArrayList<>();
		for (int val : vals) {
			for (GuihzCard card : GuihzCard.values()) {
				if (card.getVal() == val && !cards.contains(card)) {
					cards.add(card);
					break;
				}
			}
		}
		//		System.out.println(test_chi1);
		GuihzCard disCard = GuihzCard.getPaohzCard(16);
		List<CardTypeHuxi> cardhuxis = new ArrayList<>();
//		GuihuziHuLack lack = isHu(cardhuxis, test_chi1, disCard, true, 0, false, true);
//		System.out.println("是否胡牌-->" + lack.isHu() + " 胡息:" + lack.getHuxi() + " " + lack.getGhzHuCards().size() + " " + JacksonUtil.writeValueAsString(lack.getGhzHuCards()));
//		List<GuihzCard> test_chi2 = toGhzCards(new ArrayList<>(Arrays.asList(5,6)));
//		List<GuihzCard> chi = checkChi(test_chi2, GuihzCard.getPaohzCard(4));
//		System.out.println(chi);
		GuihuziHuLack huLack = new GuihuziHuLack(0);
		boolean wupingxi = wuPingXiChaiShun(huLack, cards);
		System.out.println("wupingxi:" + wupingxi + "--huxi:" + huLack.getHuxi());

	}
}
