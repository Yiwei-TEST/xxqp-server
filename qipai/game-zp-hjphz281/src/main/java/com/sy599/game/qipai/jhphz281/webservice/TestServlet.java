package com.sy599.game.qipai.jhphz281.webservice;

import com.sy.mainland.util.IpUtil;
import com.sy599.game.base.BaseTable;
import com.sy599.game.manager.TableManager;
import com.sy599.game.qipai.jhphz281.bean.JhPhzPlayer;
import com.sy599.game.qipai.jhphz281.bean.JhPhzTable;
import com.sy599.game.qipai.jhphz281.util.JhPhzCardUtils;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class TestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public TestServlet() {
    }

    public void init() throws ServletException {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(new StringBuilder(30).append("text/html;charset=").append("UTF-8").toString());
        response.setCharacterEncoding("UTF-8");
        String tableIdStr = request.getParameter("tableId");
        String userCards = request.getParameter("userCards");

        String ip = IpUtil.getIpAddr(request);

        LogUtil.msgLog.info("ip=" + ip + ",tableId=" + tableIdStr + ",userCards=" + userCards);
        try {
            if (StringUtils.isBlank(userCards)) {
                writeMsg(response, "param error");
                return;
            }
            if (isDigits(tableIdStr)) {
                int tableId = Integer.parseInt(tableIdStr);
                BaseTable table = TableManager.getInstance().getTable(tableId);
                if (table == null) {
                    writeMsg(response, "table is null");
                } else {
                    if (table instanceof JhPhzTable) {
                        JhPhzTable table1 = (JhPhzTable) table;
                        String[] ucs = userCards.split(";");
                        if (ucs.length == 1 && isDigits(ucs[0])) {
                            int val = Integer.parseInt(ucs[0]);
                            if (val <= 0) {
                                writeMsg(response, table1.getLeftCards().toString());
                                return;
                            }
                            synchronized (table1) {
                                Integer card = null;
                                for (Integer temp : table1.getLeftCards()) {
                                    if (val == JhPhzCardUtils.loadCardVal(temp)) {
                                        card = temp;
                                        break;
                                    }
                                }
                                if (card != null) {
                                    table1.getLeftCards().remove(card);
                                    table1.getLeftCards().add(0, card);
                                    writeMsg(response, "ok");
                                } else {
                                    writeMsg(response, "没有这个牌了");
                                }
                                return;
                            }
                        } else if (ucs.length >= 1 || ucs.length <= 4) {
                            synchronized (table1) {
                                if (table1.getMoCount() == 0 && table1.getDisCount() == 0) {
                                    List<Integer> list1 = StringUtil.explodeToIntList(ucs[0], ",");
                                    Map<Integer, List<Integer>> map = new HashMap<>();
                                    map.put(list1.get(0), list1);

                                    List<Integer> list = new ArrayList<>();
                                    list.add(list1.remove(0));
                                    if (ucs.length > 1) {
                                        List<Integer> list2 = StringUtil.explodeToIntList(ucs[1], ",");
                                        map.put(list2.get(0), list2);
                                        list.add(list2.remove(0));
                                    }
                                    if (ucs.length > 2) {
                                        List<Integer> list3 = StringUtil.explodeToIntList(ucs[2], ",");
                                        map.put(list3.get(0), list3);
                                        list.add(list3.remove(0));
                                    }
                                    if (ucs.length > 3) {
                                        List<Integer> list4 = StringUtil.explodeToIntList(ucs[3], ",");
                                        map.put(0, list4);
                                    }
                                    boolean is2 = table1.getMaxPlayerCount() == 2;
                                    Collections.sort(list);
                                    if (list.get(0).intValue() > 0) {
                                        int user1 = list.get(0);
                                        if (list.size() == 1) {
                                            JhPhzPlayer player1 = table1.getPlayer(user1, JhPhzPlayer.class);
                                            if (player1 != null && (player1.getHandCards().SRC.size() == map.get(user1).size())
                                                    && player1.getSeat() != table1.getShuXingSeat()) {

                                                Set<Integer> card1 = new HashSet<>(map.get(user1));
                                                if (card1.size() == map.get(user1).size()) {
                                                    List<Integer> allList = JhPhzCardUtils.loadCards(table1.getWangbaCount() > 0 ? table1.getWangbaCount() : 0);
                                                    if (allList.containsAll(map.get(user1))) {
                                                        allList.removeAll(map.get(user1));

                                                        List<Integer> list2 = new ArrayList<>();
                                                        List<Integer> list3;
                                                        if (is2) {
                                                            list3 = null;
                                                        } else {
                                                            list3 = new ArrayList<>();
                                                        }
                                                        List<Integer> list4 = new ArrayList<>();
                                                        int len = card1.size() == 21 ? 20 : 21;
                                                        for (int i = 0; i < len; i++) {
                                                            list2.add(allList.remove(0));
                                                        }
                                                        len = 20;
                                                        if (!is2) {
                                                            for (int i = 0; i < len; i++) {
                                                                list3.add(allList.remove(0));
                                                            }
                                                        }

                                                        list4.addAll(allList);

                                                        int seat = table1.calcNextSeat(player1.getSeat());
                                                        JhPhzPlayer player2 = (JhPhzPlayer) table1.getSeatMap().get(seat);
                                                        seat = table1.calcNextSeat(seat);
                                                        JhPhzPlayer player3;
                                                        if (is2) {
                                                            player3 = null;
                                                        } else {
                                                            player3 = (JhPhzPlayer) table1.getSeatMap().get(seat);
                                                        }

                                                        player1.reBuildHandCards();
                                                        player2.reBuildHandCards();
                                                        if (!is2)
                                                            player3.reBuildHandCards();
                                                        player1.dealHandPais(map.get(user1));
                                                        player2.dealHandPais(list2);
                                                        if (!is2)
                                                            player3.dealHandPais(list3);
                                                        table1.setLeftCards(list4);
                                                        table1.buildCreateTableRes(player1.getUserId(), false, false);
                                                        table1.buildCreateTableRes(player2.getUserId(), false, false);
                                                        if (!is2)
                                                            table1.buildCreateTableRes(player3.getUserId(), false, true);
                                                        table1.reSend();

                                                        table1.checkAction();

                                                        writeMsg(response, "ok");
                                                        return;

                                                    }
                                                }
                                            }
                                        } else if (list.size() == 2) {
                                            int user2 = list.get(1);
                                            JhPhzPlayer player1 = table1.getPlayer(user1, JhPhzPlayer.class);
                                            JhPhzPlayer player2 = table1.getPlayer(user2, JhPhzPlayer.class);
                                            if (player1 != null && (player1.getHandCards().SRC.size() == map.get(user1).size())
                                                    && player2 != null && (player2.getHandCards().SRC.size() == map.get(user2).size())
                                                    && player1.getSeat() != table1.getShuXingSeat()
                                                    && player2.getSeat() != table1.getShuXingSeat()) {

                                                Set<Integer> card1 = new HashSet<>(map.get(user1));
                                                Set<Integer> card2 = new HashSet<>(map.get(user2));
                                                if (card1.size() == map.get(user1).size() && card2.size() == map.get(user2).size()) {
                                                    List<Integer> allList = JhPhzCardUtils.loadCards(table1.getWangbaCount() > 0 ? table1.getWangbaCount() : 0);
                                                    if (allList.containsAll(map.get(user1))) {
                                                        allList.removeAll(map.get(user1));

                                                        if (allList.containsAll(map.get(user2))) {
                                                            allList.removeAll(map.get(user2));
                                                            List<Integer> list3;
                                                            if (is2) {
                                                                list3 = null;
                                                            } else {
                                                                list3 = new ArrayList<>();
                                                            }

                                                            List<Integer> list4 = new ArrayList<>();
                                                            int len = card1.size() == 21 || card2.size() == 21 ? 20 : 21;
                                                            if (!is2)
                                                                for (int i = 0; i < len; i++) {
                                                                    list3.add(allList.remove(0));
                                                                }
                                                            list4.addAll(allList);

                                                            int seat = table1.calcNextSeat(player1.getSeat());
                                                            if (seat == player2.getSeat()) {
                                                                seat = table1.calcNextSeat(seat);
                                                            }
                                                            JhPhzPlayer player3;
                                                            if (is2) {
                                                                player3 = null;
                                                            } else {
                                                                player3 = (JhPhzPlayer) table1.getSeatMap().get(seat);
                                                            }

                                                            player1.reBuildHandCards();
                                                            player2.reBuildHandCards();
                                                            if (!is2)
                                                                player3.reBuildHandCards();
                                                            player1.dealHandPais(map.get(user1));
                                                            player2.dealHandPais(map.get(user2));
                                                            if (!is2)
                                                                player3.dealHandPais(list3);
                                                            table1.setLeftCards(list4);
                                                            table1.buildCreateTableRes(player1.getUserId(), false, false);
                                                            table1.buildCreateTableRes(player2.getUserId(), false, false);
                                                            if (!is2)
                                                                table1.buildCreateTableRes(player3.getUserId(), false, true);
                                                            table1.reSend();

                                                            table1.checkAction();

                                                            writeMsg(response, "ok");
                                                            return;
                                                        }

                                                    }
                                                }
                                            }
                                        } else if (list.size() == 3 && !is2) {
                                            int user2 = list.get(1);
                                            int user3 = list.get(2);
                                            if (user1 < user2 && user2 < user3) {
                                                JhPhzPlayer player1 = table1.getPlayer(user1, JhPhzPlayer.class);
                                                JhPhzPlayer player2 = table1.getPlayer(user2, JhPhzPlayer.class);
                                                JhPhzPlayer player3 = table1.getPlayer(user3, JhPhzPlayer.class);
                                                if (player1 != null && (player1.getHandCards().SRC.size() == map.get(user1).size())
                                                        && player2 != null && (player2.getHandCards().SRC.size() == map.get(user2).size())
                                                        && player3 != null && (player3.getHandCards().SRC.size() == map.get(user3).size())
                                                        && player1.getSeat() != table1.getShuXingSeat() && player2.getSeat() != table1.getShuXingSeat() && player3.getSeat() != table1.getShuXingSeat()) {

                                                    Set<Integer> card1 = new HashSet<>(map.get(user1));
                                                    Set<Integer> card2 = new HashSet<>(map.get(user2));
                                                    Set<Integer> card3 = new HashSet<>(map.get(user3));
                                                    if (card1.size() == map.get(user1).size() && card2.size() == map.get(user2).size() && card3.size() == map.get(user3).size()) {
                                                        List<Integer> allList = JhPhzCardUtils.loadCards(table1.getWangbaCount() > 0 ? table1.getWangbaCount() : 0);
                                                        if (allList.containsAll(map.get(user1))) {
                                                            allList.removeAll(map.get(user1));
                                                            if (allList.containsAll(map.get(user2))) {
                                                                allList.removeAll(map.get(user2));
                                                                if (allList.containsAll(map.get(user3))) {
                                                                    allList.removeAll(map.get(user3));
                                                                    if (ucs.length == 3) {
                                                                        player1.reBuildHandCards();
                                                                        player2.reBuildHandCards();
                                                                        player3.reBuildHandCards();
                                                                        player1.dealHandPais(map.get(user1));
                                                                        player2.dealHandPais(map.get(user2));
                                                                        player3.dealHandPais(map.get(user3));
                                                                        table1.setLeftCards(allList);
                                                                        table1.buildCreateTableRes(user1, false, false);
                                                                        table1.buildCreateTableRes(user2, false, false);
                                                                        table1.buildCreateTableRes(user3, false, true);
                                                                        table1.reSend();

                                                                        table1.checkAction();

                                                                        writeMsg(response, "ok");
                                                                        return;
                                                                    } else {
                                                                        if (allList.containsAll(map.get(0))) {
                                                                            allList.removeAll(map.get(0));
                                                                            if (allList.size() == 0) {
                                                                                player1.reBuildHandCards();
                                                                                player2.reBuildHandCards();
                                                                                player3.reBuildHandCards();
                                                                                player1.dealHandPais(map.get(user1));
                                                                                player2.dealHandPais(map.get(user2));
                                                                                player3.dealHandPais(map.get(user3));
                                                                                table1.setLeftCards(map.get(0));
                                                                                table1.buildCreateTableRes(user1, false, false);
                                                                                table1.buildCreateTableRes(user2, false, false);
                                                                                table1.buildCreateTableRes(user3, false, true);
                                                                                table1.reSend();

                                                                                table1.checkAction();

                                                                                writeMsg(response, "ok");
                                                                                return;
                                                                            }
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    writeMsg(response, "userCards error");
                                } else {
                                    writeMsg(response, "牌局已开始");
                                }
                            }
                        }
                        return;
                    }


                    writeMsg(response, "Not support");
                }
            } else {
                writeMsg(response, "tableIdStr is not number");
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    private static void writeMsg(HttpServletResponse response, String msg) throws IOException {
        Writer writer = response.getWriter();
        writer.write(msg);
        writer.flush();
    }

    /**
     * 判断是否是数字（正整数、0、负整数）
     *
     * @param str
     * @return
     */
    private static boolean isDigits(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        } else {
            if (str.charAt(0) == '-') {
                return NumberUtils.isDigits(str.substring(1));
            } else if (str.charAt(0) == '+') {
                return NumberUtils.isDigits(str.substring(1));
            } else {
                return NumberUtils.isDigits(str);
            }
        }
    }

}
