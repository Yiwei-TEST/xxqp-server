package com.sy599.game.qipai.cqxzmj.bean;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.CqxzMjScoreboardRes;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CqxzMjBoard {
    private int observer;   //查看人
    List<ActOperator> aorList=new ArrayList<>(); //每一条积分明细
    private int countPoint=0; //累计实时输赢

    private long lastCheckTime=0;
    public static final int BaGang=1;
    public static final int XiaYu=2;
    public static final int ZiMo=3;
    public static final int DianPao=4;
    public static final int JiePao=5;
    public static final int HuJiaoZhuanYi=6;
    public static final int ChaJiao=7;
    public static final int ZhiGang=8;
    public static final int TuiShui=9;

    public CqxzMjBoard(int observer) {
        this.observer = observer;
    }

    public int getCountPoint() {
        return countPoint;
    }

    public void addActOperator(int actId, int act, long operatorId, int operatorSeat, int point){
        ActOperator aot=new ActOperator(actId,act,operatorId,operatorSeat,point);
        aorList.add(aot);
        countPoint+=point;
    }

    public int getMingGangNum(){
        int num=0;
        for (ActOperator aot:aorList) {
            if((aot.act==BaGang||aot.act==ZhiGang)&&aot.point>0)
                num++;
        }
        return num;
    }

    public boolean haveChaJiao(){
        for (ActOperator aot:aorList) {
            if(aot.act==ChaJiao)
                return true;
        }
        return false;
    }

    public int [] getOverHuMsg(CqxzMjTable table){
        //0 自摸，1，接炮，2点1胡，3，点2胡，4，点3胡 5,查叫，6被查叫
        int [] arr=new int[7];
        for (ActOperator aot:aorList) {
            switch (aot.act){
                case ZiMo:
                    if(aot.point>0)
                        arr[0]=1;
                    break;
                case JiePao:
                    arr[1]=1;
                    break;
                case DianPao:
                    CqxzMjPlayer player = (CqxzMjPlayer) table.getSeatMap().get(aot.operatorSeat);
                    arr[player.getHuIndex()+1]=1;
                    break;
                case ChaJiao:
                    if(aot.point>0)
                        arr[5]=1;
                    else
                        arr[6]=1;
                    break;
            }
        }
        return arr;
    }

    public int [] getOverGangMsg(){
        //0暗杠，1巴杠，2直杠，3点杠
        int [] arr=new int[4];
        for (ActOperator aot:aorList) {
            switch (aot.act){
                case XiaYu:
                    if(aot.point>0)
                        arr[0]++;
                    break;
                case BaGang:
                    if(aot.point>0)
                        arr[1]++;
                    break;
                case ZhiGang:
                    if(aot.point>0)
                        arr[2]++;
                    else
                        arr[3]++;
                    break;
            }
        }
        return arr;
    }

    /**
     * 转移退税
     * @return
     */
    public int [] getOverZYTS(){
        //0转移分，2退税分
        int [] arr=new int[2];
        for (ActOperator aot:aorList) {
            switch (aot.act){
                case HuJiaoZhuanYi:
                    arr[0]=aot.point;
                    break;
                case TuiShui:
                    arr[1]=aot.point;
                    break;
            }
        }
        return arr;
    }

    public int getGSHActId(){
        ActOperator aot = aorList.get(aorList.size() - 1);
        if(aot.act!=ZhiGang)
            return 0;
        return aot.actId;
    }

    public CqxzMjScoreboardRes getBuild(){
        if(lastCheckTime==0||System.currentTimeMillis()-lastCheckTime>1000){
            return buildPbRes().build();
        }
        return null;
    }

    public CqxzMjScoreboardRes.Builder buildPbRes(){
        CqxzMjScoreboardRes.Builder builder=CqxzMjScoreboardRes.newBuilder();
        builder.setObserverSeat(observer);
        builder.setCountPoint(countPoint);
        for (ActOperator aor:aorList) {
            builder.addAor(aor.buildPbRes());
        }
        return builder;
    }

    public List<Integer> noTingTuiShui(){
        List<Integer> tuiShuiId=new ArrayList<>();
        Iterator<ActOperator> it = aorList.iterator();
        while (it.hasNext()){
            ActOperator next = it.next();
            if((next.act==XiaYu||next.act==BaGang||next.act==ZhiGang)&&next.point>0){
                tuiShuiId.add(next.actId);
            }
        }
        return tuiShuiId;
    }

    public boolean tuiShuiById(List<Integer> tuiIds){
        List<ActOperator> tui=new ArrayList<>();
        int tuiPoint=0;
        for (ActOperator aot:aorList) {
            if(tuiIds.contains(aot.actId)){
                tui.add(aot);
                tuiPoint+=aot.point;
            }

        }
        if(tuiPoint!=0){
            addActOperator(0,TuiShui,0,0,-tuiPoint);
            countPoint();
            return true;
        }
        return false;
    }

    public boolean findLoseSeatById(int actId){
        for (ActOperator aot:aorList) {
            if(aot.actId==actId&&aot.point<0)
                return true;
        }
        return false;
    }

    public int getLastGangPoint(){
        if(aorList.size()>0)
            return aorList.get(aorList.size()-1).point;
        return 0;
    }

    private void countPoint(){
        int point=0;
        for (ActOperator aot:aorList) {
            point+=aot.point;
        }
        this.countPoint=point;
    }

    public void clear(){
        aorList.clear();
        countPoint=0;
        lastCheckTime=0;
    }

    public void init(String data) {
        if (!StringUtils.isBlank(data)) {
            String[] values = data.split("_");
            String aorListStr = StringUtil.getValue(values, 0);
            if (!StringUtils.isBlank(aorListStr)) {
                String[] aots = aorListStr.split("\\|");
                for (int i = 0; i < aots.length; i++) {
                    ActOperator aot=new ActOperator(aots[i]);
                    if(aot!=null){
                        aorList.add(aot);
                    }
                }
            }
            countPoint = StringUtil.getIntValue(values, 1);
        }
    }

    public String toStr() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < aorList.size(); i++) {
            sb.append(aorList.get(i).toStr());
            if(i<aorList.size()-1)
                sb.append("|");
        }
        sb.append("_");
        sb.append(countPoint).append("_");
        return sb.toString();
    }



    public class ActOperator {
        private int actId;        //杠序列号，其他操作暂时用不到，默认为0
        private int act;		  //操作
        private String operatorId ;  //操作人id
        private int operatorSeat; //操作人坐位
        public int point;        //输赢分

        public ActOperator(int actId, int act, long operatorId, int operatorSeat, int point) {
            this.actId = actId;
            this.act = act;
            this.operatorId = operatorId+"";
            this.operatorSeat = operatorSeat;
            this.point = point;
        }

        public ActOperator(String data){
            init(data);
        }

        public PlayCardResMsg.ActOperatorRes.Builder buildPbRes(){
            PlayCardResMsg.ActOperatorRes.Builder builder= PlayCardResMsg.ActOperatorRes.newBuilder();
            builder.setAct(act);
            builder.setOperatorId(operatorId);
            builder.setOperatorSeat(operatorSeat);
            builder.setPoint(point);
            return builder;
        }


        public void init(String data) {
            if (!StringUtils.isBlank(data)) {
                String[] values = data.split("\\^");
                actId = StringUtil.getIntValue(values, 0);
                act = StringUtil.getIntValue(values, 1);
                operatorId = StringUtil.getValue(values, 2);
                operatorSeat = StringUtil.getIntValue(values, 3);
                point = StringUtil.getIntValue(values, 4);
            }
        }

        public String toStr() {
            StringBuffer sb = new StringBuffer();
            sb.append(actId).append("^");
            sb.append(act).append("^");
            sb.append(operatorId).append("^");
            sb.append(operatorSeat).append("^");
            sb.append(point).append("^");
            return sb.toString();
        }

    }
}
