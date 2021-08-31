package com.sy599.game.qipai.daozmj.tool;

import java.time.Clock;
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

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.daozmj.bean.DaozMjCardDisType;
import com.sy599.game.qipai.daozmj.bean.DaozMjDisAction;
import com.sy599.game.qipai.daozmj.bean.DaozMjPlayer;
import com.sy599.game.qipai.daozmj.bean.DaozMjTable;
import com.sy599.game.qipai.daozmj.constant.DaozMjConstants;
import com.sy599.game.qipai.daozmj.rule.DaozMjIndex;
import com.sy599.game.qipai.daozmj.rule.DaozMjIndexArr;
import com.sy599.game.qipai.daozmj.rule.DzMj;
import com.sy599.game.qipai.daozmj.tool.hulib.util.HuUtil;
import com.sy599.game.util.JacksonUtil;

/**
 * @author lc
 */
public class DaozMjTool {


    public static synchronized List<List<DzMj>> fapai(List<Integer> copy, List<List<Integer>> t) {
        List<List<DzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<DzMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(DaozMjHelper.find(copy, zp));
                testcount += zp.size();
            }

            if (list.size() == 4) {
                list.add(DaozMjHelper.toMajiang(copy));
                System.out.println(JacksonUtil.writeValueAsString(list));
                return list;
            } else if (list.size() == 5) {
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = 13 * 4 + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*4+1 正好第一个发牌的人14张其他人13张
            DzMj majiang = DzMj.getMajang(copy.get(i));
            copy2.remove((Object) copy.get(i));
            if (test) {
                if (i < j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 13) {
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
        List<DzMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(DzMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static synchronized List<List<DzMj>> fapai(List<Integer> copy, int playerCount) {
        List<List<DzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<DzMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(DzMj.getMajang(id));
        }
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                list.add(new ArrayList<>(allMjs.subList(0, 14)));
            } else {
                list.add(new ArrayList<>(allMjs.subList(14 + (i - 1) * 13, 14 + (i - 1) * 13 + 13)));
            }
            if (i == playerCount - 1) {
                list.add(new ArrayList<>(allMjs.subList(14 + (i) * 13, allMjs.size())));
            }

        }
        return list;
    }

    public static synchronized List<List<DzMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<DzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<DzMj>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(DaozMjHelper.find(copy, zp));
            }
        }
        List<DzMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(DzMj.getMajang(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<DzMj> pai = zpList.get(0);
                    int len = 14 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 14)));
                }
            } else {
                if (zpList.size() > i) {
                    List<DzMj> pai = zpList.get(i);
                    int len = 13 - pai.size();
                    pai.addAll(allMjs.subList(count, count + len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, count + 13)));
                    count += 13;
                }
            }
            if (i == playerCount - 1) {
                if (zpList.size() > i + 1) {
                    List<DzMj> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
        return list;
    }

    public static synchronized List<List<DzMj>> fapai(List<Integer> copy) {
        List<List<DzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<DzMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDeveloper()) {
            if (list.size() == 5) {
                System.out.println(JacksonUtil.writeValueAsString(list));
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);

        int fapaiCount = 13 * 4 + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*4+1 正好第一个发牌的人14张其他人13张
            DzMj majiang = DzMj.getMajang(copy.get(i));
            copy2.remove((Object) copy.get(i));
            if (test) {
                if (i < j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 13) {
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
        List<DzMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(DzMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    /**
     * 长沙麻将胡牌
     *
     * @param majiangIds
     * @return 0平胡 1 碰碰胡 2将将胡 3清一色 4双豪华7小对 5豪华小对 6:7小对 7全求人 8大四喜 9板板胡 10缺一色
     * 11六六顺
     */
//	public static CSMjiangHu isHuChangsha(List<DzMj> majiangIds, List<DzMj> gang, List<DzMj> peng, List<DzMj> chi, List<DzMj> buzhang, boolean isbegin) {
//		CSMjiangHu hu = new CSMjiangHu();
//		if (majiangIds == null || majiangIds.isEmpty()) {
//			return hu;
//		}
//
//		if (isPingHu(majiangIds)) {
//			hu.setPingHu(true);
//			hu.setHu(true);
//			hu.setXiaohu(true);
//		}
//
//		ChangshaMajiangRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang);
//		if (isbegin) {
//			ChangshaMajiangRule.checkXiaoHu(hu, majiangIds);
//		}
//		if (hu.isHu()) {
//			hu.setShowMajiangs(majiangIds);
//		}
//		if (hu.isDahu()) {
//			hu.initDahuList();
//		}
//		return hu;
//	}
//    public static boolean isPingHu(List<DzMj> majiangIds) {
//        return isPingHu(majiangIds, true);
//
//    }

    public static boolean isPingHu(List<DzMj> majiangIds, int wangVal) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        if (majiangIds.size() % 3 != 2) {
            System.out.println("%3！=2");
            return false;

        }

        // 先去掉红中
        List<DzMj> copy = new ArrayList<>(majiangIds);
        List<DzMj> hongzhongList = dropWangBa(copy,wangVal);

        DaozMjIndexArr card_index = new DaozMjIndexArr();
        DaozMjQipaiTool.getMax(card_index, copy);
        // 拆将
        if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isHu(List<DzMj> mjs, DaozMjTable table) {
    	
    	int wangCount = DaozMjQipaiTool.getMajiangCount(mjs, table.getWangMjVal());
//        if (hongZhongCount >= 4&& table.isSiBaHZ()) {
//            // 4张红中直接胡
//            return true;
//        }
    	
    	
    	
    	
    	return isHu(mjs, true,table,false);
    	
    }
    
 public static boolean disIsHu(List<DzMj> mjs, DaozMjTable table,boolean moWang) {
//    	int wangCount = DaozMjQipaiTool.getMajiangCount(mjs, table.getWangMjVal());
////        if (hongZhongCount >= 4&& table.isSiBaHZ()) {
////            // 4张红中直接胡
////            return true;
////        }
//    	
    	return isHu(mjs, true,table,moWang);
    	
    }

    /**
     * 麻将胡牌
     *
     * @param mjs
     * @return
     */
    public static boolean isHu(List<DzMj> mjs, boolean hu7dui,DaozMjTable table,boolean disWang) {
        return isHuNew(mjs, hu7dui, table, disWang,0);
        // 拆将
//        if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
//            return true;
//        } else {
//            return false;
//        }

    }

	public static boolean isHuNew(List<DzMj> mjs, boolean hu7dui, DaozMjTable table, boolean disWang,int dwang) {
		if (mjs == null || mjs.isEmpty()) {
            return false;
        }
        List<DzMj> copy = new ArrayList<>(mjs);
        
        // 先去掉红中
        List<DzMj> wangs = dropWangBa(copy,table.getWangMjVal());
        
        if(disWang){
        	copy.add(wangs.remove(0));
        }
        
//        if (hongzhongList.size() == 4) {
//            // 4张红中直接胡
//            return true;
//        }
        if (mjs.size() % 3 != 2) {
            return false;
        }
  
        
        int hzCount = DaozMjTool.haveHongzhong(copy);
        
        if(hu7dui){
        	 boolean qidui = checkHuQdui(table, copy, wangs.size(), hzCount);
             if(qidui){
             	return true;
             }
        }
       
        //checkHuQdui(hu7dui, wangVal, copy, wangs, hzCount);
        
        boolean isHu= HuUtil.isCanHu(new ArrayList<>(copy), wangs.size()-dwang);
        
        if(!isHu&&hzCount>0&&table.getWangMjVal()<200){                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
//        	//有红中再变下牌
//        	for(int i=1;i<=hzCount;i++){
        		 List<DzMj> cards2 = new ArrayList<>(copy);
        			// 有红中再变下牌
					for (int j = 1; j <= hzCount; j++) {
						List<DzMj> cards3 = new ArrayList<>(cards2);
						DaozMjTool.dropHzhong(cards3, j);
						if(j>=2){
							if(wangs.size()>0){
								DaozMjTool.dropWangByCout(cards3, table.getWangMjVal(), 1);
								isHu = HuUtil.isCanHu(cards3, wangs.size()-1);
							}else{
								
								DaozMjTool.dropWangByCout(cards3, table.getWangMjVal(), 1);
								isHu = HuUtil.isCanHu(cards3, wangs.size()-dwang);
							}
							//变成刻子能胡
							if(isHu){
								isHu =false;
								break;
							}
						}
						cards3.addAll(DzMj.getSameMjList(j, table.getWangMjVal(), copy));
						isHu = HuUtil.isCanHu(cards3, wangs.size()-dwang);
						if (isHu) {
							table.setHuHzRepCount(j);
//							break;
						}
					}
//        		 
//        		 
//        		 
//        		DaozMjTool.dropHzhong(cards2, i);
//        		cards2.addAll(DzMj.getSameMjList(i, table.getWangMjVal(),copy));
//        		isHu= HuUtil.isCanHu(cards2, wangs.size());
//        		if(isHu){
//        			table.setHuHzRepCount(i);
//        			break;
//        		}
//        	}
        }
        
        
        return isHu;
	}
    
    
    
    
    
    
    
    
    /**
     * 检查王有没有替代其他牌
     *
     * @param
     * @return
     */
    public static int isHuCalM(List<DzMj> copy,DaozMjTable table,int wangCount) {
//    	if(wangCount==0){
//    		return 0;
//    	}
    	
		// List<DzMj> copy = new ArrayList<>(mjs);
		// if (mjs.size() % 3 != 2) {
		// return false;
		// }

		int hzCount = DaozMjTool.haveHongzhong(copy);
		// 有红中再变下牌
		for (int i = 0; i <= wangCount; i++) {
			List<DzMj> cards2 = new ArrayList<>(copy);
			DaozMjTool.dropWangByCout(cards2, table.getWangMjVal(), i);
			// 有王但是不去掉，就当本身
			boolean qidui = checkHuQdui(table, cards2, i, hzCount);
			
//			else {
				boolean isHu = HuUtil.isCanHu(cards2, i);
				if (isHu) {
					return i;
				}
				if (!isHu && hzCount > 0 && table.getWangMjVal() < 200) {
					// 有红中再变下牌
					int repHzCountHu= 0;
					for (int j = 1; j <= hzCount; j++) {
						List<DzMj> cards3 = new ArrayList<>(cards2);
						
						List<DzMj> reHzs = DaozMjTool.dropHzhong(cards3, j);
						if(j>=2){
							if(i>0){
								List<DzMj> reWangs = DaozMjTool.dropWangByCout(cards3, table.getWangMjVal(), 1);
								isHu = HuUtil.isCanHu(cards3, i-1);
								if(!isHu){
									reWangs.addAll(reWangs);
								}
								
							}else{
								List<DzMj> reWangs = DaozMjTool.dropWangByCout(cards3, table.getWangMjVal(), 1);
								isHu = HuUtil.isCanHu(cards3, i);
								if(!isHu){
									reWangs.addAll(reWangs);
								}
							}
							//变成刻子能胡
							if(isHu){
								break;
							}
						}
						
						List<DzMj> hzReps = DzMj.getSameMjList(j, table.getWangMjVal(), cards3);
						cards3.addAll(hzReps);
						//红中代替王，应该是顺子，如果组不成顺子就不能替代
						int hzRepWang = j;//如果
						boolean hzRepShunzi =true;
						if(j==1&&!hzReps.isEmpty()&&wangCount>0){
							//List<DzMj> chiList = checkChi(cards3, hzReps.get(0), table.getWangMjVal());

                            List<List<DzMj>> chiList2 = checkChiHzrep(cards3,hzReps.get(0));

							if(chiList2.isEmpty()){
								//不能替代回滚
								if(!getWangChiMj(table.getWangMjVal(), cards3)){
									hzRepWang = 0;
									cards3.remove(hzReps.get(0));
									cards3.addAll(reHzs);
								}
								
							}else{
							    for (List<DzMj> chiList:chiList2){
                                    //红中当顺子拿掉
                                    boolean isHu2 = HuUtil.isCanHu(cards3, i);
                                    if(isHu2){
                                        cards3.removeAll(chiList);
                                        cards3.remove(hzReps.get(0));
                                        isHu2 = HuUtil.isCanHu(cards3, i);
                                        if(!isHu2){
                                            cards3.addAll(chiList);
                                            cards3.add(hzReps.get(0));
                                            hzRepShunzi =false;
                                            //红中当顺子不能胡，那就是当将
                                            //checkHZRepHu(i, cards3, hzReps, chiList);
                                        }else{
                                            hzRepShunzi = true;
                                            break;
                                        }
                                    }
                                }

							}
						}
						
						isHu = HuUtil.isCanHu(cards3, i);
						if (isHu) {
							table.setHuHzRepCount(hzRepWang);
							repHzCountHu = i;
							if(!hzRepShunzi&&i==0){
								repHzCountHu=1;
							}
							
							if(i<wangCount){
								return repHzCountHu;
							}
							
//							return i;
						}
					}
					
					if(repHzCountHu>0){
						return repHzCountHu;
					}
					
				}
				
				if (qidui) {
					return i;
				} 
				

//			}

		}

        return 0;
        
        

    }

	private static boolean  checkHZRepHu(int i, List<DzMj> cards3, List<DzMj> hzReps, List<DzMj> chiList) {
		boolean isHu2=false;
		for(int k=2;k<=3;k++){
			cards3.removeAll(chiList);
			chiList= checkChi2(cards3, hzReps.get(0), k);
			if(!chiList.isEmpty()){
				cards3.addAll(chiList);
				isHu2= HuUtil.isCanHu(cards3, i);
				if(isHu2){
					cards3.removeAll(chiList);
					cards3.remove(hzReps.get(0));
					return isHu2;
				}
			}
		}
		return isHu2;
	}
    
    
    
    private static boolean getWangChiMj(int wangVal,List<DzMj> hands){
    	if(wangVal>200){
    		return false;
    	}
    	for(DzMj mj: hands){
    		if(mj.getVal()+1==wangVal||mj.getVal()+2==wangVal||mj.getVal()-1==wangVal||mj.getVal()-2==wangVal){
    			return true;
    		}
    		
    	}
    	return false;
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    

	private static boolean checkHuQdui(DaozMjTable table, List<DzMj> copy, int wangCount, int hzCount) {
		DaozMjIndexArr card_index = new DaozMjIndexArr();
        DaozMjQipaiTool.getMax(card_index, copy);
        	if(check7duizi(copy, card_index, wangCount)){
        		return true;
        	}
//        	if(hzCount>0&&table.getWangMjVal()<200){
//        		//有红中再变下牌
//            	for(int i=1;i<=hzCount;i++){
//            		 List<DzMj> cards2 = new ArrayList<>(copy);
//            		DaozMjTool.dropHzhong(cards2, i);
//            		cards2.addAll(DzMj.getSameMjList(i, table.getWangMjVal(),cards2));
//            		if(check7duizi(cards2, card_index, wangCount)){
//            			table.setHuHzRepCount(i);
//                		return true;
//                	}
//            	}
//        	}
        	return false;
	}
    
    /**
     * 是否是清一色
     * @param allMajiangs
     * @return
     */
    public static boolean isQingyise(List<DzMj> allMajiangs,int wangVal){
    	boolean qingyise = false;
		int se = 0;
		for (DzMj mjiang : allMajiangs) {
			//mjiang.isHongzhong()
			if (mjiang.getVal()==wangVal) {
				continue;
			}
			if (se == 0) {
				qingyise = true;
				se = mjiang.getHuase();
				continue;
			}

			if (mjiang.getHuase() != se) {
				qingyise = false;
				break;
			}
		}
		return qingyise;
    }
    
    /***
     * 胡七对
     * @param allMajiangs
     * @return
     */
    public static boolean isHuQidui(List<DzMj> allMajiangs,int wangVal){
         // 先去掉红中
         List<DzMj> hongzhongList = dropWangBa(allMajiangs,wangVal);
         DaozMjIndexArr card_index = new DaozMjIndexArr();
         DaozMjQipaiTool.getMax(card_index, allMajiangs);
         if (check7duizi(allMajiangs, card_index, hongzhongList.size())) {
             return true;
         }
         return false;
    }
    
    
    
    /**
     * 碰碰胡
     * @param majiangIds
     * @return
     */
    public static boolean isPengPengHu(List<DzMj> majiangIds,int wangCount) {
		DaozMjIndexArr all_card_index = new DaozMjIndexArr();
		DaozMjQipaiTool.getMax(all_card_index, majiangIds);
		
		
		DaozMjIndex index4 = all_card_index.getMajiangIndex(3);
		DaozMjIndex index3 = all_card_index.getMajiangIndex(2);
		DaozMjIndex index2 = all_card_index.getMajiangIndex(1);
		DaozMjIndex index1 = all_card_index.getMajiangIndex(0);

		int sameCount = 0;
		if (index4 != null) {
			sameCount += index4.getLength();
		}
		if (index3 != null) {
			sameCount += index3.getLength();
		}
		
		// 3个相同或者4个相同有4个
		if (sameCount == 4 && index2 != null && index2.getLength() == 1 ) {
			return true;
		} 
		else if (wangCount>0 && index4 != null && index4.getLength() == 2 && index1 != null && index1.getLength() == 1 && index2 == null) {
			return true;
		} else if (wangCount>0 && sameCount >= 3 && index2 == null && index1 != null && index1.getLength() == 2) {
			return true;
		} else if (wangCount>0 && sameCount >= 2 && index2 != null && index2.getLength() == 2 && index1 != null && index1.getLength() == 1) {
			return true;
		}else if(wangCount>0 && sameCount > 3 && index2 == null && index1 != null && index1.getLength() == 1){
			return true;
		}else if(wangCount>0 && sameCount == 3 && ((index2 != null  && index2.getLength() == 2)||(index1 != null && index1.getLength() == 1&&wangCount>1))){
			return true;
		}
		return false;
	}
    
    

    /**
     * 是否胡七对
     *
     * @param mjs     去掉红中的麻将
     * @param hzCount
     * @return
     */
    public static boolean isHu7Dui(List<DzMj> mjs, int hzCount) {
        DaozMjIndexArr card_index = new DaozMjIndexArr();
        DaozMjQipaiTool.getMax(card_index, mjs);
        return check7duizi(mjs, card_index, hzCount);
    }

    public static boolean isTing(List<DzMj> majiangIds, boolean hu7dui,int wangVal) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        List<DzMj> copy = new ArrayList<>(majiangIds);
        // 先去掉红中
        List<DzMj> hongzhongList = dropWangBa(copy,wangVal);
        if (majiangIds.size() % 3 != 1) {
            return false;
        }
        DaozMjIndexArr card_index = new DaozMjIndexArr();
        DaozMjQipaiTool.getMax(card_index, copy);
        if (hu7dui && check7duizi(copy, card_index, hongzhongList.size() + 1)) {
            return true;
        }
        return HuUtil.isCanHu(copy, hongzhongList.size() + 1);

        // 拆将
//        if (chaijiang(card_index, copy, hongzhongList.size() + 1, false)) {
//            return true;
//        } else {
//            return false;
//        }
    }

    /**
     * 红中麻将没有7小对，所以不用红中补
     *
     * @param majiangIds
     * @param card_index
     */
    public static boolean check7duizi(List<DzMj> majiangIds, DaozMjIndexArr card_index, int hongzhongNum) {
        if (majiangIds.size() == 14) {
            // 7小对
            int duizi = card_index.getDuiziNum();
            if (duizi == 7) {
                return true;
            }

        } else if (majiangIds.size() + hongzhongNum == 14) {
            if (hongzhongNum == 0) {
                return false;
            }

            DaozMjIndex index0 = card_index.getMajiangIndex(0);
            DaozMjIndex index2 = card_index.getMajiangIndex(2);
            int lackNum = index0 != null ? index0.getLength() : 0;
            lackNum += index2 != null ? index2.getLength() : 0;

            if (lackNum <= hongzhongNum) {
                return true;
            }

            if (lackNum == 0) {
                lackNum = 14 - majiangIds.size();
                if (lackNum == hongzhongNum) {
                    return true;
                }
            }

        }
        return false;
    }

    // 拆将
    public static boolean chaijiang(DaozMjIndexArr card_index, List<DzMj> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<DzMj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<DzMj>> valEntry : jiangMap.entrySet()) {
            List<DzMj> copy = new ArrayList<>(hasPais);
            DaozMjHuLack lack = new DaozMjHuLack(hongzhongnum);
            List<DzMj> list = valEntry.getValue();
            int i = 0;
            for (DzMj majiang : list) {
                i++;
                copy.remove(majiang);
                if (i >= 2) {
                    break;
                }
            }
            lack.setHasJiang(true);
            boolean hu = chaipai(lack, copy, needJiang258);
            if (hu) {
                System.out.println(JacksonUtil.writeValueAsString(lack));
                return hu;
            }
        }

        if (hongzhongnum > 0) {
            // 只剩下红中
            if (hasPais.isEmpty()) {
                return true;
            }
            // 没有将
            for (DzMj majiang : hasPais) {
                List<DzMj> copy = new ArrayList<>(hasPais);
                DaozMjHuLack lack = new DaozMjHuLack(hongzhongnum);
                boolean isJiang = false;
                if (!needJiang258) {
                    // 不需要将
                    isJiang = true;

                } else {
                    // 需要258做将
                    if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                        isJiang = true;
                    }

                }
                if (isJiang) {
                    lack.setHasJiang(true);
                    lack.changeHongzhong(-1);
                    lack.addLack(majiang.getVal());
                    copy.remove(majiang);
                }

                boolean hu = chaipai(lack, copy, needJiang258);
                if (lack.isHasJiang() && hu) {
                    System.out.println(JacksonUtil.writeValueAsString(lack));
                    return true;
                }
                if (!lack.isHasJiang() && hu) {
                    if (lack.getHongzhongNum() == 2) {
                        // 红中做将
                        System.out.println(JacksonUtil.writeValueAsString(lack));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 拆牌
    public static boolean chaipai(DaozMjHuLack lack, List<DzMj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<DzMj> hasPais) {
        Collections.sort(hasPais, new Comparator<DzMj>() {

            @Override
            public int compare(DzMj o1, DzMj o2) {
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

    /**
     * 拆顺
     *
     * @param hasPais
     * @return
     */
    public static boolean chaishun(DaozMjHuLack lack, List<DzMj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        DzMj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<DzMj> minList = DaozMjQipaiTool.getVal(hasPais, minVal);
        if (minList.size() >= 3) {
            // 先拆坎子
            removeAllPai(hasPais, minList.subList(0, 3));
            //hasPais.removeAll(minList.subList(0, 3));
            return chaipai(lack, hasPais, needJiang258);
        }

        // 做顺子
        int pai1 = minVal;
        int pai2 = 0;
        int pai3 = 0;
        if (pai1 % 10 == 9) {
            pai1 = pai1 - 2;

        } else if (pai1 % 10 == 8) {
            pai1 = pai1 - 1;
        }
        pai2 = pai1 + 1;
        pai3 = pai2 + 1;

        List<Integer> lackList = new ArrayList<>();
        List<DzMj> num1 = DaozMjQipaiTool.getVal(hasPais, pai1);
        List<DzMj> num2 = DaozMjQipaiTool.getVal(hasPais, pai2);
        List<DzMj> num3 = DaozMjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<DzMj> hasMajiangList = new ArrayList<>();
        if (!num1.isEmpty()) {
            hasMajiangList.add(num1.get(0));
        }
        if (!num2.isEmpty()) {
            hasMajiangList.add(num2.get(0));
        }
        if (!num3.isEmpty()) {
            hasMajiangList.add(num3.get(0));
        }

        // 一句话缺少的麻将
        if (num1.isEmpty()) {
            lackList.add(pai1);
        }
        if (num2.isEmpty()) {
            lackList.add(pai2);
        }
        if (num3.isEmpty()) {
            lackList.add(pai3);
        }

        int lackNum = lackList.size();
        if (lackNum > 0) {
            if (lack.getHongzhongNum() <= 0) {
                return false;
            }

            // 做成一句话缺少2张以上的，没有将优先做将
            if (lackNum >= 2) {
                // 补坎子
                List<DzMj> count = DaozMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() >= 3) {
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);

                } else if (count.size() == 2) {
                    if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
                        // 没有将做将
                        lack.setHasJiang(true);
                        removeAllPai(hasPais, count);
                        //hasPais.removeAll(count);
                        return chaipai(lack, hasPais, needJiang258);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }

                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }
            } else if (lackNum == 1) {
                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(minMajiang, needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.remove(minMajiang);
                    lack.addLack(minMajiang.getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }

                List<DzMj> count = DaozMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                removeAllPai(hasPais, hasMajiangList);
                //hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<DzMj> count1 = DaozMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<DzMj> count2 = DaozMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<DzMj> count3 = DaozMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<DzMj> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    DaozMjHuLack copyLack = lack.copy();
                    copyLack.changeHongzhong(-1);

                    copyLack.addLack(hasMajiangList.get(0).getVal());
                    if (chaipai(copyLack, copy, needJiang258)) {
                        return true;
                    }
                }
            }
            removeAllPai(hasPais, hasMajiangList);
            //hasPais.removeAll(hasMajiangList);
        }
        return chaipai(lack, hasPais, needJiang258);
    }

    public static void removeAllPai(List<DzMj> hasPais, List<DzMj> remPai) {
        for (DzMj majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(DzMj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }	

    public static List<DzMj> checkChi(List<DzMj> majiangs, DzMj dismajiang,int wangVal) {
    	List<DzMj> res = checkChi(majiangs, dismajiang, null);
    	if(res.isEmpty()&& wangVal<200){//&&!haveWang(majiangs, wangVal)
    		List<DzMj> mjs = new ArrayList<DzMj>();
    		mjs.addAll(majiangs);
    		if(haveHongzhong(majiangs)>0){
    			mjs.add(DzMj.getMajangVal2(wangVal));
    		}
    		
    		if(dismajiang.isHongzhong()){
    			dismajiang = DzMj.getMajangVal2(wangVal);
    		}
    		
    		res = checkChi(mjs, dismajiang, null);
    	}
        return res;
    }
    
    public static HashMap<Integer,Integer>  getMingTangFans(DzMj huMj,DaozMjPlayer player,DaozMjTable table,boolean zimo){
    	
    	HashMap<Integer,Integer> mingTangs = new HashMap<>();
    	
    	if(!player.isChiPengGangNoAgang()&&zimo){
    		mingTangs.put(DaoZMingTang.Men_Qing, 2);
    	}
    	
    	if(zimo&&player.getHandPais().size()<=2&&player.getaGang().isEmpty()){
    		mingTangs.put(DaoZMingTang.Quan_Qiu_Ren, 2);
    	}else if(player.getHandPais().size()==1&&player.getaGang().isEmpty()){
    		mingTangs.put(DaoZMingTang.Quan_Qiu_Ren, 2);
    	}
    	List<DzMj> allMajiangs = new ArrayList<>();
		allMajiangs.addAll(player.getHandMajiang());
		allMajiangs.addAll(player.getGang());
		allMajiangs.addAll(player.getPeng());
		allMajiangs.addAll(player.getChi());
//    	int hunyise = hunYiSe(allMajiangs, table.getWangMjVal());
//    	

//    	
//    	if(hunyise==1){
//    		mingTangs.put(DaoZMingTang.Ruan_Hun_Yi_Se, zimo?2:3);
//    	}else if(hunyise==2){
//    		mingTangs.put(DaoZMingTang.Ying_Hun_Yi_Se, zimo?2:3);
//    	}
        if(huMj!=null&&!zimo){
        	allMajiangs.add(huMj);
        //	allMajiangs.add(table.getNowDisCardIds().get(0));
        }
        
		
//    	
    	int[] infos = getAllMjInfo(new ArrayList<>(allMajiangs), table.getWangMjVal(),player,huMj,table,zimo);
    	
    	boolean isDaDao = false;
    	boolean isXiaoDao =false;
    	if(infos[4]==0&&infos[5]==0&&infos[2]<2){
    		isDaDao = true;
    	}
    	else if(infos[5]>0&&infos[2]==0){
    		isXiaoDao= true;
    	}
    	
    	
    	int qingyiseX = getQingYiseX(infos, isXiaoDao);
    	//混一色
        
         if(infos[8]==1&&infos[7]==0||(qingyiseX>0)){
        	mingTangs.put(DaoZMingTang.Ying_Qing_YI_Se, 8);

    	}else if(infos[8]==2&&table.getHunYiSe()==1&&infos[7]==1){
        	
        	
        	mingTangs.put(DaoZMingTang.Ruan_Hun_Yi_Se, 2);
        	
//        	if(infos[2]==0&&infos[5]==0){
//        		mingTangs.put(DaoZMingTang.Ying_Hun_Yi_Se, zimo?8:12);
//        	}else {
//        		if(isXiaoDao){
//        			
//        		}
//        		mingTangs.put(DaoZMingTang.Ruan_Hun_Yi_Se, zimo?2:3);
//        	}
        	
    	}//清一色
        
//        else if(){
//    		mingTangs.put(DaoZMingTang.Qing_YI_Se_Xiao, zimo?8:12);
//    		
//    	}else if(qingyiseX==2){
//    		mingTangs.put(DaoZMingTang.Ruan_Qing_YI_Se, zimo?4:6);
//    		
//    	}
        
        if(infos[8]==1&&infos[7]==1){
//        	if(isDaDao){
//        		mingTangs.put(DaoZMingTang.Feng_Yi_Se, zimo?16:24);
//        	}else{
        		mingTangs.put(DaoZMingTang.Feng_Yi_Se_Ruan,2);
//        	}
        }
        
        
        
        if(infos[0]==1){
        	mingTangs.put(DaoZMingTang.Dan_Wang_Zhua, 2);
        }else if(infos[0]==2){
        	mingTangs.put(DaoZMingTang.Dan_Wang_Zhua_Wang, 4);
        }else if(infos[1]==1){
        	mingTangs.put(DaoZMingTang.Shuang_Wang_Zhua, 4);
        }else if(infos[1]==2){
        	mingTangs.put(DaoZMingTang.Shuang_Wang_Zhua_Wang, 8);
        } else if(isXiaoDao&&table.getXiaoDao()==1){
        	mingTangs.put(DaoZMingTang.Xiao_Dao, 2);
        }else  if(isDaDao&&table.getDaDao()==1){
        	mingTangs.put(DaoZMingTang.Da_Dao, 4);
        }
        
        
        if( player.getHuType().contains(DaozMjConstants.GANG_SHANG_HUA)){
        	mingTangs.put(DaozMjConstants.GANG_SHANG_HUA, 2);
        }
        
        if( player.getHuType().contains(DaozMjConstants.HU_QIANGGANGHU)){
        	mingTangs.put(30, 1);
        }
        
        if(infos[10]==1){
        	mingTangs.put(DaoZMingTang.Yi_Tiao_Long, 2);
        }
        if(infos[11]==1){
        	mingTangs.put(DaoZMingTang.YOU_ER_TAO, 4);
        }
        else if(infos[9]==1){
        	mingTangs.put(DaoZMingTang.YOU_YI_TAO, 2);
        }
        
        
        
        if(pengpengHu(player, table.getWangMjVal(),huMj,zimo,infos)){
        	mingTangs.put(DaoZMingTang.Ruan_Peng_Peng_Hu, 2);
        	mingTangs.remove(DaoZMingTang.YOU_ER_TAO);
        	mingTangs.remove(DaoZMingTang.YOU_YI_TAO);
//        	if(!isDaDao){
//        		
//        	}else{
//        		mingTangs.put(DaoZMingTang.Ying_Peng_Peng_Hu, zimo?8:12);
//        	}
        }
    	
   	 //十三浪
        int lang = DaozMjTool.shiSanLang(allMajiangs);
       if(!player.isChiPengGang()&&lang>0){
       	mingTangs.put(DaoZMingTang.Shi_San_Lang,2);
       	if(lang==2){
       		mingTangs.put(DaoZMingTang.QI_Feng_Dao_Wei,2);
       	}
       	mingTangs.remove(DaoZMingTang.Da_Dao);
    	mingTangs.remove(DaoZMingTang.Men_Qing);
       	isDaDao = false;
       }
       
        //只有红中
        if(infos[2]>0&&infos[4]<infos[2]&&!isDaDao){
        	int nuRep = infos[2]-infos[4];//没有替牌的
        	int nuRep2 = infos[2];
        	if(mingTangs.containsKey(DaoZMingTang.Dan_Wang_Zhua)){
        		nuRep2 -=1;
        	}else if(mingTangs.containsKey(DaoZMingTang.Dan_Wang_Zhua_Wang)||mingTangs.containsKey(DaoZMingTang.Shuang_Wang_Zhua)){
        		nuRep2 -=2;
        	}else if(mingTangs.containsKey(DaoZMingTang.Shuang_Wang_Zhua_Wang)){
        		nuRep2 -=3;

        	}
        	
        	if(nuRep2>0){
        		if(nuRep==1){
    				mingTangs.put(DaoZMingTang.Wang_Gui_Wei, 2);
                    //如果沒有一句話那就不算王归位,十三浪除外
                    if(lang==0&&(player.getChi().isEmpty()||!hasWang(player.getChi(),table.getWangMjVal()))){
                        List<DzMj> copy = new ArrayList<>(player.getHandMajiang());
                        if(!zimo){
                            copy.add(huMj);
                        }
                        List<DzMj> wangs = dropWangBa(copy,table.getWangMjVal());
                        if(!wangs.isEmpty()){
                            List<DzMj> chiList =  checkChi(copy,wangs.get(0),null);
                            if(chiList.isEmpty()){
                                mingTangs.remove(DaoZMingTang.Wang_Gui_Wei);
                            }
                        }

                    }
    			}else if(nuRep==2){
    				mingTangs.put(DaoZMingTang.SHUANG_WANGGUI_WEI, 4);
    			}else if(nuRep==3){
    				mingTangs.put(DaoZMingTang.San_WANGGUI_WEI, 8);
    			}else if(nuRep==4){
    				mingTangs.put(DaoZMingTang.Si_WANGGUI_WEI, 16);
    			}
        		
        	}
        	
        }
        
        
        //七对
        List<DzMj> copy = new ArrayList<>(player.getHandMajiang());
        if (copy.size() % 3 != 2&&!zimo) {
        	copy.add(table.getNowDisCardIds().get(0));
        }
        

        int rapWang = getWangzhuaRap(table.getWangMjVal(), huMj, infos, copy);
		dropWangBa(copy, table.getWangMjVal());
		boolean qidui = checkHuQdui(table, copy, infos[2]-rapWang, infos[3]);
        
        // 先去wang
//        List<DzMj> wangs = dropWangBa(copy,table.getWangMjVal());
//        int hzCount = DaozMjTool.haveHongzhong(copy);
//        boolean qidui = checkHuQdui(table, copy, wangs.size(), hzCount);
        

        
        
    	DaozMjIndexArr card_index = new DaozMjIndexArr();
        DaozMjQipaiTool.getMax(card_index, allMajiangs);
        DaozMjIndex index4 = card_index.getMajiangIndex(3);
        if(qidui&&mingTangs.get(DaoZMingTang.Ruan_Peng_Peng_Hu)==null){
        	 List<DzMj> copy2 = new ArrayList<>(player.getHandMajiang());
             if (!zimo) {
            	 copy2.add(table.getNowDisCardIds().get(0));
             }
             
             int repWang = getWangzhuaRap(table.getWangMjVal(), huMj, infos, copy2);
        	boolean canhu = isHuNew(copy2, false, table,false,repWang);
        	
        	
        	//只能胡七对
        	if(!canhu||(!mingTangs.containsKey(DaoZMingTang.Wang_Gui_Wei)&&!mingTangs.containsKey(DaoZMingTang.SHUANG_WANGGUI_WEI))){
        		//有两套，自摸
        		if(mingTangs.containsKey(DaoZMingTang.YOU_ER_TAO)){
        			if(!zimo){
        				addQiduiMingt(mingTangs);
        			}
        		}else{
        			addQiduiMingt(mingTangs);
        		}
        		
        		
        	}
        	
//        	//能不能普通胡
//        	if(!mingTangs.containsKey(DaoZMingTang.Wang_Gui_Wei)&&!mingTangs.containsKey(DaoZMingTang.SHUANG_WANGGUI_WEI)){
//        		
//        	}
//        	if(mingTangs.containsKey(DaoZMingTang.YOU_ER_TAO)){
//        		mingTangs.remove(DaoZMingTang.Qi_Dui_Hu);
//        	}else{
//        		mingTangs.remove(DaoZMingTang.YOU_YI_TAO);
//        	}
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
//        	if(index4!=null||){
//        		
//        	}
        	
        	
//            if(!isDaDao){
//            	if(index4!=null){
//            		mingTangs.put(DaoZMingTang.Long_Qiao_Dui, zimo?4:6);
//            	}else{
//            		mingTangs.put(DaoZMingTang.Qi_Dui_Hu, zimo?2:3);
//            	}
//        		
//        	}else {
//        		if(index4!=null){
//        			mingTangs.put(DaoZMingTang.Ying_Long_Qiao_Dui, zimo?16:24);
//            	}else{
//            		mingTangs.put(DaoZMingTang.Ying_Qiao_Dui, zimo?8:12);
//            	}
//        	}
//            mingTangs.remove(DaoZMingTang.Men_Qing);
        }
        
        
        

    	
    	if(index4!=null){
    		Map<Integer, List<DzMj>> mj4 = index4.getMajiangValMap();
    		if(mj4.get(table.getWangMjVal())!=null){
    			List<DzMj> mjfo = 	mj4.get(table.getWangMjVal());
    			boolean  siwangDaow  = true;
    			for(DzMj mj: mjfo){
    				if(player.getaGang().contains(mj)){
    					siwangDaow = false;
    					break;
    				}
    			}
    			if(siwangDaow){
    				mingTangs.put(DaoZMingTang.Si_Wang_Dao_Wei, 2);
    			}
    			//mingTangs.remove(DaoZMingTang.Wang_Gui_Wei);
    		}
    		int siguiyi = 0;
    		for(Entry<Integer, List<DzMj>> entry: mj4.entrySet()){
    			int  mjVal = entry.getKey();
    			if(!containsMj(player.getGang(), mjVal)&& mjVal!=table.getWangMjVal()){
    				siguiyi++;
    			}
    			
    		}
    		
    		if(siguiyi>0){
    			if(siguiyi==1){
    				mingTangs.put(DaoZMingTang.Si_Gui_Yi, 2);
    			}else if(siguiyi==2){
    				mingTangs.put(DaoZMingTang.Si_Gui_Yi_X2, 4);
    			}else if(siguiyi==3){
    				mingTangs.put(DaoZMingTang.Si_Gui_Yi_X3, 8);
    			}
    			
    		}
    		
    	}
    
    	
    	return mingTangs;
    }

	private static void addQiduiMingt(HashMap<Integer, Integer> mingTangs) {
		mingTangs.put(DaoZMingTang.Qi_Dui_Hu, 2);
		mingTangs.remove(DaoZMingTang.Wang_Gui_Wei);
		mingTangs.remove(DaoZMingTang.SHUANG_WANGGUI_WEI);
		mingTangs.remove(DaoZMingTang.Men_Qing);
	}
    
    
    
    
    
    
    public static int getQingYiseX(int[] infos,boolean xiaoDao){
    	
    	if(infos[6]>0||infos[8]>2||infos[3]!=infos[5]){
    		return 0;
    	}
    	
    	//没有王，那花色最多只能两种 有两种一定是小道，并且所有都要红中替换
    	if(infos[2]==0&&infos[8]<=2&&xiaoDao){
    		if(infos[3]==infos[5])
    		return 1;//清一色 小道
    	}
    	
    	//有王 那最多三种花色
    	if(infos[2]>0){
    		//王替换别的牌了
    		if(infos[8]==1){
    			return 2;//软清一色
    		}else if(infos[8]==2){
    			//有红中只有两花色，算清一色，又有王（王替代本花色）
    			if(infos[3]>0){
    				return 2;
    			}
    		}
    		
    	}
    	
    	
    	
    	
    	return 0;
    	
    	
    }
    
    
    public static int shiSanLang(List<DzMj> majiangs){
    	HashSet<Integer> fengPais = new HashSet<Integer>();
    	 HashMap<Integer,List<Integer>> twsMjs = new HashMap<>();
    	 int fengCount = 0;
    	for (DzMj majiang : majiangs) {
            if (majiang.isFeng()) {
            	fengCount++;
            	fengPais.add(majiang.getVal());
            }else{
            	List<Integer> mjs = twsMjs.get(majiang.getColourVal());
            	if(mjs==null){
            		mjs = new ArrayList<>();
            			twsMjs.put(majiang.getColourVal(), mjs);
            	}
            	mjs.add(majiang.getVal()%10);
            	
            	if(mjs.size()>3){
            		return 0;
            	}
            }
        }
    	
    	if(fengPais.size()<4||fengPais.size()<fengCount){
    		return 0;
    	}
    	
    	for(Entry<Integer,List<Integer>> entry: twsMjs.entrySet()){
    		List<Integer> mjs = entry.getValue();
    		if(!checkMjbetween(mjs)){
    			return 0;
    		}
    	}
    	
    	
    	if(fengCount==7){
    		return 2;
    	}
    	
    	return 1;
    }
    
    
    public static boolean pengpengHu(DaozMjPlayer player,int wangVal,DzMj huMj,boolean zimo,int[] infos){
    	//||player.getGang().size()>0
    	if(player.getChi().size()>0){
    		return false;
    	}
    	
    	List<DzMj> handMjs = new ArrayList<>(player.getHandMajiang());
    	
    	
    	int repWang = getWangzhuaRap(wangVal, huMj, infos, handMjs);
    	
    	if(!zimo){
    		handMjs.add(huMj);
    	}
    	handMjs.addAll(player.getPeng());
    	handMjs.addAll(player.getGang());
    	
    	int wangCount  = dropWangBa(handMjs, wangVal).size();
//    	
//    	DaozMjIndexArr card_index = new DaozMjIndexArr();
//        DaozMjQipaiTool.getMax(card_index, handMjs);
//        DaozMjIndex index3 = card_index.getMajiangIndex(2);
//    	
//        DaozMjIndex index2 = card_index.getMajiangIndex(1);
//        DaozMjIndex index1 = card_index.getMajiangIndex(0);
//        
//        
//        if(index3!=null){
//        	 List<DzMj> mj3s =  index3.getMajiangs();
//             
//             for(DzMj mj: mj3s){
//             	handMjs.remove(mj);
//             }
//        }
//       
        
        return isPengPengHu(handMjs,wangCount);
        
    	
    	
    }
    
    
    
    private static boolean checkMjbetween(List<Integer> mjs){
    	if(mjs.size()<2){
    		return true;
    	}
    	if(mjs.contains(3)&&(mjs.contains(7)||mjs.contains(8))  ||  (mjs.contains(2)&&mjs.contains(7))){
    		return false;
    	}
    	Collections.sort(mjs);
    	for (int i=0;i<mjs.size()-1;i++) {
    		if(mjs.get(i+1)-mjs.get(i)<3){
    			return false;
    		}
    		
        }
    	return true;
    	
    }
    
    
    private static boolean containsMj(List<DzMj> majiangs,int val){
    	for (DzMj majiang : majiangs) {
            if (majiang.getVal()==val) {
//                return true;
            	return true;
            }
        }
    	return false;
    }
    
    
    public static int haveHongzhong(List<DzMj> majiangs) {
    	int count = 0;
        for (DzMj majiang : majiangs) {
            if (majiang.isHongzhong()) {
//                return true;
            	count++;
            }
        }
        return count;
    }
    
    
    
    
    
    private static int[] getAllMjInfo(List<DzMj> majiangs,int wangVal,DaozMjPlayer player,DzMj huMj,DaozMjTable table,boolean zimo){

    	int[] infos = getHandMjsInfo(majiangs, wangVal, player, huMj, table, zimo);
		//long time1 = System.currentTimeMillis();
		int[] res =youYiTao2(player, infos, wangVal, table, huMj, zimo);
		
		
		
		//long time2 = System.currentTimeMillis();
		
		//System.out.println("算一条龙 有一套时间==========================================================" +(time2-time1));
		
		
		
		//int[] ytao =  youYiTao(majiangs, infos, wangVal, table, huMj);
//		if(res[1]!=1){
			infos[9] = res[0];
//		}
		infos[10] = res[1];
		infos[11] = res[2];
    	return infos;
    	
    }

	private static int[] getHandMjsInfo(List<DzMj> majiangs, int wangVal, DaozMjPlayer player, DzMj huMj,
			DaozMjTable table, boolean zimo) {
		int[] infos = new int[12];
    	
//    	List<Integer> infos = new ArrayList<>(16);
    	int[] actionArr = new int[2];
    	HashSet<Integer> colors = new HashSet<Integer>();
    	//除了红中之外的风牌
    	boolean hasZi = false;
    	//是否有风牌
    	boolean haseFeng = false;
    	
    	for (DzMj majiang : majiangs) {
        	if(majiang.isFeng()){
        		if(majiang.getVal()!=wangVal){
        			colors.add(4);
        			haseFeng  =true;
            		if(!majiang.isHongzhong()){
            			hasZi =true;
            		}
        		}
        	}else{
        		//王不算入花色，如果王代替本身，那一定有它本色的花色
        		if(majiang.getVal()!=wangVal){
        			colors.add(majiang.getColourVal());
        		}
        	}
        	if(majiang.getVal()==wangVal){
        		actionArr[0] +=1;
        	}else if(majiang.isHongzhong()){
        		actionArr[1] +=1;
        	}
        	
        }
    	
    	
    	
    	if(!player.getPeng().isEmpty()){
    		//检查王有没有碰
    		boolean wangPeng = false;
    		for(DzMj majiang : player.getPeng()) {
    			if(majiang.getVal()==wangVal){
    				wangPeng = true;
    				break;
    			}
    		}
    		if(wangPeng){
    			actionArr[0] -=3;
    		}
    	}
    	
    	if(!player.getGang().isEmpty()){
    		//检查王有没有杠
    		boolean wangGang = false;
    		for(DzMj majiang : player.getGang()) {
    			if(majiang.getVal()==wangVal){
    				wangGang = true;
    				break;
    			}
    		}
    		if(wangGang){
    			actionArr[0] -=4;
    		}
    	}
    	
    	
    	
    	
    	//王爪牌型
		if (player.getWangzState() == 1 && actionArr[0] > 0) {
			if (actionArr[0] == 1) {
				// 单王爪1 爪王2
				infos[0] = huMj.getVal() == wangVal ? 2 : 1;
			} else {
				//
				List<DzMj> handmjs = new ArrayList<>(player.getHandMajiang());
				int i = 0;
				for (DzMj mj : player.getHandMajiang()) {
					if (mj.getVal() == wangVal) {
						i++;
						handmjs.remove(mj);
						if (i == 2) {
							break;
						}
					}
				}
				handmjs.remove(huMj);
				// 不要双王也能胡，双王爪
				boolean huflag = isHu(handmjs, table);
				if (huflag) {
					infos[1] = huMj.getVal() == wangVal ? 2 : 1;
				} else {
					infos[0] = huMj.getVal() == wangVal ? 2 : 1;
				}
			}
		} 
		
		
		
    	int hzRepCount = 0;
		if(!player.getChi().isEmpty()){
			for(DzMj mj: player.getChi()){
				if(mj.isHongzhong()){
					hzRepCount+=1;
				}
//				break;
			}
		}
		
		
		
		//王不是自摸胡的，不算王
    	if(huMj.getVal()==wangVal&&!zimo){
    		//actionArr[0] -=1;
    	}
    	
    	
    	
    	
    	List<DzMj> copy = new ArrayList<>(player.getHandMajiang());
    	if(!zimo){
    		copy.add(huMj);
    	}
    	
    	List<DzMj> dropMjs = new ArrayList<>();
    	int rapWang = getWangzhuaRap(wangVal, huMj, infos, copy);
    	
		int wangRep = isHuCalM(copy, table,actionArr[0]-rapWang);

		
		hzRepCount +=table.getHuHzRepCount();
		
		
    	//王的个数
		infos[2] = actionArr[0];
		//红中个数
		infos[3] = actionArr[1];
		//王替代
		infos[4] = wangRep+rapWang;
		//红中替代的牌
		infos[5] = hzRepCount;
		//有字
		infos[6] =hasZi?1:0;
		//有风
		infos[7] =haseFeng?1:0;
		//花色
		infos[8] =colors.size();
		return infos;
	}

	private static int getWangzhuaRap(int wangVal, DzMj huMj, int[] infos, List<DzMj> copy) {
		int rapWang= 0;
		if(infos[0]>0||infos[1]>0){
			if(infos[0]==1){
				int wangRepVal = getWangRepMjVal(1, huMj.getVal(), copy);
				dropWangByCout(copy, wangVal, 1);
				if(wangRepVal!=huMj.getVal()){
					copy.remove(huMj);
					copy.addAll(DzMj.getSameMjList(2,wangRepVal, copy));
				}else{
					copy.addAll(DzMj.getSameMjList(1,huMj.getVal(), copy));
				}
				
//				dropMjs.addAll();
				rapWang =1;
				//dropWangByCout
			}else if(infos[0]==2||infos[1]==1){
				//双王爪
				if(infos[1]==1){
					int wangRepVal = getWangRepMjVal(2, huMj.getVal(), copy);
					dropWangByCout(copy, wangVal, 2);
					if(wangRepVal!=huMj.getVal()){
						copy.remove(huMj);
						copy.addAll(DzMj.getSameMjList(3,wangRepVal, copy));
					}else{
						copy.addAll(DzMj.getSameMjList(2,huMj.getVal(), copy));
					}
				
				}else if(infos[0]==2){
					int wangRepVal = getWangRepMjVal(2, huMj.getVal(), copy);
					dropWangByCout(copy, wangVal, 2);
					copy.addAll(DzMj.getSameMjList(2,wangRepVal, copy));
					
				}
				//dropMjs.addAll();
				rapWang =2;
			}else if(infos[1]==2){
				
				int wangRepVal = getWangRepMjVal(2, huMj.getVal(), copy);
				dropWangByCout(copy, wangVal, 3);
				copy.addAll(DzMj.getSameMjList(3,wangRepVal, copy));
				
				//dropMjs.addAll(dropWangByCout(copy, wangVal, 3));
				rapWang =3;
			}
		}
		return rapWang;
	}
    
    
	
	  private static int[]  youYiTao2(DaozMjPlayer player,int[] infos,int wangVal,DaozMjTable table,DzMj huMj,boolean zimo) {
		  int[] res = new int[3];
		  List<DzMj> copy = new ArrayList<>(player.getHandMajiang());
	    	if(player.getChi().isEmpty()){
	    		if(!zimo){
	    			copy.add(huMj);
	    		}
	    		
	    		 List<DzMj> copy2 = new ArrayList<>(player.getHandMajiang());
	    		 if(!zimo){
	    			 copy2.add(huMj);
		    	}
	    		int repWang = getWangzhuaRap(table.getWangMjVal(), huMj, infos, copy2);
	    		
	    		 List<DzMj> copy3 = new ArrayList<>(copy2);
	    		dropWangBa(copy2, wangVal);
	    		boolean qidui = checkHuQdui(table, copy2, infos[2]-repWang, infos[3]);
	    		boolean pingHu = isHu(copy3, false, table,false);
	    		//int guiwei = infos[2]-infos[4];
	    		if(qidui){
	    			//能胡王归位的平胡
	    			if(!pingHu){
	    				 getWangzhuaRap(table.getWangMjVal(), huMj, infos, copy);
	    				return youYiTao(copy, infos, wangVal, table, huMj);
	    			}
	    			
	    		}
	    	}
		  
	    	List<DaozMjCardDisType> chiList = player.getCardTypesByAction(DaozMjDisAction.action_chi);
	    	
	    	HashMap<Integer,List<Integer>> yijuhua = new HashMap<>();
	    	HashSet<Integer> colors = new HashSet<>();
		  for(DaozMjCardDisType type: chiList){
			  DzMj mj = DzMj.getMajang( type.getCardIds().get(0));
			  if(mj.isHongzhong()){
				  mj = DzMj.getMajangVal2(wangVal);
			  }
			  
			  List<Integer> typeIds =  yijuhua.get(mj.getColourVal());
			  colors.add(mj.getColourVal());
			  if(typeIds==null){
				  typeIds =new ArrayList<>();
				  yijuhua.put(mj.getColourVal(), typeIds);
			  }
			  int chiType = getChiType(type.getCardIds(),wangVal);
//			  if(!typeIds.contains(chiType)){
				  typeIds.add(chiType);
//			  }
		  }
		  
		  
		  
		  int hzRepCount = 0;
			if(!player.getChi().isEmpty()){
				for(DzMj mj: player.getChi()){
					if(mj.isHongzhong()){
						hzRepCount+=1;
					}
//					break;
				}
			}
		  
		  
		  
		  boolean ytl= false;
		  int hzco = infos[5]-hzRepCount;
		  int taoNum =0;
		for (Map.Entry<Integer, List<Integer>> entry : yijuhua.entrySet()) {
			int color = entry.getKey();
			if (!ytl) {
				ytl = checkYTL(entry.getValue(), color, player.getHandMajiang(), infos, table, huMj, zimo, hzco);
			}
			int getTaoRes = checkYyeTao(new ArrayList<Integer>(entry.getValue()), color, player.getHandMajiang(), infos, table, huMj, hzco,
					zimo, true);
			if (getTaoRes == 0) {
				getTaoRes = checkYyeTao(entry.getValue(), color, player.getHandMajiang(), infos, table, huMj, hzco,
						zimo, false);
			}
			taoNum += getTaoRes;

		}

		if (!ytl) {
			for (int i = 1; i <= 3; i++) {
				if (colors.contains(i)) {
					continue;
				}
				if (!ytl) {
					ytl = checkYTL(new ArrayList<Integer>(), i, player.getHandMajiang(), infos, table, huMj, zimo,
							hzco);
				}
				int getTaoRes = checkYyeTao(new ArrayList<Integer>(), i, player.getHandMajiang(), infos, table, huMj,
						hzco, zimo, true);
				if (getTaoRes == 0) {
					getTaoRes = checkYyeTao(new ArrayList<Integer>(), i, player.getHandMajiang(), infos, table, huMj,
							hzco, zimo, false);
				}

				taoNum += getTaoRes;
				// if(taoNum>0){
				// res[0] =taoNum;
				// }
			}
		}
		  	if(taoNum==1){
				 res[0] =1;
			 }else 	if(taoNum>=2){
				 res[2] =1;
			 }
		  
		  res[1] = ytl?1:0;
		  
//		 int[] ytao =  youYiTao(copy, infos, wangVal, table, huMj);
//		  
//		  
//		  if(res[1]!=1){
//			  res[0] = ytao[0];
//		  }
//		  res[2] = ytao[2];
		  
	    	return res;
	    }
	
	  
	  
	  
	  
	  
	  private static boolean checkYTL(List<Integer> cardIds,int color,List<DzMj> mjs,int[] infos,DaozMjTable table,DzMj huMj,boolean zimo,int hzR){
	  
		  List<DzMj> copy = new ArrayList<>(mjs);
		  if(!zimo){
			  copy.add(huMj);
		  }
		  List<Integer> ytlTypes = new ArrayList<>();
		  List<Integer> commTypes = new ArrayList<>();
		  commTypes.add(1);
		  commTypes.add(4);
		  commTypes.add(7);
		
		  for(Integer type:cardIds){
			  if(type==1||type==4||type==7){
				  if(!ytlTypes.contains(type))
				  ytlTypes.add(type);
			  }
		  }
		  //一条龙全部吃下去
		  if(ytlTypes.size()==3){
			  return true;
		  }
		  
		  //需要从手上拿的牌
		  commTypes.removeAll(ytlTypes);
		  List<DzMj> colorMjs = getMjsByColor(copy, color,table.getWangMjVal(),hzR,infos[2]-infos[4]);
		  
		  List<DzMj> ytl = new ArrayList<>();
		  for(Integer type: commTypes){
			  List<DzMj> yitlMjs = getHandYTLMjs(colorMjs, type);
			  if(yitlMjs.size()<3){
				  return false;
			  }
			  ytl.addAll(yitlMjs);
			  
		  }
		  
		  copy.removeAll(ytl);

		  DzMj wangPai = DzMj.getMajangVal2(table.getWangMjVal());
		  if(wangPai!=null&&wangPai.getColourVal()==color){
              for(int i=0;i<hzR;i++){
                  dropAssignMj(copy, 201);
              }
          }

		  int count = getWangCounts(copy, table.getWangMjVal());
  		int rapWang = getWangzhuaRap(table.getWangMjVal(), huMj, infos, copy);
  		
		boolean yitiaoLong = checkyiTiaoLong(copy, count-rapWang, table);
	  
		return yitiaoLong;
	  
	  
	  }
	  
	  
	  
	  
	  
	  private static int checkYyeTao(List<Integer> cardIds,int color,List<DzMj> mjs,int[] infos,DaozMjTable table,DzMj huMj,int hzr,boolean zimo,boolean zheng){
		  
		  List<DzMj> copy = new ArrayList<>(mjs);
		  if(!zimo){
			  copy.add(huMj);
		  }
//		  List<Integer> ytlTypes = new ArrayList<>();
		  List<Integer> commTypes =  getYEtaoCommList(zheng);
//		  ytlTypes.addAll(cardIds);
		  
		  
		 int yitaoNum =  getYEtao(cardIds);
		  
		  //需要从手上拿的牌
		  //commTypes.removeAll(ytlTypes);
		  
		  List<DzMj> colorMjs = getMjsByColor(copy, color,table.getWangMjVal(),hzr,infos[2]-infos[4]);
		  List<DzMj> ytl = new ArrayList<>();
		  if(infos[0]>0||infos[1]>0){
			  colorMjs.remove(huMj);
		  }
		  
		  List<DzMj> wangs = DzMj.getWangMj(table.getWangMjVal(), copy);
		  
		  //一边加入牌一边检测有一套
		  for(Integer type: commTypes){
			  List<DzMj> yitlMjs = getHandYTLMjs(colorMjs, type);
			  if(yitlMjs.size()<3){
				  continue;
			  }

			  //红中替代的王优先，如果手上有王的话
              checkWangYjhRep(yitlMjs,colorMjs,wangs);
			  ytl.addAll(yitlMjs);
			  //检查有一套
			  cardIds.add(type);
			  int reHzCount = 0;
			  //有红中替代的王
			  if(hzr>0&&hasWangYjh(yitlMjs, table.getWangMjVal(),wangs)){
				  reHzCount=1;
			  }
			  
			  boolean canHu = checkYYTET(infos, table, huMj, reHzCount, copy, ytl);
			//能胡才算，不能胡需要放回从新拿
			  if(canHu){
				 int yitaoNum2 = getYEtao(cardIds);
				  if(yitaoNum2>yitaoNum){
					  yitaoNum=yitaoNum2;
				  }else{
					  //如果不能组成有一套，那看下一个能不能组成，
					  if(type>1){ 
						  int type2 = type-1;
						  List<DzMj> yitlMjs2 = getHandYTLMjs(colorMjs, type2);
						  if(yitlMjs2.size()==3){
							 // cardIds.add(type2);
							  List<DzMj> ytl2 = new ArrayList<>(ytl);
							  ytl2.removeAll(yitlMjs);
							  ytl2.addAll(yitlMjs2);
							  boolean canHu2 = checkYYTET(infos, table, huMj, reHzCount, copy, ytl2);
							  if(canHu2){
								  cardIds.remove((Integer)type);
								  cardIds.add(type2);
								  int yitaoNum3 = getYEtao(cardIds);
								  if(yitaoNum3>yitaoNum){
									  yitaoNum=yitaoNum3;
								  }
								  ytl=ytl2;
								  yitlMjs = yitlMjs2;
							  }
						  }
					  }
					  
				  }
				  if(reHzCount>0){
					  hzr-=1;
					  dropAssignMj(copy, 201);
				  }
				  copy.removeAll(ytl);
				  colorMjs.removeAll(yitlMjs);
			  }else{
				  ytl.removeAll(yitlMjs);
				  cardIds.remove((Integer)type);
			  }
			  
			  
		  }
		  
	  
		return yitaoNum;
	  
	  
	  }



    private static  void checkWangYjhRep(List<DzMj> yitlMjs,List<DzMj> colorMjs,List<DzMj> wangs){

        DzMj repMj =null;
        DzMj wangMj =null;
        for(DzMj mj: yitlMjs){
            if(wangs.contains(mj)){
                wangMj = mj;
                repMj =  getOtherYJHWang(colorMjs,wangs);
                break;
            }
        }

        if(repMj!=null&&wangMj!=null){
            yitlMjs.remove(wangMj);
            yitlMjs.add(repMj);
        }

    }

    private static  DzMj getOtherYJHWang(List<DzMj> colorMjs,List<DzMj> wangs){
        DzMj repMj =null;
        int wangVal = wangs.get(0).getVal();
        for(DzMj mj: colorMjs){
            if(mj.getVal()==wangVal&&!wangs.contains(mj)){
                repMj = mj;
                break;
            }
        }
        return repMj;
    }







	  private static  boolean hasWangYjh(List<DzMj> yitlMjs,int wangVal,List<DzMj> wangs){
		  for(DzMj mj: yitlMjs){
			  if(mj.getVal()==wangVal&&!wangs.contains(mj)){
				  return true;
			  }
		  }
		  return false;
	  }

    private static  boolean hasWang(List<DzMj> yitlMjs,int wangVal){
        for(DzMj mj: yitlMjs){
            if(mj.getVal()==wangVal){
                return true;
            }
        }
        return false;
    }

	  
	  
	  private static int getYEtao(List<Integer> ytlTypes){
		  if(ytlTypes.size()<2){
			  return 0;
		  }
		  
		  List<Integer> types = new ArrayList<>(ytlTypes);
		  Collections.sort(types);
		  int taoNum=0;
		  for(int i=0;i<2;i++){
			  int size = types.size();
			  List<Integer> taoList = new ArrayList<>();
			  for(int j=0;j<size;j++){
				  if(j<size-1){
					 int type1=  types.get(j);
					 int type2 = types.get(j+1);
					 if(type1==type2||type2-type1==1){
						 taoNum++;
						 taoList.add(type1);
						 taoList.add(type2);
						 break;
					 }
					 
				  }
			  }
			  types.removeAll(taoList);
			  
		  }
		  
		  
		  return taoNum;
	  }

	private static boolean  checkYYTET(int[] infos, DaozMjTable table, DzMj huMj, int hzr, List<DzMj> copy,
			List<DzMj> ytl) {
		List<DzMj> copy2 = new ArrayList<>(copy);
		  copy2.removeAll(ytl);
			 
		  for(int i=0;i<hzr;i++){
			  dropAssignMj(copy2, 201);
		  }
		  
		  int count = getWangCounts(copy2, table.getWangMjVal());
  		int rapWang = getWangzhuaRap(table.getWangMjVal(), huMj, infos, copy2);
  		
		boolean yet = checkyiTiaoLong(copy2, count-rapWang, table);
		return yet;
	}
	  
	  
	  
	  private static List<Integer> getYEtaoCommList(boolean zheng){
		  List<Integer> commTypes = new ArrayList<>();
		  
		  for(int i=1;i<=7;i++){
			  if(zheng){
				  commTypes.add(8-i);
				  commTypes.add(8-i);
			  }else{
				  commTypes.add(i);
				  commTypes.add(i);
			  }
			 
		  }
//		  for(int i=1;i<=7;i++){
//			  commTypes.add(i);
//		  }
		  
		  return commTypes;
		  
	  }
	  
	  //拿一条龙或有一套需要的麻将
	  private  static List<DzMj> getHandYTLMjs( List<DzMj> colorMjs,int type){
		  List<DzMj> res =new ArrayList<>();
		  HashSet<Integer> vals = new HashSet<>();
		  for(DzMj mj: colorMjs){
			  
			  int val = mj.getVal()%10;
			  if(type==1){
				 if(val==1||val==2||val==3){
					 addMtHandMj(res, vals, mj, val);
				 }
			  }else  if(type==4){
				  if(val==4||val==5||val==6){
						 addMtHandMj(res, vals, mj, val);
					 }
			  }else  if(type==7){
				  if(val==7||val==8||val==9){
						 addMtHandMj(res, vals, mj, val);
					 }
			  }else  if(type==2){
				  if(val==2||val==3||val==4){
						 addMtHandMj(res, vals, mj, val);
					 }
			  }else  if(type==3){
				  if(val==3||val==4||val==5){
						 addMtHandMj(res, vals, mj, val);
					 }
			  }else  if(type==5){
				  if(val==5||val==6||val==7){
						 addMtHandMj(res, vals, mj, val);
					 }
			  }else  if(type==6){
				  if(val==6||val==7||val==8){
						 addMtHandMj(res, vals, mj, val);
					 }
			  }
			  
		  }
		  
		  
		  
		  
		  return res;
		  
		  
	  }

	private static void addMtHandMj(List<DzMj> res, HashSet<Integer> vals, DzMj mj, int val) {
		if(!vals.contains(val)){
			 vals.add(val);
			 res.add(mj);
		 }
	}
	  
	  private static int getWangCounts(List<DzMj> copy,int wangVal){
		  int count = 0;
	        Iterator<DzMj> iterator = copy.iterator();
	        while (iterator.hasNext()) {
	            DzMj majiang = iterator.next();
	            if (majiang.getVal() == wangVal) {
	            	count++;
	            }
	        }
	        return count;
	  }





    private static List<DzMj> getMjsByColor(List<DzMj> copy,int color,int wangVal,int hzr,int wangGWCount){
        List<DzMj> hongzhong = new ArrayList<>();
        Iterator<DzMj> iterator = copy.iterator();
        int  rep= 0;
        int repWangC = 0;
        List<DzMj> reWangs = new ArrayList<>();
        List<DzMj> wangs = DzMj.getSameMjList(4, wangVal, copy);
        while (iterator.hasNext()) {
            DzMj majiang = iterator.next();
            if (majiang.getColourVal() == color) {
                if(majiang.getVal()==wangVal){
                    if(wangGWCount>repWangC){
                        hongzhong.add(majiang);
                        repWangC++;
                    }
                }else{
                    hongzhong.add(majiang);
                }
            }
            if(majiang.isHongzhong()&&hzr>rep){
                wangs.removeAll(reWangs);
                DzMj mj2 =null;
                if(!wangs.isEmpty()){
                    mj2 = 	wangs.get(0);
                }else{
                    if(!reWangs.isEmpty())
                        mj2 = 	reWangs.get(0);
                }
                if(mj2==null||mj2.getColourVal()!=color){
                    continue;
                }
                reWangs.add(mj2);
                hongzhong.add(mj2);
                rep++;
            }
        }
        return hongzhong;
    }




    private static int getChiType(List<Integer> cardIds,int wangVal){
		  HashSet<Integer> hset = new HashSet<Integer>();
		  for(Integer id: cardIds){
			  DzMj mj = DzMj.getMajang(id);
			  if(mj.isHongzhong()){
				  hset.add(wangVal%10);
			  }else{
				  hset.add(mj.getVal()%10);
			  }
		  }
		  
		  if(hset.contains(1)&&hset.contains(2)&&hset.contains(3)){
			  return 1;
		  }else if(hset.contains(2)&&hset.contains(3)&&hset.contains(4)){
			  return 2;
		  }else if(hset.contains(3)&&hset.contains(4)&&hset.contains(5)){
			  return 3;
		  }else if(hset.contains(4)&&hset.contains(5)&&hset.contains(6)){
			  return 4;
		  }else if(hset.contains(5)&&hset.contains(6)&&hset.contains(7)){
			  return 5;
		  }else if(hset.contains(6)&&hset.contains(7)&&hset.contains(8)){
			  return 6;
		  }else if(hset.contains(7)&&hset.contains(8)&&hset.contains(9)){
			  return 7;
		  }
		  return 0;
		  
	  }
    
    
    private static int[]  youYiTao(List<DzMj> majiangs,int[] infos,int wangVal,DaozMjTable table,DzMj huMj) {
    	
    	dropWangBa(majiangs, wangVal);
    	int[] res = new int[3];
    	HashMap<Integer,int[]> map = new HashMap<Integer,int[]>();
    	
    	DzMj wang =  DzMj.getMajangVal2(wangVal);
		int[] mjVals2 = map.get(wang.getColourVal());
		if(mjVals2 ==null){
			mjVals2 = new int[9];
			map.put(wang.getColourVal(), mjVals2);
		}
    	
    	if(infos[5]>0&&wangVal<200){
    		mjVals2[wangVal%10-1] +=infos[5];//红中代替王
    	}
    	
//    	int repWang =0;
//		if(infos[0]>0||infos[1]>0){
//			if(infos[0]==1){
//				repWang=1;
//			}else if(infos[0]==2||infos[1]==1){
//				repWang=2;
//			}else if(infos[1]==2){
//				repWang=3;
//			}
//		}else{
//			repWang = infos[4];
//		}
		
		mjVals2[wangVal%10-1] +=(infos[2]-infos[4]);//王是本身
    	
    	
    	for(DzMj mj: majiangs){
    		if(mj.isFeng()){
    			continue;
    		}
    		int[] mjVals = map.get(mj.getColourVal());
    		if(mjVals ==null){
    			mjVals = new int[9];
    			map.put(mj.getColourVal(), mjVals);
    		}
    		mjVals[mj.getVal()%10-1] +=1;
    	}
    	
    	
    	
    	
    	int index =-1;
    	int lianShuang=0;//2连对
    	
    	boolean youyit  =false;
    	boolean youliangt = false;
    	
    	int taoNum = 0;
    	int longColor = 0;
    	for(Map.Entry<Integer,int[]>  entry: map.entrySet()){
    		int[] mjVals = entry.getValue();
    		int lian=0;//连
    		for(int i=0;i<mjVals.length;i++){
    			int count = mjVals[i];
    			if(count>=1){
    				lian+=1;
    			}else{
    				lianShuang=0;
    			}
    			if(count>=2){
    				//不连续
    				if(index+1!=i){
    					lianShuang=1;
    				}else{
    					lianShuang+=1;
    				}
    				index =i;
    			}
    			
    			if(index !=i){
    				lianShuang=0;
    			}
    			
    			if(lianShuang>=2&&i>=1&&i<8){
    				if(i==1){
    					int pre = mjVals[i-1];
    					int after = mjVals[i+1];
//    					int yytIndex = i-2;
    					if(pre>1&&after>1){//记录有一套的时候的位置，后面算有两套的时候需要排除
//    						if(taoNum==0||yytIndex!=youyitanIndex){
    							youyit = true;
            					taoNum++;
            					
            					mjVals[i+1] -=2;
            					mjVals[i] -=2;
            					mjVals[i-1] -=1;
            					
//            					youyitanIndex = i;
//    						}
        				}
    				}else{
//    					int pre = mjVals[i-2];
//    					int after = mjVals[i+1];
////    					int yytIndex = i-2;
//    					if(pre>0&&after>0){
////    						if(taoNum==0||yytIndex!=youyitanIndex){
//    							youyit = true;
//            					taoNum++;
//            					
//            					mjVals[i+1] -=1;
//            					mjVals[i] -=2;
//            					mjVals[i-2] -=1;
//            					
//            					
////            					youyitanIndex = i;
////    						}
//        				}else{
//        					 pre = mjVals[i-1];
        					 int cu = mjVals[i-2];
        					int pre = mjVals[i+1];
//        					 yytIndex = i-1;
        					 if(pre>=2||cu>1){
//        						 if(taoNum==0||yytIndex!=youyitanIndex){
         							youyit = true;
                 					taoNum++;
                 					
                 					
                 					
                 					mjVals[i+1] -=2;
                					mjVals[i] -=2;
                					mjVals[i-1] -=1;
                 					
//                 					youyitanIndex = i;
//         						}
//        					 }
        					
        				}
    				}
    				
    				//重置
    				if(youyit){
    					lianShuang = 0;
    				}
    				if(taoNum==2){
    					youliangt = true;
    				}
    				
    			}
    		}
    		if(res[1]==0){
    			res[1]=lian==9?1:0;
    			longColor = entry.getKey();
    		}
    	}
    	
    	if(res[1]!=1){
    		res[0]=youyit?1:0;
    	}else{
    		//如果是一条龙，删除掉一条龙还能胡，那就算
    		boolean dorpFlag = false;;
    		if(longColor==wangVal/10){
    			int hzCount = infos[5];
        		if(hzCount>0){
        			dropAssignMj(majiangs, 201);
        			dorpFlag =true;
        		}
    		}
    		int[] mjVals = map.get(longColor);
    		for(int i=0;i<mjVals.length;i++){
    			int val = longColor*10+1+i;
    			if(val==wangVal&&dorpFlag){
    				continue;
    			}
    			dropAssignMj(majiangs, val);
    		}
    		
    		
    		int rapWang = getWangzhuaRap(wangVal, huMj, infos, majiangs);
//    		int rapWang =0;
//    		if(infos[0]==1){
//				rapWang =1;
//				//dropWangByCout
//			}else if(infos[0]==2||infos[1]==1){
//				rapWang =2;
//			}else if(infos[1]==2){
//				//dropMjs.addAll(dropWangByCout(copy, wangVal, 3));
//				rapWang =3;
//			}
			
			boolean yitiaoLong = checkyiTiaoLong(majiangs, infos[2]-rapWang, table);
			if (!yitiaoLong) {
				res[1] = 0;
			}
			res[0] = youyit ? 1 : 0;
			
			
    		
    	}
    	res[2]=youliangt?1:0;
    	return res;
    }
    
    
    private static int getWangRepMjVal(int count,int repVal,List<DzMj> majiangs){
    	
    	HashMap<Integer,Integer> map= new HashMap<Integer,Integer>();
    	for(DzMj mj: majiangs){
    		
    		Integer valCount = map.get(mj.getVal());
    		
    		if(valCount==null){
    			map.put(mj.getVal(), 1);
    		}else{
    			map.put(mj.getVal(), valCount+1);
    		}
    	}
    	
    	Integer repValCount = map.get(repVal);
    	int realRepVal = 0;
    	//|| 4-repValCount>=count
    	if(repValCount==null){
    		realRepVal = repVal;
    	}else{
    		  for (DzMj mj : DzMj.fullMj) {
    			  Integer repcout  = map.get(mj.getVal());
                  Integer repcout2  = map.get(mj.getVal()+1);
                  Integer repcout3  = map.get(mj.getVal()-1);

    			  //||4-repcout>count
    			  if(repcout==null&&repcout2==null&&repcout3==null){
    				  realRepVal = mj.getVal();
    				  break;
    			  }
    		  }
    	}
    	
    	return realRepVal;
    	
    	
    }

	private static boolean checkyiTiaoLong(List<DzMj> majiangs, int wangCount, DaozMjTable table) {
		int hzCount = DaozMjTool.haveHongzhong(majiangs);
		boolean longyitiao = false;
		// 有红中再变下牌
		for (int i = 0; i <= wangCount; i++) {
			List<DzMj> cards2 = new ArrayList<>(majiangs);
			DaozMjTool.dropWangByCout(cards2, table.getWangMjVal(), i);
				boolean isHu = HuUtil.isCanHu(cards2, i);
				if(isHu){
					longyitiao  = true;
					break;
				}
				if (!isHu && hzCount > 0 && table.getWangMjVal() < 200) { 
					// 有红中再变下牌
					for (int j = 1; j <= hzCount; j++) {
//						if(j==2){
//							break;
//						}
						List<DzMj> cards3 = new ArrayList<>(cards2);
						DaozMjTool.dropHzhong(cards3, j);
						cards3.addAll(DzMj.getSameMjList(j, table.getWangMjVal(), majiangs));
						isHu = HuUtil.isCanHu(cards3, i);
						if (isHu) {
							table.setHuHzRepCount(j);
							longyitiao  = true;
							break;
						}
					}
				
				}
			}
		return longyitiao;
	}
    
    
    
    public static boolean dropAssignMj(List<DzMj> majiangs,int val) {
    	  Iterator<DzMj> iterator = majiangs.iterator();
          while (iterator.hasNext()) {
              DzMj majiang = iterator.next();
              if (majiang.getVal() == val) {
                  iterator.remove();
                  break;
              }
          }
          return true;
    	
    }
    
    
    
    public static boolean haveWang(List<DzMj> majiangs,int wangVal) {
        for (DzMj majiang : majiangs) {
            if (majiang.getVal()==wangVal) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<DzMj> checkChi(List<DzMj> majiangs, DzMj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = DaozMjHelper.toMajiangVals(majiangs);
        if (wangValList == null || !checkWang(chi1, wangValList)) {
            if (majiangIds.containsAll(chi1)) {
                return findMajiangByVals(majiangs, chi1);
            }
        }
        
        if (wangValList == null || !checkWang(chi3, wangValList)) {
            if (majiangIds.containsAll(chi3)) {
                return findMajiangByVals(majiangs, chi3);
            }
        }
        
        if (wangValList == null || !checkWang(chi2, wangValList)) {
            if (majiangIds.containsAll(chi2)) {
                return findMajiangByVals(majiangs, chi2);
            }
        }
        return new ArrayList<DzMj>();
    }


    public static List<List<DzMj>> checkChiHzrep(List<DzMj> majiangs, DzMj dismajiang) {
        int disMajiangVal = dismajiang.getVal();

        List<List<DzMj>> list = new ArrayList<>();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = DaozMjHelper.toMajiangVals(majiangs);
            if (majiangIds.containsAll(chi1)) {
                list.add(findMajiangByVals(majiangs, chi1));
            }

            if (majiangIds.containsAll(chi3)) {
                list.add(findMajiangByVals(majiangs, chi3));
            }

            if (majiangIds.containsAll(chi2)) {
                list.add(findMajiangByVals(majiangs, chi2));
            }
        return list;
    }

    
    
    public static List<DzMj> checkChi2(List<DzMj> majiangs, DzMj dismajiang,int index ) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = DaozMjHelper.toMajiangVals(majiangs);
        
        if(index==1){
        	 if (majiangIds.containsAll(chi1)) {
                 return findMajiangByVals(majiangs, chi1);
             }
        }else if(index==2){
        	if (majiangIds.containsAll(chi3)) {
                return findMajiangByVals(majiangs, chi3);
            }
        }else{
        	 if (majiangIds.containsAll(chi2)) {
                 return findMajiangByVals(majiangs, chi2);
             }
        }
           
        return new ArrayList<DzMj>();
    }

    public static List<DzMj> findMajiangByVals(List<DzMj> majiangs, List<Integer> vals) {
        List<DzMj> result = new ArrayList<>();
        for (int val : vals) {
            for (DzMj majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 去掉红中
     *
     * @param copy
     * @return
     */
    public static List<DzMj> dropWangBa(List<DzMj> copy,int wangVal) {
        List<DzMj> hongzhong = new ArrayList<>();
        Iterator<DzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            DzMj majiang = iterator.next();
            if (majiang.getVal() == wangVal) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }
    
    
    /**
     * 去掉红中
     *
     * @param copy
     * @return
     */
    public static List<DzMj> dropHzhong(List<DzMj> copy,int count) {
        List<DzMj> hongzhong = new ArrayList<>();
        Iterator<DzMj> iterator = copy.iterator();
        int hzCount = 0;
        while (iterator.hasNext()) {
            DzMj majiang = iterator.next();
            if (majiang.isHongzhong() ) {
            	hzCount ++;
                hongzhong.add(majiang);
                iterator.remove();
            }
            if(hzCount==count){
            	break;
            }
        }
        return hongzhong;
    }
    
    public static List<DzMj> dropWangByCout(List<DzMj> copy,int wangVal,int count) {
        List<DzMj> hongzhong = new ArrayList<>();
        if(count==0){
        	return hongzhong;
        }
        Iterator<DzMj> iterator = copy.iterator();
        int hzCount = 0;
        while (iterator.hasNext()) {
            DzMj majiang = iterator.next();
            if (majiang.getVal()==wangVal ) {
            	hzCount ++;
                hongzhong.add(majiang);
                iterator.remove();
            }
            if(hzCount==count){
            	break;
            }
        }
        return hongzhong;
    }
    
    

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof DzMj) {
                    val = ((DzMj) majiang).getVal();
                } else {
                    val = (int) majiang;
                }
                if (wangValList.contains(val)) {
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * 相同的麻将
     *
     * @param majiangs 麻将牌
     * @param majiang  麻将
     * @param num      想要的数量
     * @return
     */
    public static List<DzMj> getSameMajiang(List<DzMj> majiangs, DzMj majiang, int num) {
        List<DzMj> hongzhong = new ArrayList<>();
        int i = 0;
        for (DzMj maji : majiangs) {
            if (maji.getVal() == majiang.getVal()) {
                hongzhong.add(maji);
                i++;
            }
            if (i >= num) {
                break;
            }
        }
        return hongzhong;

    }
    
    
    public static DzMj getSameMajiang2(List<DzMj> majiangs, int  majiangVal) {
        for (DzMj maji : majiangs) {
            if (maji.getVal() == majiangVal) {
               return maji;
            }
        }
        return null;

    }
    
    
    
    public static List<DzMj> addChiList(List<DzMj> majiangs, int  wangVal) {
    	 List<DzMj> hongzhong = new ArrayList<>();
    	
    	 DzMj mj1 = getSameMajiang2(majiangs, wangVal-2);
    	 if(mj1!=null){
    		 hongzhong.add(mj1);
    	 }
    	 
    	 DzMj mj2 = getSameMajiang2(majiangs, wangVal-1);
    	 if(mj2!=null){
    		 hongzhong.add(mj2);
    	 }
    	 
    	 
    	 DzMj mj3 = getSameMajiang2(majiangs, wangVal+1);
    	 if(mj3!=null){
    		 hongzhong.add(mj3);
    	 }
    	 DzMj mj4 = getSameMajiang2(majiangs, wangVal+2);
    	 if(mj4!=null){
    		 hongzhong.add(mj4);
    	 }
    	 
        return hongzhong;

    }
    

    /**
     * 先去某个值
     *
     * @param copy
     * @return
     */
    public static List<DzMj> dropMjId(List<DzMj> copy, int id) {
        List<DzMj> hongzhong = new ArrayList<>();
        Iterator<DzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            DzMj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static List<DzMj> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<DzMj> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (DzMj mj : DzMj.values()) {
                if (mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)) {
                    handPais.add(mj);
                    break;
                }
            }
        }
        return handPais;
    }

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cards   去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<DzMj> getLackListOld(List<DzMj> cards, int hzCount, boolean hu7dui) {
        if ((cards.size() + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<DzMj> lackPaiList = HuUtil.getLackPaiList(cards, hzCount);
        if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
            // 听所有
            return lackPaiList;
        }
        Set<Integer> have = new HashSet<>();
        if (lackPaiList.size() > 0) {
            Iterator<DzMj> iterator = lackPaiList.iterator();
            int lastIndex = cards.size();
            DzMj mj;
            while (iterator.hasNext()) {
                mj = iterator.next();
                // 去重
                if (have.contains(mj.getVal())) {
                    iterator.remove();
                } else {
                    have.add(mj.getVal());

                    //检验
                    cards.add(mj);
                    if (!HuUtil.isCanHu(cards, hzCount)) {
                        iterator.remove();
                    }
                    cards.remove(lastIndex);
                }
            }
            if (lackPaiList.size() == 27) {
                lackPaiList.clear();
                lackPaiList.add(null);
            }
        }
        if (hu7dui && cards.size() + hzCount + 1 == 14) {
            //检查七小对
            if (DaozMjTool.isHu7Dui(cards, hzCount + 1)) {
                int rmIndex = cards.size();
                for (DzMj mj : DzMj.fullMj) {
                    if (mj.isHongzhong()|| have.contains(mj.getVal())) {
                        continue;
                    }
                    cards.add(mj);
                    if (DaozMjTool.isHu7Dui(cards, hzCount)) {
                        lackPaiList.add(mj);
                        have.add(mj.getVal());
                    }
                    cards.remove(rmIndex);
                }
            }
        }


        return lackPaiList;
    }

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cardArr 去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<DzMj> getLackList(int[] cardArr, int hzCount, boolean hu7dui,int wangVal) {
        int cardNum = 0;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
        }
        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<DzMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (DzMj mj : DzMj.fullMj) {
        	//mj.isHongzhong(mj.getVal()==wangVal ||
            if ( have.contains(mj.getVal())) {
                continue;
            }
//            
//            if(wangVal<200){
//            	
//            }
            
            int cardIndex = HuUtil.getMjIndex(mj);
            cardArr[cardIndex] = cardArr[cardIndex] + 1;
            //hu7dui &&
            if (HuUtil.isCanHu7Dui(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            if (!lackPaiList.contains(mj)&&HuUtil.isCanHu(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            cardArr[cardIndex] = cardArr[cardIndex] - 1;
        }
        if (lackPaiList.size() == 27) {
            lackPaiList.clear();
            lackPaiList.add(null);
        }
        return lackPaiList;
    }


    public static void main(String[] args) {
        HuUtil.init();
        int laiZiVal = 331;
        int laiZiNum = 1;
        String paisStr = "331,331,331,11,12,13,21,22,23,31,32,33,39,39";
        paisStr = "33,33,33,36,36,36,38,38,39,39";
        paisStr = "37,37,15,16,17,24,24,25,25";
//        paisStr = "38,39,11,11,11,15,16,17,17,18,19,26";
        paisStr = "36,36,37,37,15,16,16,17,17,18,25,25";
        paisStr = "31,37,37,37,12,14,16,17,17,19,24,24,25";
        List<DzMj> handPais = getPais(paisStr);
        System.out.println(handPais);
        int count = 1;
        boolean canHu = false;
        long start = Clock.systemDefaultZone().millis();

        for (int i = 0; i < count; i++) {
//            canHu = isHu(handPais, true,11);
        }
        long timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + canHu + " , count = " + count + " , timeUse = " + timeUse + " ms");


        canHu = false;
        start = Clock.systemDefaultZone().millis();
        for (int i = 0; i < count; i++) {
            canHu = HuUtil.isCanHu(handPais, laiZiNum);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + canHu + " , count = " + count + " , timeUse = " + timeUse + " ms");


        count = 1;
        List<DzMj> lackPaiList = new ArrayList<>();
        start = Clock.systemDefaultZone().millis();
        for (int i = 0; i < count; i++) {
           // lackPaiList = getLackListOld(handPais, laiZiNum, true);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("---count = " + count + " , timeUse = " + timeUse + " ms" + "tingList : " + lackPaiList);

        start = Clock.systemDefaultZone().millis();
        int[] cardArr = HuUtil.toCardArray(handPais);
        for (int i = 0; i < count; i++) {
          //  lackPaiList = getLackList(cardArr, laiZiNum, true);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("---count = " + count + " , timeUse = " + timeUse + " ms" + "tingList : " + lackPaiList);

    }
}
