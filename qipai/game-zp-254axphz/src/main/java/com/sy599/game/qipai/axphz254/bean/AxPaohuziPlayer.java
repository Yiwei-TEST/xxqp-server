package com.sy599.game.qipai.axphz254.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.axphz254.command.CommandProcessor;
import com.sy599.game.qipai.axphz254.constant.EnumHelper;
import com.sy599.game.qipai.axphz254.constant.PaohuziConstant;
import com.sy599.game.qipai.axphz254.constant.PaohzCard;
import com.sy599.game.qipai.axphz254.rule.PaohuziIndex;
import com.sy599.game.qipai.axphz254.rule.PaohzCardIndexArr;
import com.sy599.game.qipai.axphz254.tool.PaohuziHuLack;
import com.sy599.game.qipai.axphz254.tool.PaohuziTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;

import lombok.Data;

/**
 * @param
 * @author Guang.OuYang
 * @description 湘乡告胡子
 * @return
 * @date 2019/9/2
 */
@Data
public class AxPaohuziPlayer extends Player {
    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_Axphz);

    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    //当前状态,1牌局离线,2牌局在线,3牌局暂离,4牌局暂离回来
    private int isEntryTable;
    // 需要记下吃的胡息
    private int outHuXi;
    
    // 需要记下团圆的胡息
    private int tuanYuanXi;
    //当前胡息
    private int huxi;
    //赢得次数
    private int winCount;
    //当前积分,会影响到总积分
    private int point;
    //积分输赢分统计,用于大结算时临时展示未翻倍之前的积分
    private int winLossPoint;
    //输的次数
    private int lostCount;
    //输掉积分
    private int lostPoint;
    // 每出4张牌一次 就欠一张牌  -补牌 +出牌
    private int oweCardCount;
    //奖励分,不从输家扣除的分,单位/囤,可用于展示
    private int awardWinLossPoint;
    //胡的牌
    private PaohuziHuLack hu;
    //玩家首次出牌是否还在标志位
    private boolean isFristDisCard;
    //玩家首次出单牌标志位
    private boolean firstDisSingleCard;
    
    
    //玩家首次听牌
    private int tingFlag;
    
    //栽胡息
    private int zaiHuxi;
    //放技能,当前可提或跑
    private int fangZhao;
    //0胡1自摸2提3跑
    private int[] actionTotalArr = new int[4];
    //托管
    private volatile boolean autoPlay = false;
    //最后操作时间
    private volatile long lastOperateTime = 0;
    //最后检查时间
    private volatile long lastCheckTime = 0;
    //自动操作时间
    private volatile long autoPlayTime = 0;
    //开始托管合计时
    private volatile boolean isCheckAuto = false;
    //初始手牌, 好像是断线重连才用
    private List<Integer> firstPais = new ArrayList<>();
    //手里的牌
    private List<PaohzCard> handPais = new ArrayList<>();
    //出去的牌, 跑胡子摸排都是明牌不会再次摸到手上来, 所以摸牌时直接加入出牌队
    private List<PaohzCard> outPais = new ArrayList<>();
    //碰牌 当别人打的牌或摸的牌跟自己手上的一对一样时，则可以碰牌，碰牌后将牌放桌上。
    private List<PaohzCard> peng = new ArrayList<>();
    //吃牌 当吃的牌，手中还有时，必须将手中的这张牌根据某种组合，也要入到桌上，称为比牌。
    private List<PaohzCard> chi = new ArrayList<>();
    //跑牌 当玩家从墩牌上摸的牌正好是自己碰过的牌，称为跑；
    //c、碰过后，别人在摸到该张，同样可以形成跑；跑的牌必须将牌放在桌面上（四张明牌）；可以破跑胡（情况一：摸出来的被胡算平胡；情况二：摸出来的被胡算自摸）。
    private List<PaohzCard> pao = new ArrayList<>();
    //过张 碰 a、当自己有机会吃或碰时，自己没有吃或碰，则称为过张，过张后的牌不可再吃和碰;
    private List<Integer> passPeng = new ArrayList<>();
    //过张 吃 a、当自己有机会吃或碰时，自己没有吃或碰，则称为过张，过张后的牌不可再吃和碰;
    private List<Integer> passChi = new ArrayList<>();
    //栽/偎的牌组 摸牌时，如果所摸的牌, 正好是手中已有的一对牌，则必须将牌由手上放到桌上。（两暗一明）
    private List<PaohzCard> zai = new ArrayList<>();
    //提a、砌牌后，手中4个相同的牌，称为提; b、如果抓底牌时，抓到的底牌正好是手中一坎牌称为提，提的牌必须将牌放在桌面上（开始4张暗，庄家出第一张后亮）
    private List<PaohzCard> ti = new ArrayList<>();
    //牌型组合集, 每个CardType代表一个组合, 如碰, 吃
    private List<CardTypeHuxi> cardTypes = new ArrayList<>();
    //臭栽/偎 忍(过)碰牌之后，又名扫牌，称为过扫; 过扫牌必须将牌由手上放到桌上。（两暗一明）
    private List<PaohzCard> chouZai = new ArrayList<>();
    //--------------
    //出牌数量，只计算吃碰跑提栽之后的单出牌
    private int disNum=0;
    //限制Chu
    private List<Integer> limitChuCardVals = new ArrayList<>();
    //拆掉的坎龙
    private List<Integer> chaiKanOrLCardVals = new ArrayList<>();
    //触发吃边打边，只能胡红黑堂
    private int onlyHht = 0;
    //起手团圆息
    private int firstTuanYuanXi=0;
    // -------------------------
    private int passHuCount = 0;

    public void setAutoPlay(boolean autoPlay, BaseTable table) {
        if (this.autoPlay != autoPlay) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(132, seat, autoPlay ? 1 : 0, (int) userId);
            GeneratedMessage msg = res.build();
            for (Entry<Long, Player> kv : table.getPlayerMap().entrySet()) {
                Player player = kv.getValue();
                if (player.getIsOnline() == 0) {
                    continue;
                }
                player.writeSocket(msg);
            }
            table.addPlayLog(getSeat(), PaohuziDisAction.action_tuoguan + "",(autoPlay?1:0) + "");
        }
        this.autoPlay = autoPlay;
        this.setCheckAuto(false);
        if (!autoPlay) {
            setAutoPlayCheckedTimeAdded(false);
        }
    }

    public void setLastOperateTime(long lastOperateTime) {
        this.lastCheckTime = 0;
        this.lastOperateTime = lastOperateTime;
        this.autoPlayTime = 0;
        this.setCheckAuto(false);
    }

    /**
     * 操作
     *
     * @param index 0胡1自摸2提3跑
     * @param val
     */
    public void changeAction(int index, int val) {
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }

    /**
     * 获得初始手牌
     *
     * @return
     */
    public List<Integer> getFirstPais() {
        return firstPais;
    }

    /**
     * 移掉出的牌
     */
    void removeOutPais(PaohzCard card) {
        outPais.remove(card);
    }

    /**
     * 摸牌
     */
    public void moCard(PaohzCard card) {
        this.outPais.add(card);
        setFristDisCard(false);
    }

    public void tiLong(PaohzCard card) {
        this.handPais.add(card);
        compensateCard();
        changeSeat(seat);
    }


    /**
     * @param
     * @return
     * @description 出牌
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    void disCard(int action, List<PaohzCard> disCardList) {
        for (PaohzCard disCard : disCardList) {
            if (!handPais.remove(disCard)) {
                if (!zai.remove(disCard)) {
                    if (!peng.remove(disCard)) {
                        pao.remove(disCard);
                    }
                }
            }
        }

        if (disCardList.size() > 1) {
            PaohzCard card = disCardList.get(1);
            Iterator<CardTypeHuxi> iterator = cardTypes.iterator();
            while (iterator.hasNext()) {
                CardTypeHuxi type = iterator.next();
                if (type.isHasCard(card)) {
                    if (type.getAction() == PaohzDisAction.action_zai) {
                        // 去掉栽的息
                        changeZaihu(-type.getHux());
                    } else {
                        changeOutCardHuxi(-type.getHux());
                    }
                    iterator.remove();
                }

            }
        }

        if (disCardList.size() == 4) {
            oweCardCount--;
        }

      if (action == PaohzDisAction.action_zai) {
            zai.addAll(disCardList);
        } else if (action == PaohzDisAction.action_chouzai) {
            chouZai.addAll(disCardList);
        } else if (action == PaohzDisAction.action_peng) {
            peng.addAll(disCardList);
        } else if (action == PaohzDisAction.action_chi) {
            chi.addAll(disCardList);
        }  else {
            outPais.addAll(disCardList);
            int passVal = disCardList.get(0).getVal();
            passChi.add(passVal);
            passPeng.add(passVal);
        }
      
      

        if (action != PaohzDisAction.action_zai) {
            setFristDisCard(false);
        }
        addLimitChuCardVal(disCardList.get(0).getVal());
        checkAddTuanYuanXi(disCardList.get(0),false);
        
        
        if (disCardList.size() > 3 && disCardList.size() % 3 == 0) {
            for (int i = 0; i < disCardList.size() / 3; i++) {
                List<PaohzCard> list = disCardList.subList(i * 3, i * 3 + 3);
                addCardType(action, list, getPlayingTable(AxPaohuziTable.class).getGameModel());
            }
        } else {
            addCardType(action, disCardList, getPlayingTable(AxPaohuziTable.class).getGameModel());
        }
        changeSeat(seat);
        changeTableInfo();
    }

	public void checkAddTuanYuanXi(PaohzCard disCard,boolean add) {
		int tyXi = checkTuanYuan(disCard,add);
		if(tyXi>0){
        		changeTuanYuanXi(tyXi);
        }
	}
    
    private int checkTuanYuan(PaohzCard disCard,boolean add){
    	List<PaohzCard> allCards = new ArrayList<>();
//    	allCards.addAll(disCardList);
    	if(add&&!zai.contains(disCard)){
    		allCards.add(disCard);
    	}
    	
    	allCards.addAll(handPais);
    	allCards.addAll(chi);
    	allCards.addAll(peng);
    	allCards.addAll(zai);
    	
    	if(add&&zai.contains(disCard)){
    		allCards.remove(disCard);
    	}
    	
    	PaohzCard disc = disCard;
    	
    	int pai = disc.getVal()%100;
		int tyXi= 0;
		if(isRead(pai)){
			tyXi = 7;
		}else{
			tyXi = 5;
		}
    	
    	int count = 0;
    	for(PaohzCard card : allCards){
    		if(disc.getVal()==card.getVal()){
    			count++;
    		}
    	}
    	if(count==4){
    		if(isContainCard(peng,disc.getVal())||add){
    			tyXi -= decTuanyuanXi(pai, 3);
    		}else if(isContainCard(zai, disc.getVal())){
    			tyXi-= decTuanyuanXi(pai, 3);
    		}
    		else if(chaiKanOrLCardVals.contains(disc.getVal())){
    			tyXi-= decTuanyuanXi(pai, 4);
    		}
    		
    		return tyXi;
    	}
    	return 0;
    	
    }

	private int decTuanyuanXi(int pai, int xi) {
		int resXi = 0;
		if(isRead(pai)){
			resXi = xi;
		}else{
			resXi = xi-1;
		}
		return resXi;
	}
    
    
    
    private boolean isContainCard(List<PaohzCard> cardList,int val){
    
    	for(PaohzCard card:cardList){
    		if(card.getVal()==val)
    			return true;
    	}
    	return false;
    }
    
    

    /**
     * @param
     * @return
     * @description 新增组合牌型
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public void addCardType(int action, List<PaohzCard> disCardList, GameModel gameModel) {
        int huxi = PaohuziTool.getOutCardHuxi(action, disCardList, gameModel);
        if (action != 0) {
            CardTypeHuxi type = new CardTypeHuxi();
            type.setAction(action);
            type.setCardIds(PaohuziTool.toPhzCardIds(disCardList));
            type.setHux(huxi);
            cardTypes.add(type);
        }

        LogUtil.printDebug("增加胡息{}:牌型组合:{}", huxi, action);
        if (action == PaohzDisAction.action_zai) {
            changeZaihu(huxi);
        } else {
            changeOutCardHuxi(huxi);
        }
    }

    public List<PaohzCard> getSameCards(PaohzCard card) {
        List<PaohzCard> list = new ArrayList<>();
        List<PaohzCard> zaiList = PaohuziTool.findPhzByVal(zai, card.getVal());
        list.addAll(zaiList);
        List<PaohzCard> paoList = PaohuziTool.findPhzByVal(handPais, card.getVal());
        list.addAll(paoList);
        List<PaohzCard> pengList = PaohuziTool.findPhzByVal(peng, card.getVal());
        list.addAll(pengList);
        return list;
    }

    /**
     * 是否需要出牌
     *
     * @return
     */
    public boolean isNeedDisCard(int action) {
        return isFristDisCard() || oweCardCount >= -1;
    }

    /**
     *
     */
    public void compensateCard() {
        oweCardCount++;
        changeTableInfo();
    }

    public void pass(int action, int val) {
        pass(action, val, false);
    }

    public void pass(int action, int val, boolean addPassChi) {
        if (action == PaohzDisAction.action_chi) {
            if (addPassChi) {
                passChi.add(val);
            }
        } else if (action == PaohzDisAction.action_peng) {
            passPeng.add(val);
        } else {
            return;
        }
        changeTableInfo();
    }

    @Override
    public void initPlayInfo(String data) {
        if (!StringUtils.isBlank(data)) {
            int i = 0;
            String[] values = data.split(",");
            long duserId = StringUtil.getLongValue(values, i++);
            if (duserId != getUserId()) {
                return;
            }
            this.seat = StringUtil.getIntValue(values, i++);
            int stateVal = StringUtil.getIntValue(values, i++);
            this.state = EnumHelper.getPlayerState(stateVal);
            this.isEntryTable = StringUtil.getIntValue(values, i++);

            String passPengStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passPengStr)) {
                passPeng = StringUtil.explodeToIntList(passPengStr, "_");
            }
            String passChiStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passChiStr)) {
                passChi = StringUtil.explodeToIntList(passChiStr, "_");
            }

            this.outHuXi = StringUtil.getIntValue(values, i++);
            this.huxi = StringUtil.getIntValue(values, i++);
            this.winCount = StringUtil.getIntValue(values, i++);
            this.lostCount = StringUtil.getIntValue(values, i++);
            this.lostPoint = StringUtil.getIntValue(values, i++);
            this.point = StringUtil.getIntValue(values, i++);
            setTotalPoint(StringUtil.getIntValue(values, i++));
            this.oweCardCount = StringUtil.getIntValue(values, i++);
            this.isFristDisCard = StringUtil.getIntValue(values, i++) == 1;
            setMaxPoint(StringUtil.getIntValue(values, i++));
            this.zaiHuxi = StringUtil.getIntValue(values, i++);
            this.fangZhao = StringUtil.getIntValue(values, i++);
            String firstPaisStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(firstPaisStr)) {
                firstPais = StringUtil.explodeToIntList(firstPaisStr, "_");
            }
            String actionTotalArrStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(actionTotalArrStr)) {
                this.actionTotalArr = StringUtil.explodeToIntArray(actionTotalArrStr, "_");
            }

            String forbidS = StringUtil.getValue(values, i++);

            if (!StringUtils.isBlank(firstPaisStr)) {
                chaiKanOrLCardVals = StringUtil.explodeToIntList(forbidS, "_");
                if(chaiKanOrLCardVals==null)
                    chaiKanOrLCardVals=new ArrayList<>();
            }

            String limitS = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(firstPaisStr)) {
                limitChuCardVals = StringUtil.explodeToIntList(limitS, "_");
                if(limitChuCardVals==null)
                    limitChuCardVals=new ArrayList<>();
            }
            onlyHht=StringUtil.getIntValue(values,i++);
            disNum=StringUtil.getIntValue(values,i++);
            firstTuanYuanXi=StringUtil.getIntValue(values,i++);
