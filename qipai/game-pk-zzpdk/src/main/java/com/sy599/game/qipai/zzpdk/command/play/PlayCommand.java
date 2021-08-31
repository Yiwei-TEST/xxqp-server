package com.sy599.game.qipai.zzpdk.command.play;

import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.qipai.zzpdk.bean.ZZPdkPlayer;
import com.sy599.game.qipai.zzpdk.bean.ZZPdkTable;
import com.sy599.game.qipai.zzpdk.util.CardUtils;
import com.sy599.game.qipai.zzpdk.util.CardUtils.Result;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class PlayCommand extends BaseCommand<ZZPdkPlayer> {
    @Override
    public void execute(ZZPdkPlayer player, MessageUnit message) throws Exception {
        ZZPdkTable table = player.getPlayingTable(ZZPdkTable.class);
        if (table == null) {
            return;
        }
        PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
        StringBuilder sb = new StringBuilder("zzpdk");
        sb.append("|").append(table.getId());
        sb.append("|").append(table.getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("PlayCommand");
        sb.append("|").append(playCard.getCardType());
        sb.append("|").append(playCard.getCardIdsList());
        if (table.getState() != table_state.play) {
            return;
        }

        if (player.getSeat() != table.getNextDisCardSeat()) {
            return;
        }

        int action = playCard.getCardType();
        if (action == 100) {//取消托管
            player.setAutoPlay(false, table);
            player.setLastOperateTime(System.currentTimeMillis());
            return;
        } else if (action == 101) {//托管
            player.setAutoPlay(true, table);
            return;
        }

        List<Integer> cards = new ArrayList<>(playCard.getCardIdsList());

        List<Integer> disCardIds = table.getNowDisCardIds();

        boolean notNeedCalc = (cards.size() == 0 || (cards.size() == 1 && cards.get(0).intValue() == 0));
        CardUtils.Result result;
        if (notNeedCalc) {
            result = new CardUtils.Result(CardUtils.Result.undefined, 0, 0);
        } else {
            result = CardUtils.calcCardValue(CardUtils.loadCards(cards), table ,false);
        }

        if (result.getType() > 0) {
            synchronized (table) {

                if (player.getSeat() != table.getNextDisCardSeat()) {
                    sb.append("|").append(4);
                    LogUtil.errorLog.error(sb.toString());
                    return;
                }

                if (cards.size() > 0) {
                    if (table.checkIsChai4Zha(player, cards)) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_70));
                        sb.append("|").append("chai4Zha");
                        LogUtil.errorLog.error(sb.toString());
                        return;
                    }
                    if(table.checkBaoDan(player,cards)){
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_74));
                        sb.append("|").append("baoDan");
                        LogUtil.errorLog.error(sb.toString());
                        return;
                    }
                }



                // 该牌局是否能打牌
                int canPlay = table.isCanPlay();
                //其他玩家掉线也可以继续出牌
                if (canPlay != 0 && canPlay != 2) {
                    if (canPlay == 1) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
                    }
                    sb.append("|").append(5);
                    LogUtil.errorLog.error(sb.toString());
                    return;
                }
                // 自己要牌
                LogUtil.d_msg(player.getUserId() + "-discard-" + JacksonUtil.writeValueAsString(cards) + ":" + result.toString());
//				if (cardType == CardType.c0) {
//					// 牌型检查错误
//					return;
//				}
                // 是否第一局必出黑桃3
				if (table.getIsFirstRoundDisThree() == 1) {
					if (table.getPlayBureau() == 1 && table.getDisCardRound() == 0) {
						if (player.getHandPais().contains(403) && !cards.contains(403)) {
							player.writeErrMsg(LangHelp.getMsg(LangMsg.code_17));
							return;
						}

						if (player.getHandPais().contains(406) && !cards.contains(406)
								&& (table.getPlayType() == GameUtil.play_type_11)) {
							player.writeErrMsg("开房第一局出的牌必须包含黑桃六");
							return;
						}

					}
				}

                List<Integer> list = new ArrayList<>(player.getHandPais());
                for (Integer tempCard : cards) {
                    if (!list.remove(tempCard)) {
                        LogUtil.d_msg(player.getUserId() + "-discard-no cards-->" + JacksonUtil.writeValueAsString(cards) + ",handCards=" + player.getHandPais());
                        sb.append("|").append(6);
                        LogUtil.errorLog.error(sb.toString());
                        return;
                    }
                }

                if (table.getDisCardSeat() != player.getSeat()) {
                    // 上一张的牌不是自己出的
                    if (disCardIds != null && disCardIds.size() > 0) {
                        // 如果出的是单张下一家出牌只剩下一张了只能出最大的
                        if (cards.size() == 1 && table.getSeatMap().get(table.getNextSeat(player.getSeat())).getHandPais().size() == 1) {
                            List<Integer> cards0 = player.getHandPais();
                            CardUtils.sortCards(cards0);
                            if (CardUtils.loadCardValue(cards.get(0)) != CardUtils.loadCardValue(cards0.get(cards0.size() - 1))) {
                                sb.append("|").append(7);
                                LogUtil.errorLog.error(sb.toString());
                                return;
                            }
                        }

                        Result result1 = table.getCardClas();
                        if (result1.getType() <= 0) {
                            return;
                        } else {
                            Integer compare = CardUtils.compareTo(result1, result, table);
                            if (compare==null||compare<0) {
                                return;
                            }
                        }
                    }
                } else {
                    table.clearIsNotLet();
                }
                if (!player.isAutoPlay()) {
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
            }

        } else {
            sb.append("|").append(8);
            LogUtil.errorLog.error(sb.toString());
            return;
        }

        table.playCommand(player, cards,result);
    }

    @Override
    public void setMsgTypeMap() {
        msgTypeMap.put(PlayCardRes.class, WebSocketMsgType.sc_playcardres);
        msgTypeMap.put(ClosingInfoRes.class, WebSocketMsgType.sc_closinginfores);
    }

}