//            winLossPoint=StringUtil.getIntValue(values,i++);
            awardWinLossPoint=StringUtil.getIntValue(values,i++);
            firstDisSingleCard=StringUtil.getIntValue(values,i++)>0;
            
            tingFlag =StringUtil.getIntValue(values,i++);
            passHuCount =StringUtil.getIntValue(values,i++);
            
            this.tuanYuanXi = StringUtil.getIntValue(values, i++);
//            this.isSendChui = StringUtil.getIntValue(values, i++);
        }
    }

    @Override
    public void clearTableInfo() {
        AxPaohuziTable table = getPlayingTable();
        boolean isCompetition = false;
        if (table != null && table.isCompetition()) {
            isCompetition = true;
            endCompetition();
        }
        table.getGameModel().resetTuo();
        if (table.isAutoPlay() && autoPlay) {
            setAutoPlay(false, table);
        }
        setIsEntryTable(0);
        changeIsLeave(0);
        handPais.clear();
        outPais.clear();
        changeState(null);
        peng.clear();
        chi.clear();
        pao.clear();
        zai.clear();
        chouZai.clear();
        ti.clear();
        passChi.clear();
        passPeng.clear();
        cardTypes.clear();
        chouZai.clear();
        limitChuCardVals.clear();
        chaiKanOrLCardVals.clear();
        setPassHuCount(0);
        setZaiHuxi(0);
        setHu(null);
        setOutHuxi(0);
        setTuanYuanXi(0);
        setHuxi(0);
        setOweCardCount(0);
        setWinCount(0);
        setLostCount(0);
        setPoint(0);
        setLostPoint(0);
        setTotalPoint(0);
        setMaxPoint(0);
        setSeat(0);
        setWinLossPoint(0);
        setFangZhao(0);
        setFirstTuanYuanXi(0);
        if (!isCompetition) {
            setPlayingTableId(0);
        }
        actionTotalArr = new int[4];

        saveBaseInfo();
        setLastCheckTime(0);
    }
    
    
    
    private void changeFirstTYxi(int huxi){
    	this.firstTuanYuanXi +=huxi;
    }

    @Override
    public void setIsEntryTable(int tableOnline) {
        this.isEntryTable = tableOnline;
        changeTableInfo();
    }

    @Override
    public int getSeat() {
        return seat;
    }

    @Override
    public void setSeat(int randomSeat) {
        seat = randomSeat;
    }

    boolean isCanDisCard(List<Integer> disCards, PaohzCard nowDisCard) {
        if (disCards != null) {
            if (nowDisCard != null && disCards.contains(nowDisCard.getId())) {
                List<Integer> copy = new ArrayList<>(disCards);
                // 排除掉在桌面上已经出的牌
                copy.remove((Integer) nowDisCard.getId());
                if (!getHandPais().containsAll(copy)) {
                   // writeErrMsg("找不到牌:" + disCards);
                    return false;
                }
            } else {
                if (!getHandPais().containsAll(disCards)) {
                   // writeErrMsg("找不到牌:" + disCards);
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * @param
     * @return
     * @description //需要一对可胡牌,  前提存在"跑"或"提"
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    private boolean isNeedDui() {
        return true;
    }

    /**
     * @param
     * @return
     * @description 胡牌检测
     * @author Guang.OuYang
     * @date 2019/9/5
     */
    public PaohuziHuLack checkHu(PaohzCard card, boolean isSelfMo) {
        List<PaohzCard> handCopy = new ArrayList<>(handPais);
        boolean isPaoHu = true;
        if (card != null && !handCopy.contains(card)) {
            handCopy.add(card);
            isPaoHu = false;
        }
        
        PaohuziHuLack lack = PaohuziTool.isHuNew(PaohuziTool.getPaohuziHandCardBean(handCopy), card, isSelfMo, outHuXi,true, isPaoHu, getPlayingTable(AxPaohuziTable.class).getGameModel() );

        LogUtil.printDebug("胡检测, 结果:{}, 手里牌的胡息:{},跑or提增加胡息:{},偎或碰的胡息:{},数量:{},出牌胡息:{}, 手上展示的胡息:{}", lack.isHu(), lack.getHuxi(), 0, zaiHuxi, zai.size() / 3, outHuXi, huxi);

        if (lack.isHu()) {
        	int[] erximan = isErxiManYuanHuaHu(lack.getPhzHuCards(),card);
        	erximan = isErxiManYuanHuaHu(lack.getPhzHuCards(),card);
        	int totalHx =lack.getHuxi() + outHuXi + zaiHuxi+getTuanYuanXi()+erximan[1];
            if (totalHx < ((AxPaohuziTable) this.getPlayingTable()).getGameModel().getRoundFinishLowestHuXi()&&!(erximan[0]==1&&totalHx==2)) {
                // 10胡息能胡
                lack.setHu(false);
            } else {
                setHuxi(lack.getHuxi()+erximan[1]);
            }
        }
        return lack;
    }
    
    
    
    
    
    
    
    private  int[] isErxiManYuanHuaHu( List<CardTypeHuxi> ctypes,PaohzCard card){
    	
    	int[]  result = new int[2];
    	boolean res = true;
    	 List<PaohzCard> allCards = new ArrayList<>();
    	
    	int jiangVal = 0;
    	for (CardTypeHuxi type : getCardTypes()) {
			List<PaohzCard> huTypeCards = PaohuziTool.toPhzCards(type.getCardIds());
			allCards.addAll(huTypeCards);
			//putCardToMap(cardMap, huTypeCards);
			List<PaohzCard> redCards = PaohuziTool.findRedPhzs(huTypeCards);
			if (redCards.isEmpty()) {
				res  =false;
			}
			
    	}
     
            for (CardTypeHuxi type : ctypes) {
            	List<PaohzCard> huTypeCards = PaohuziTool.toPhzCards(type.getCardIds());
    			List<PaohzCard> redCards = PaohuziTool.findRedPhzs(huTypeCards);
    			if (redCards.isEmpty()) {
    				res  =false;
    			}
    			allCards.addAll(huTypeCards);
    			if(type.getCardIds().size()==2){
    				jiangVal= huTypeCards.get(0).getVal();
    			}

        }
            
        	if(card!=null&&!allCards.contains(card)){
        		allCards.add(card);
        	}
            List<PaohzCard> jiangs =  PaohuziTool.findPhzByVal(allCards, jiangVal);
            int zhuanWangXi = 0;
            if(jiangs.size()==3){
            	if(jiangs.get(0).isRed()){
            		zhuanWangXi=1;
            	}else{
            		zhuanWangXi=2;
            	}
            	
            	//changeTuanYuanXi(zhuanWangXi);
            }
//            if(getTotalHu()==2){
//            	return true;
//            }
            result[0]=res?1:0;
            result[1] = zhuanWangXi;
            
            return result;
    }
    
    
    
    
    
    
    public PaohuziHuLack checkTingHu(PaohzCard card, boolean isSelfMo) {
        List<PaohzCard> handCopy = new ArrayList<>(handPais);
        boolean isPaoHu = true;
        if (card != null && !handCopy.contains(card)) {
            handCopy.add(card);
            isPaoHu = false;
        }
        PaohuziHuLack lack = PaohuziTool.isHuNew(PaohuziTool.getPaohuziHandCardBean(handCopy), card, isSelfMo, outHuXi, true, isPaoHu, getPlayingTable(AxPaohuziTable.class).getGameModel() );

        LogUtil.printDebug("胡检测, 结果:{}, 手里牌的胡息:{},跑or提增加胡息:{},偎或碰的胡息:{},数量:{},出牌胡息:{}, 手上展示的胡息:{}", lack.isHu(), lack.getHuxi(), 0, zaiHuxi, zai.size() / 3, outHuXi, huxi);

        if (lack.isHu()) {
        	int addHxi = 0;
        	for(CardTypeHuxi cth: lack.getPhzHuCards()){
        		if(cth.getAction()==PaohzDisAction.action_peng){
        			addHxi+=3;
        			break;
        		}
        	}
            if (lack.getHuxi() + outHuXi + zaiHuxi+addHxi < ((AxPaohuziTable) this.getPlayingTable()).getGameModel().getRoundFinishLowestHuXi()) {
                // 10胡息能胡
                lack.setHu(false);
            } else {
                setHuxi(lack.getHuxi());
            }

        }
        return lack;
    }
    
    
    
    
    
    
    
    
    

    /**
     * 得到可以操作的牌 4张和3张是不能操作的
     */
    PaohuziHandCard getPaohuziHandCard() {
        return PaohuziTool.getPaohuziHandCardBean(handPais);
    }

    /**
     * 拿到可以操作的牌   排除掉不能出的
     *
     * @return
     */
    public List<PaohzCard> getOperateCards() {
        List<PaohzCard> copy = new ArrayList<>(handPais);
        Map<Integer, Integer> valMap = PaohuziTool.toPhzValMap(copy);
        for (Entry<Integer, Integer> entry : valMap.entrySet()) {
        	if(limitChuCardVals.contains(entry.getKey())||entry.getValue()>2)
                PaohuziTool.removePhzByVal(copy, entry.getKey());
        }
        return copy;

    }

    public PaohuziCheckCardBean checkCard(PaohzCard card, boolean isSelfMo, boolean isFirstCard) {
        return checkCard(card, isSelfMo, false, false, isFirstCard, false);
    }

    public PaohuziCheckCardBean checkCard(PaohzCard card, boolean isSelfMo, boolean isBegin, boolean isFirstCard) {
        return checkCard(card, isSelfMo, false, isBegin, isFirstCard, false);
    }

    /**
     *@description 检查跑胡, 提胡
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/5
     */
    public PaohuziCheckCardBean checkPaoHu() {
        PaohuziCheckCardBean check = new PaohuziCheckCardBean();
        check.setSeat(seat);
        PaohuziHuLack lack = checkHu(null, false);
        if (lack.isHu()) {
            check.setLack(lack);
            check.setHu(true);
        }
        check.buildActionList();
        return check;
    }



    /**
     * 已经提，跑，栽固定了的出了的牌值
     *
     * @return
     */
    public List<Integer> getFixedOutVals() {
        // 提，跑，栽
        List<PaohzCard> phzs = new ArrayList<>();
//        phzs.addAll(chouZai);
//        phzs.addAll(zai);
//        phzs.addAll(pao);
//        phzs.addAll(ti);
        return PaohuziTool.toPhzRepeatVals(phzs);
    }

    public boolean canFangZhao(PaohzCard card) {

        if (!zai.contains(card)) {
            List<Integer> zaiVals = PaohuziTool.toPhzCardVals(zai, true);
            if (zaiVals.contains(card.getVal())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查牌, 其他家玩家可做的操作
     * 下家可做的操作: 碰胡, 偎胡, 跑胡, 提胡, 吃胡, 单吊胡
     *
     * @param card        出的牌或者是摸的牌
     * @param isSelfMo    是否是自己摸的牌
     * @param isPassHu    可以胡牌，然后点了过
     * @param isBegin     刚开局
     * @param isFirstCard 首次出牌是否还在
     * @param isPass      点了过
     */
    public PaohuziCheckCardBean checkCard(PaohzCard card, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass) {
        List<PaohzCard> handCardsCopy = new ArrayList<>(handPais);

        //检测结果
        PaohuziCheckCardBean check = new PaohuziCheckCardBean();
        check.setSeat(seat);
        check.setDisCard(card);

        //根据当前手牌, 得到可以操作的牌 4张和3张是不能操作的剔除
        PaohuziHandCard handCardsOperation = getPaohuziHandCard();
        //有跑的或提的情况下,手牌20-4=16%3=1+系统的牌=2所以不能组成吃或碰,手牌仅有1了
        boolean isCanChiPeng = true;
        int operateCount = handCardsOperation.getOperateCards().size();
        if (operateCount < 4 && oweCardCount >= -1) {
            // 必须要出牌但是可以可操作的牌已经不够了
            isCanChiPeng = false;
        }

        PaohzCardIndexArr groupByNumberToArray = handCardsOperation.getGroupByNumberToArray();

        //当前牌桌
        AxPaohuziTable table = getPlayingTable(AxPaohuziTable.class);

        //当前打出来的牌是否是当前摸出来的
        boolean currentCardIsMoFlag = table.isMoFlag();

        if (card != null) {
            List<Integer> fixdOutVals = getFixedOutVals();
            // 有没有一样的牌
            List<PaohzCard> sameCardsList = getSameCards(card, isSelfMo, handCardsCopy);
            if (!CollectionUtils.isEmpty(sameCardsList)) {
                //是坎
               if (sameCardsList.size() >= 2) {  //是对
                    if (isSelfMo) {
                        // 玩家手中有一对牌，当自已从墩上摸起一只相同的牌，玩家必须将手中的一对连同摸起的牌放置于桌面，且不能明示给其它玩家，称为栽/偎
                        // 看看是否是臭栽,臭偎,第一次没有碰,第二次碰了
                        if (passPeng.contains(card.getVal())) {
                            check.setAuto(PaohzDisAction.action_chouzai, sameCardsList);
                            check.setChouZai(true);
                        } else {
                        	if(sameCardsList.size()>2){
                        		sameCardsList.remove(0);
                        	}
                            check.setAuto(PaohzDisAction.action_zai, sameCardsList);
                            check.setZai(true);
                        }
                    } else {
                        if (isCanChiPeng && !passPeng.contains(card.getVal())) {
                            // 检查碰
                            check.setPeng(true);
                        }

                    }
                }
            }

            //从手牌中移除所有数量>=3张的组合
            removeAllKanAnTiAnPao(handCardsCopy, groupByNumberToArray);


            //吃检测&过张操作检测
            //能吃的牌一定要自己摸到的或者上家摸到的
            checkChiOrPassChi(card, isSelfMo, isPass, check, isCanChiPeng, table, currentCardIsMoFlag, fixdOutVals);

            //牌是摸出来的,检测平胡
            if (currentCardIsMoFlag && !isPass && !check.isChouZai() && !check.isZai() && !check.isTi()) {
                // 检查胡
                PaohuziHuLack lack = checkHu(card, isSelfMo);

                LogUtil.printDebug("平胡检测结果{},,,,,当前胡息:{}", lack.isHu(), getHuxi() + outHuXi + zaiHuxi);

                if (lack.isHu() && !isPass) {
                    check.setHu(true);
                }
            }

            //放炮验证,放炮是别人打出来的牌胡了
            //不是摸出来的牌, 上游会过滤当前出牌的玩家
            //组牌优先顺序为: 偎、臭偎或提> > 胡牌> > 跑或碰> > 吃
            if (((AxPaohuziTable) getPlayingTable()).getGameModel().getSpecialPlay().isIgnite() && !currentCardIsMoFlag && !isPass) {
                PaohuziHuLack paohuziHuLack = checkHu(card, false);

                LogUtil.printDebug("点炮检测结果{},,,,,当前胡息:{}, {}", paohuziHuLack.isHu(), paohuziHuLack.getHuxi(), getHuxi() + outHuXi + zaiHuxi);

                if (paohuziHuLack.isHu() && !isPass) {
                    check.setAuto(PaohzDisAction.action_hu, new ArrayList<>(Arrays.asList(card)));
                    check.setHu(true);
                }
            }

                //能胡的又是摸的，提示胡
            	if(check.isHu()&&table.isMoFlag())
                check.setPao(false);
        }

        check.buildActionList();
        return check;
    }

    /**
     * @param
     * @return in.chi true能吃
     * @description 吃检测&过张操作检测,能吃的牌一定要自己摸到的或者上家摸到的
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    private void checkChiOrPassChi(PaohzCard card, boolean isSelfMo, boolean isPass, PaohuziCheckCardBean check, boolean isCanChiPeng, AxPaohuziTable table, boolean currentCardIsMoFlag, List<Integer> fixdOutVals) {
        // 不是已经提跑栽过的牌&&(如果这张牌是摸的,摸上来的人也能检测||不管怎么样，摸牌打出的下家是一定要检测的)&&放招之后不能吃碰
        // 检测能做吃牌操作
        boolean isCheckChi = !fixdOutVals.contains(card.getVal()) && ((currentCardIsMoFlag && table.getMoSeat() == seat) || (table.calcNextSeat(table.getDisCardSeat()) == seat)) && !isFangZhao();

        //吃检测&过张操作检测
        if (isCanChiPeng && !check.isTi() && !check.isPao() && isCheckChi && !isPass) {
            //上家检测
            AxPaohuziPlayer frontPlayer = (AxPaohuziPlayer) table.getPlayerBySeat(table.calcFrontSeat(seat));
            List<PaohzCard> frontOutPaisCard = frontPlayer.getOutPaisCard();
            List<Integer> frontOutPais = PaohuziTool.toPhzCardVals(frontOutPaisCard, true);
            //当前玩家摸过的牌
            List<Integer> selfOutVals = PaohuziTool.toPhzCardVals(outPais, true);
            // System.out.println("前面过的牌:" + frontOutPais + "自己过的牌:" + selfOutVals + ",名字->" + name);

            List<Integer> passChiList = new ArrayList<>();
            if (currentCardIsMoFlag) {
                if (isSelfMo && !selfOutVals.isEmpty()) {// 自己摸的牌
                    // 如果是自己摸的牌把自己这张牌给去掉
                    selfOutVals.remove((int) (selfOutVals.size() - 1));
                } else {// 别人摸的牌
                    frontOutPais.remove((int) (frontOutPais.size() - 1));
                }
            } else {// 出的牌
            	if(frontOutPais.size()>0)
                frontOutPais.remove((int) (frontOutPais.size() - 1));
            }
            passChiList.addAll(frontOutPais);
            passChiList.addAll(selfOutVals);

            // System.out.println(name + "所有过掉的牌:" + passChiList);
            //检测是否能吃, 不是在这轮打出来的牌(已经检测过)&已经检测过不能吃的牌
            if (!passChiList.contains(card.getVal()) && !passChi.contains(card.getVal())) {
                //得到当前可吃的牌组
                List<PaohzCard> chiList = getChiList(card, null);
                if (!CollectionUtils.isEmpty(chiList)) {
                    check.setChi(true);
                } else {
                    // 不能吃后就再也不能吃了(从一开局手牌就是固定的, 一张牌检测下来不能吃以后也不能再吃的逻辑是对的)
                    if (!check.isPeng() && !check.isZai() && !check.isChouZai()) {
                        // pass(PaohzDisAction.action_chi, card.getVal());
                        passChi.add(card.getVal());
                    }
                }
            }
        }
    }

    /**
     * @param
     * @return
     * @description 从手牌中移除数量大于等于3张的卡组集合, 也就是坎提或者跑的组合
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    private void removeAllKanAnTiAnPao(List<PaohzCard> handCardsCopy, PaohzCardIndexArr groupByNumberToArray) {
        if (groupByNumberToArray == null) {
            PaohuziHandCard handCardsOperation = getPaohuziHandCard();
            groupByNumberToArray = handCardsOperation != null ? handCardsOperation.getGroupByNumberToArray() : null;
            if (groupByNumberToArray == null) {
                return;
            }
        }

        // 去掉4张和3张
        PaohuziIndex threeNumberIndex = groupByNumberToArray.getPaohzCardIndex(2);
        if (threeNumberIndex != null && !CollectionUtils.isEmpty(threeNumberIndex.getPaohzList())) {
            handCardsCopy.removeAll(threeNumberIndex.getPaohzList());
        }

        PaohuziIndex fourNumberIndex = groupByNumberToArray.getPaohzCardIndex(3);
        if (fourNumberIndex != null && !CollectionUtils.isEmpty(fourNumberIndex.getPaohzList())) {
            handCardsCopy.removeAll(fourNumberIndex.getPaohzList());
        }
    }

    /**
     * @param card     摸得牌
     * @param isSelfMo 是否自己摸出
     * @param cards
     * @return
     * @description 获得最大相同数, 去除当前摸得卡牌, 这种可能不存在(一副牌中不可能出现同一张牌的相同对象)
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    private List<PaohzCard> getSameCards(PaohzCard card, boolean isSelfMo, List<PaohzCard> cards) {
        Optional<List<PaohzCard>> sameCards = Optional.ofNullable(PaohuziTool.getSameCards(cards, card));
        if (isSelfMo && sameCards.isPresent()) {
            sameCards.get().remove(card);
        }
        return sameCards.isPresent() ? sameCards.get() : Collections.emptyList();
    }

    List<PaohzCard> getPengList(PaohzCard card, List<PaohzCard> cardList) {
        PaohuziHandCard handCard = getPaohuziHandCard();
        if (!handCard.isCanoperateCard(card)) {
            return null;
        }
        if (cardList != null) {
            // 吃牌不符合规则
            List<PaohzCard> gameList = PaohuziTool.getSameCards(cardList, card);
            if (gameList == null || gameList.isEmpty()) {
                return null;
            }
            if (gameList.size() != cardList.size()) {
                return null;
            }
        } else {
            cardList = PaohuziTool.getSameCards(handCard.getOperateCards(), card);
            if(cardList!=null&&cardList.size()==3){
            	cardList.remove(0);
            }
            if (cardList == null || cardList.size() != 2) {
                return null;
            }
        }
        return cardList;

    }

    /**
     * @param
     * @return
     * @description 该牌能否做吃操作
     */
    public List<PaohzCard> getChiList(PaohzCard card, List<PaohzCard> cardList) {
        PaohuziHandCard handCard = getPaohuziHandCard();
        if (!handCard.isCanoperateCard(card)) {
            return null;
        }
        if (cardList != null) {
            List<PaohzCard> sameList = PaohuziTool.findPhzByVal(cardList, card.getVal());
            List<PaohzCard> handSameList = PaohuziTool.findPhzByVal(handCard.getOperateCards(), card.getVal());
//            if (!handSameList.isEmpty() && !sameList.containsAll(handSameList)) {
//                return null;
//            }
            List<PaohzCard> copy = new ArrayList<>(cardList);
            // 吃牌不符合规则
            if (copy.contains(card)) {
                copy.remove(card);
            }

            List<PaohzCard> chiList = PaohuziTool.checkChi(copy, card, getPlayingTable(AxPaohuziTable.class).getGameModel());
            if (chiList == null || chiList.isEmpty()) {
                return null;
            }
            // 找出有没有要吃的相同的牌
            if (sameList == null || sameList.isEmpty()) {
                // 如果没有其他相同的牌 直接吃
                return cardList;
            } else {
                // 有相同的牌
                List<PaohzCard> allChi = new ArrayList<>();
                allChi.addAll(chiList);
                //
                copy.removeAll(chiList);
                for (PaohzCard sameCard : sameList) {
                    if (!copy.contains(sameCard)) {
                        continue;
                    }
                    // 相同的牌还能不能继续吃
                    List<PaohzCard> samechiList = PaohuziTool.checkChi(copy, sameCard, getPlayingTable(AxPaohuziTable.class).getGameModel());
                    if (samechiList == null || samechiList.isEmpty()) {
                        // 如果不能吃 则这个牌不能吃
                        return null;
                    }

                    // 添加相同的牌 的吃
                    copy.removeAll(samechiList);
                    samechiList.add(0, sameCard);
                    allChi.addAll(samechiList);
                }
                return cardList;
            }
        } else {
            cardList = PaohuziTool.checkChi(handCard.getOperateCards(), card, getPlayingTable(AxPaohuziTable.class).getGameModel());
            if (cardList == null || cardList.isEmpty()) {
                return null;
            }
        }

        if (cardList != null) {
            if (cardList.contains(card)) {
                cardList.remove(card);
            }
            //手牌内移除可提供吃操作的另外两张牌
            handCard.getOperateCards().removeAll(cardList);
        }

        //寻找另外可吃的组合
       // List<PaohzCard> sameList = PaohuziTool.findCountByVal(handCard.getOperateCards(), card, true);
        // 找出有没有要吃的相同的牌
//        if (sameList == null || sameList.isEmpty()) {
            // 如果没有其他相同的牌 直接吃
            return cardList;
//        } else {
//            // 有相同的牌
//            List<PaohzCard> allChi = new ArrayList<>();
//            allChi.addAll(cardList);
//            //
//            handCard.getOperateCards().removeAll(cardList);
//            for (PaohzCard sameCard : sameList) {
//                if (!handCard.getOperateCards().contains(sameCard)) {
//                    continue;
//                }
//                // 相同的牌还能不能继续吃
//                List<PaohzCard> samechiList = PaohuziTool.checkChi(handCard.getOperateCards(), sameCard, getPlayingTable(AxPaohuziTable.class).getGameModel());
//                if (samechiList == null || samechiList.isEmpty()) {
//                    // 如果不能吃 则这个牌不能吃
//                    return null;
//                }
//
//                // 添加相同的牌 的吃
//                handCard.getOperateCards().removeAll(samechiList);
//                samechiList.add(0, sameCard);
//                allChi.addAll(samechiList);
//            }
//            return allChi;
//        }

    }

    @Override
    public void changeState(player_state state) {
        this.state = state;
        changeTableInfo();

    }

    @Override
    public void initNext() {
        setHu(null);
        setPoint(0);
//        setWinLossPoint(0);
        setFristDisCard(true);
        setFirstDisSingleCard(false);
        tingFlag = 0;
        setZaiHuxi(0);
        cardTypes.clear();
        handPais.clear();
        outPais.clear();
        peng.clear();
        chi.clear();
        pao.clear();
        zai.clear();
        chouZai.clear();
        ti.clear();
        passChi.clear();
        passPeng.clear();
        limitChuCardVals.clear();
        chaiKanOrLCardVals.clear();
        setPassHuCount(0);
        setOutHuxi(0);
        setTuanYuanXi(0);
        setHuxi(0);
        setOweCardCount(0);
        setFangZhao(0);
        setFirstTuanYuanXi(0);
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(player_state.entry);
    }

    public List<PaohzCard> getHandPhzs() {
        return handPais;
    }

    @Override
    public List<Integer> getHandPais() {
        return PaohuziTool.toPhzCardIds(handPais);
    }

    public List<Integer> getOutPais() {
        return PaohuziTool.toPhzCardIds(outPais);
    }

    public List<PaohzCard> getOutPaisCard() {
        return outPais;
    }
    public void addLimitChuCardVal(int val) {
    	boolean add =false;
    	for(PaohzCard card: handPais){
    		if(card.getVal()==val){
    			add = true;
    			break;
    		}
    	}
    	if(add){
    		 if(!limitChuCardVals.contains(val))
    	        	limitChuCardVals.add(val);
    	}
       
    }
    
    
    public List<Integer> getLimitChuCardVals() {
        return limitChuCardVals;
    }
    
    public void addChaiKanOrLCardVal(int val) {
        if(!chaiKanOrLCardVals.contains(val))
        	chaiKanOrLCardVals.add(val);
    	
    }
    
    
    public List<Integer> getChaiKanOrLCardVals() {
        return chaiKanOrLCardVals;
    }
    
    
    /**
     * 出牌
     *
     * @return
     */
    public String buildOutPaiStr() {
        StringBuilder sb = new StringBuilder();
        List<Integer> outPais = getOutPais();
        sb.append(StringUtil.implode(outPais)).append(";");
        for (CardTypeHuxi huxi : cardTypes) {
            sb.append(huxi.toStr()).append(";");
        }

        return sb.toString();
    }

    /**
     * 手牌
     *
     * @return
     */
    public String buildHandPaiStr() {
        StringBuilder sb = new StringBuilder();
        List<Integer> outPais = getHandPais();
        List<Integer> tiPais = PaohuziTool.toPhzCardIds(ti);
        List<Integer> paoPais = PaohuziTool.toPhzCardIds(pao);
        List<Integer> zaiPais = PaohuziTool.toPhzCardIds(zai);
        List<Integer> pengPais = PaohuziTool.toPhzCardIds(peng);
        List<Integer> chiPais = PaohuziTool.toPhzCardIds(chi);
        List<Integer> chouZaiPais = PaohuziTool.toPhzCardIds(chouZai);

        sb.append(StringUtil.implode(outPais)).append(";");
        sb.append(StringUtil.implode(tiPais)).append(";");
        sb.append(StringUtil.implode(paoPais)).append(";");
        sb.append(StringUtil.implode(zaiPais)).append(";");
        sb.append(StringUtil.implode(pengPais)).append(";");
        sb.append(StringUtil.implode(chiPais)).append(";");
        sb.append(StringUtil.implode(chouZaiPais)).append(";");
        return sb.toString();
    }

    @Override
    public String toInfoStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(getUserId()).append(",");
        sb.append(seat).append(",");
        int stateVal = 0;
        if (state != null) {
            stateVal = state.getId();
        }
        sb.append(stateVal).append(",");
        sb.append(isEntryTable).append(",");
        sb.append(StringUtil.implode(passPeng, "_")).append(",");
        sb.append(StringUtil.implode(passChi, "_")).append(",");
        sb.append(outHuXi).append(",");
        sb.append(huxi).append(",");
        sb.append(winCount).append(",");
        sb.append(lostCount).append(",");
        sb.append(lostPoint).append(",");
        sb.append(point).append(",");
        sb.append(getTotalPoint()).append(",");
        sb.append(oweCardCount).append(",");
        sb.append(isFristDisCard ? 1 : 0).append(",");
        sb.append(getMaxPoint()).append(",");
        sb.append(zaiHuxi).append(",");
        sb.append(fangZhao).append(",");
        sb.append(StringUtil.implode(firstPais, "_")).append(",");
        sb.append(StringUtil.implode(actionTotalArr, "_")).append(",");
        sb.append(StringUtil.implode(chaiKanOrLCardVals, "_")).append(",");
        sb.append(StringUtil.implode(limitChuCardVals, "_")).append(",");
        sb.append(onlyHht).append(",");
        sb.append(disNum).append(",");
        sb.append(firstTuanYuanXi).append(",");
//        sb.append(winLossPoint).append(",");
        sb.append(awardWinLossPoint).append(",");
        sb.append(firstDisSingleCard?1:0).append(",");
        sb.append(tingFlag).append(",");
        sb.append(passHuCount).append(",");
        sb.append(tuanYuanXi).append(",");
        return sb.toString();
    }

    /**
     * 单局详情
     */
    public ClosingPhzPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
        ClosingPhzPlayerInfoRes.Builder res = ClosingPhzPlayerInfoRes.newBuilder();
        res.setUserId(userId + "");
        res.addAllCards(getHandPais());
        res.setName(name);
        res.setPoint(point);        //小结共计
        res.setTotalPoint(getWinLossPoint());//大结总分
        res.setSeat(seat);
        res.setAllHuxi(getTotalHu());
        res.setFinalPoint(getTotalPoint());
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }

        res.setSex(sex);
        return res;
    }

    public int roundNumber(int number) {
        int ret = 0;
        if (number > 0) {
            ret = (number + 5) / 10 * 10;
        } else if (number < 0) {
            ret = (number - 5) / 10 * 10;
        }

        return ret;
    }
    
    
    
    
    

    /**
     * 总局详情
     *
     * @return
     */
    public ClosingPhzPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
        ClosingPhzPlayerInfoRes.Builder res = bulidOneClosingPlayerInfoRes();
        res.setLostCount(lostCount);
        res.setWinCount(winCount);
        res.setMaxPoint(getMaxPoint());
        return res;
    }

    @Override
    public PlayerInTableRes.Builder buildPlayInTableInfo() {
        return buildPlayInTableInfo(0, false);
    }

    public PlayerInTableRes.Builder buildPlayInTableInfo(long lookUid, boolean isrecover) {
        PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
        res.setUserId(userId + "");
        if (!StringUtils.isBlank(ip)) {
            res.setIp(ip);
        } else {
            res.setIp("");
        }

        AxPaohuziTable table = getPlayingTable(AxPaohuziTable.class);

        if (table == null) {
            LogUtil.e("userId=" + this.userId + "table null-->" + getPlayingTableId());
            return null;
        }

        res.setPoint(getTotalPoint());
        // 当前出的牌
        res.addAllOutedIds(getOutPais());
        
        
        res.addAllMoldIds(limitChuCardVals);

        if (seat == table.getShuXingSeat()) {
            AxPaohuziPlayer paohuziPlayer = (AxPaohuziPlayer) table.getSeatMap().get(table.getLastWinSeat());

            int outHuxi = paohuziPlayer.getOutHuxi();
            // if (lookUid == userId) {
            outHuxi += paohuziPlayer.getZaiHuxi();
            // }

            res.addExt(outHuxi); // 0
            res.addExt(paohuziPlayer.getFangZhao());// 1
        } else {
            int outHuxi = getOutHuxi();
            // if (lookUid == userId) {
            outHuxi += zaiHuxi;
            // }

            res.addExt(outHuxi);// 0
            res.addExt(fangZhao);// 1
        }
        res.addExt((table.getMasterId() == this.userId) ? 1 : 0);// 2

//		if (table.isSiRenBoPi()) {
//			res.addExt(table.getShuXingSeat());// 3
//			res.addExt(table.getState().getId());// 4
//        }else{
        res.addExt(zaiHuxi);// 3
        res.addExt(0);// 4
//        }
        res.addExt(table.isGoldRoom() ? (int) loadAllGolds() : 0);// 5
        res.addExt(isAutoPlay() ? 1 : 0);// 6
        res.addExt(getAutoPlayCheckedTime() > table.getAutoTimeOut() ? 1 : 0);//7
//        res.addExt(chui);//8
        res.setName(name);
        res.setSeat(seat);
        res.setSex(sex);

        res.addAllMoldCards(buildPhzCards(table, lookUid));

        List<PaohzCard> nowDisCard = table.getNowDisCardIds();
        if (table.getDisCardSeat() == seat /**|| (table.getDisCardSeat()==table.getLastWinSeat()&&seat==table.getShuXingSeat())**/) {
            if (nowDisCard != null && !nowDisCard.isEmpty()) {
                int selfMo = 0;
                if (table.isSelfMo(this)) {
                    selfMo = 1;

                }
                res.addOutCardIds(selfMo);
                PaohzCard zaiCard = table.getZaiCard();
                PaohzCard beremoveCard = table.getBeRemoveCard();
                if (zaiCard != null && nowDisCard.contains(zaiCard)) {
                    // 如果栽了牌
                } else if (beremoveCard != null && nowDisCard.contains(beremoveCard)) {
                    // 被移掉的牌
                } else {
                    res.addAllOutCardIds(PaohuziTool.toPhzCardIds(nowDisCard));

                }
            }

        }

        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());

        } else {
            res.setIcon("");

        }

        if (state == player_state.ready || state == player_state.play) {
            // 玩家装备已经准备和正在玩的状态时通知前台已准备
            res.setStatus(PaohuziConstant.state_player_ready);
        } else {
            res.setStatus(0);
        }

        if (isrecover) {
            // 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
            List<Integer> recover = new ArrayList<>();
            recover.add(isEntryTable);
            res.addAllRecover(recover);
        }

        //信用分
        if (table.isCreditTable()) {
            GroupUser gu = getGroupUser();
            String groupId = table.loadGroupId();
            if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
                gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
            }
            res.setCredit(gu != null ? gu.getCredit() : 0);
        }
        return buildPlayInTableInfo1(res);

    }

    public boolean isAlreadyMo() {
        int count = handPais.size();
        count += outPais.size();
        count += ti.size();
        count += chi.size();
        count += peng.size();
        count += zai.size();
        count += chouZai.size();
        return count == 21;

    }

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;

            for (String value : values) {
                i++;
                if (i == 0) {
                    this.outPais = PaohuziTool.explodePhz(value, ",");
                } else {
                    CardTypeHuxi type = new CardTypeHuxi();
                    type.init(value);
                    cardTypes.add(type);
                }
            }
        }
        if (!StringUtils.isBlank(handPai)) {
            String[] values = handPai.split(";");
            int i = 0;
            String outPaiStr = StringUtil.getValue(values, i++);
            String tiPaiStr = StringUtil.getValue(values, i++);
            String paoPaiStr = StringUtil.getValue(values, i++);
            String zaiPaiStr = StringUtil.getValue(values, i++);
            String pengPaiStr = StringUtil.getValue(values, i++);
            String chiPaiStr = StringUtil.getValue(values, i++);
            String chouZaiPaiStr = StringUtil.getValue(values, i++);
            this.handPais = PaohuziTool.explodePhz(outPaiStr, ",");
            this.ti = PaohuziTool.explodePhz(tiPaiStr, ",");
            this.pao = PaohuziTool.explodePhz(paoPaiStr, ",");
            this.zai = PaohuziTool.explodePhz(zaiPaiStr, ",");
            this.peng = PaohuziTool.explodePhz(pengPaiStr, ",");
            this.chi = PaohuziTool.explodePhz(chiPaiStr, ",");
            this.chouZai = PaohuziTool.explodePhz(chouZaiPaiStr, ",");

        }
    }

    @Override
    public void endCompetition1() {

    }

    /**
     * @param list
     * @description 发牌
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public void dealHandPais(List<PaohzCard> list) {
        this.handPais = list;
        setFristDisCard(true);
        setTingFlag(0);
        setFirstDisSingleCard(false);
        getPlayingTable().changeCards(seat);
        
        
        
        List<PaohzCard> copy = new ArrayList<>(handPais);
        Map<Integer, Integer> valMap = PaohuziTool.toPhzValMap(copy);
        for (Entry<Integer, Integer> entry : valMap.entrySet()) {
        	if(entry.getValue()>2){
        		addLimitChuCardVal(entry.getKey());
        		addChaiKanOrLCardVal(entry.getKey());
        	}
        	
        	if(entry.getValue()==4){
        		int pai = entry.getKey()%100;
        		int tyXi= 0;
        		if(isRead(pai)){
        			tyXi = 8;
        		}else{
        			tyXi = 6;
        		}
        		changeFirstTYxi(tyXi);
        		changeTuanYuanXi(tyXi);
        	}
        	
        }
        
        
    }
    
    
    private static boolean isRead(int pai){
    	if(pai==2||pai==7||pai==10){
    		return true;
    	}
    	return false;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getLostCount() {
        return lostCount;
    }

    public void setLostCount(int lostCount) {
        this.lostCount = lostCount;
    }

    public int getLostPoint() {
        return lostPoint;
    }

    public void setLostPoint(int lostPoint) {
        this.lostPoint = lostPoint;
    }

    public int getPoint() {
        return point;
    }
    public int getPassHuCount() {
        return passHuCount;
    }
    
    
    public void setPassHuCount(int passCount) {
        this.passHuCount = passCount;
    }
    


    public void calcResult(BaseTable table, int count, int point, boolean huangzhuang) {
        if (!huangzhuang) {
            if (point > 0) {
                this.winCount += count;
            } else {
                this.lostCount += count;
            }
        }
        changeWinLossPoint(point);
        changePoint(point);
        if (table != null && table.isGoldRoom()) {
            getGoldPlayer().changePlayCount();
            if (point > 0) {
                getGoldPlayer().changeWinCount();
                if (!isRobot()) {
                    GoldDao.getInstance().updateGoldUserCount(userId, 1, 0, 0, 1);
                }
            } else {
                getGoldPlayer().changeLoseCount();
                if (!isRobot()) {
                    GoldDao.getInstance().updateGoldUserCount(userId, 0, 1, 0, 1);
                }
            }
        }
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void changePoint(int point) {
        this.point += point;
        myExtend.changePoint(getPlayingTable().getPlayType(), point);
        changeTotalPoint(point);
        if (point > getMaxPoint()) {
            setMaxPoint(point);
        }
        changeTableInfo();
    }

    public int getOweCardCount() {
        return oweCardCount;
    }

    public void setOweCardCount(int oweCardCount) {
        this.oweCardCount = oweCardCount;
    }

    public int getOutHuxi() {
        return outHuXi;
    }

    public void setOutHuxi(int chiHuxi) {
        this.outHuXi = chiHuxi;
        changeTableInfo();
    }
    public int getTuanYuanXi() {
        return tuanYuanXi;
    }

    public void setTuanYuanXi(int tuanyuanXi) {
        this.tuanYuanXi = tuanyuanXi;
        changeTableInfo();
    }
    public void changeTuanYuanXi(int huxi) {
        if (huxi != 0) {
            this.tuanYuanXi += huxi;
            changeTableInfo();
            // writeErrMsg("出的牌总胡息:" + this.outHuXi + "加的胡息:" + huxi);
        }
    }
    

    public void changeOutCardHuxi(int huxi) {
        if (huxi != 0) {
            this.outHuXi += huxi;
            changeTableInfo();
            // writeErrMsg("出的牌总胡息:" + this.outHuXi + "加的胡息:" + huxi);
        }
    }

    public int getZaiHuxi() {
        return zaiHuxi;
    }

    public void setZaiHuxi(int zaiHuxi) {
        this.zaiHuxi = zaiHuxi;
    }

    public void changeZaihu(int zaiHuxi) {
        this.zaiHuxi += zaiHuxi;
        changeTableInfo();
    }

    public int getHuxi() {
        return huxi;
    }

    public void setHuxi(int huxi) {
        if (this.huxi != huxi) {
            this.huxi = huxi;
        }
    }

    /**
     * 总胡息
     *
     * @return
     */
    public int getTotalHu() {
        return outHuXi + huxi + zaiHuxi+tuanYuanXi;
    }

    public List<PhzHuCards> buildPhzHuCards() {
        List<PhzHuCards> list = new ArrayList<>();
        for (CardTypeHuxi type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        if (hu != null && hu.getPhzHuCards() != null) {
            for (CardTypeHuxi type : hu.getPhzHuCards()) {
                list.add(type.buildMsg().build());
            }
        }

        return list;
    }

    public List<PhzHuCards> buildNormalPhzHuCards() {
        List<PhzHuCards> list = new ArrayList<>();
        for (CardTypeHuxi type : cardTypes) {
            list.add(type.buildMsg().build());
        }

        if (hu != null && hu.getPhzHuCards() != null) {
            for (CardTypeHuxi type : hu.getPhzHuCards()) {
                list.add(type.buildMsg().build());
            }
        } else {
            PhzHuCards.Builder msg = PhzHuCards.newBuilder();
            msg.addAllCards(handPais.stream().map(v -> v.getId()).collect(Collectors.toList()));
            msg.setAction(0);
            msg.setHuxi(0);
            list.add(msg.build());
        }


        return list;
    }

    public List<PhzHuCards> buildPhzCards(AxPaohuziTable table, long lookUid) {
        List<PhzHuCards> list = new ArrayList<>();
        for (CardTypeHuxi type : cardTypes) {
            if (type.getAction() == PaohzDisAction.action_zai) {
                // 不是本人并且是栽
                if (table.getShuXingSeat() == this.userId && lookUid == table.getLastWinSeat()) {
                    list.add(type.buildMsg().build());
                } else {
                    if (lookUid != this.userId) {
                        list.add(type.buildMsg(true).build());
                    } else {
                        list.add(type.buildMsg().build());
                    }
                }
            } else {
                list.add(type.buildMsg().build());
            }
        }

        return list;
    }

    /**
     * 胡牌得到的积分
     *
     * @return
     */
    public int calcHuPoint(int total, int xiToTun) {
        if (xiToTun == 0 || total == 0) {
            return 0;
        }

        return total / xiToTun;
    }


    public List<PaohzCard> getAllCards() {
        List<PaohzCard> cards = new ArrayList<>();
        cards.addAll(ti);
        cards.addAll(pao);
        cards.addAll(zai);
        cards.addAll(chouZai);
        cards.addAll(peng);
        cards.addAll(chi);
        cards.addAll(handPais);
        return cards;
    }

    /**
     * 是否栽过的牌跑
     */
    boolean isZaiPao(int val) {
        return PaohuziTool.isHasCardVal(zai, val);
    }

    /**
     * 得到要提的栽的牌
     */
    List<PaohzCard> getTiCard(PaohzCard card) {
        List<PaohzCard> list = PaohuziTool.getSameCards(zai, card);
        if (list == null || list.isEmpty()) {
            list = PaohuziTool.getSameCards(handPais, card);
        }
        return list;
    }

    public PaohuziHuLack getHu() {
        return hu;
    }

    public void setHu(PaohuziHuLack hu) {
        this.hu = hu;
    }
//
//    public static void main(String[] args) {
////        List<Integer> list1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
////        System.out.println(list1.subList(0, 3));
////        System.out.println(list1);
//
//        XtPaohuziPlayer xtPaohuziPlayer = new XtPaohuziPlayer();
//        XtPaohuziTable table;
//        xtPaohuziPlayer.setTable(table=new XtPaohuziTable());
//        xtPaohuziPlayer.setPlayingTableId(1000);
//        table.setId(xtPaohuziPlayer.getPlayingTableId());
//        table.setMoFlag(1);
//        xtPaohuziPlayer.setHuxi(20);
//        xtPaohuziPlayer.handPais = new ArrayList<>(Arrays.asList(
//                PaohzCard.phz1,PaohzCard.phz11,PaohzCard.phz21,
////                PaohzCard.phz2,PaohzCard.phz22,PaohzCard.phz32,
//                PaohzCard.phz3
//        ));
//
//        table.checkMo();
//        PaohuziCheckCardBean paohuziCheckCardBean = xtPaohuziPlayer.checkCard(PaohzCard.phz13, true, false, false, false, false);
//
//        System.out.println(paohuziCheckCardBean.buildActionList());
//
//        System.out.println(paohuziCheckCardBean.isHu());
//    }

    public boolean isFristDisCard() {
        return isFristDisCard;
    }

    public void setFristDisCard(boolean isFristDisCard) {
        if (this.isFristDisCard != isFristDisCard) {
            this.isFristDisCard = isFristDisCard;
            changeTableInfo();
        }
    }

    public void setFirstDisSingleCard(boolean firstDisSingleCard) {
        if (this.firstDisSingleCard != firstDisSingleCard) {
            this.firstDisSingleCard = firstDisSingleCard;
            changeTableInfo();
        }
    }
    
    
    
    public void setTingFlag(int flag){
    	this.tingFlag = flag;
    	changeTableInfo();
    }
    
    public int getTingFlag(){
    	return tingFlag;
    }
    
    
    
    
    

    public int getFangZhao() {
        return fangZhao;
    }

    public void setFangZhao(int fangZhao) {
        this.fangZhao = fangZhao;
        changeTableInfo();
    }

    public boolean isFangZhao() {
        return 1 == fangZhao;
    }

    public List<Integer> getPassChi() {
        return passChi;
    }

    public void removePassChi(int val) {
        if (passChi.contains(val)) {
            int index = passChi.indexOf(val);
            passChi.remove(index);
            changeTableInfo();
        }
    }

    @Override
    public int loadScore() {
        AxPaohuziTable table = getPlayingTable(AxPaohuziTable.class);
        if (table.isGoldRoom()) {
            return getPoint();
        } else {
            return getTotalPoint();
        }
    }

    public boolean isCheckAuto() {
        return isCheckAuto;
    }

    public void setCheckAuto(boolean checkAuto) {
        isCheckAuto = checkAuto;
    }


    /**
     * @param cls XtPaohuziPlayer
     * @description 反射加载Method每个类对应一个
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, CommandProcessor.getInstance());
        }
    }



    public long getLastOperateTime() {
        return lastOperateTime;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public long getAutoPlayTime() {
        return autoPlayTime;
    }

    public void setAutoPlayTime(long autoPlayTime) {
        this.autoPlayTime = autoPlayTime;
    }

    public int[] getActionTotalArr() {
        return actionTotalArr;
    }

    public List<CardTypeHuxi> getCardTypes() {
        return cardTypes;
    }

    public int getWinLossPoint() {
        return winLossPoint;
    }

//    public void setWinLossPoint(int winLossPoint) {
//        this.winLossPoint = winLossPoint;
//        changeExtend();
//    }

    public void setAwardWinLossPoint(int awardWinLossPoint) {
        this.awardWinLossPoint = awardWinLossPoint;
        changeExtend();
    }

    public void changeWinLossPoint(int winLossPoint) {
//        this.winLossPoint += winLossPoint;
    }

    @Override
    public player_state getState() {
        return state;
    }

    @Override
    public int getIsEntryTable() {
        return isEntryTable;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

}
