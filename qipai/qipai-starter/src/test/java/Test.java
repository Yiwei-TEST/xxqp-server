public class Test {
    public static void main(String[] args) {
        //斗牛
        loadKey("斗牛","20,21,22,23,24,25","10","6",10,40);
        loadKey("斗牛","20,21,22,23,24,25","10","10",10,80);
        loadKey("斗牛","20,21,22,23,24,25","20","6",20,80);
        loadKey("斗牛","20,21,22,23,24,25","20","10",20,160);
////        //十点半
        loadKey("十点半","61,62,63,64","8","4",10,38);
        loadKey("十点半","61,62,63,64","16","4",20,76);
////        //斗地主
//        loadKey("斗地主","91,92,93","6","2",20,60);
//        loadKey("斗地主","91,92,93","6","3",20,60);
//        loadKey("斗地主","91,92,93","12","2",38,108);
//        loadKey("斗地主","91,92,93","12","3",38,108);
//打筒子
        loadKey("永州跑胡子","37,38,39","5","3",8,20);
        loadKey("永州跑胡子","37,38,39","10","3",12,30);
        loadKey("永州跑胡子","41,42,43","5","2",8,20);
        loadKey("永州跑胡子","41,42,43","10","2",12,30);
        loadKey("永州跑胡子","35,36,40","5","4",8,20);
        loadKey("永州跑胡子","35,36,40","10","4",12,30);
//        loadKey("永州跑胡子","35,36","5,10","4",6,20);

        loadKey("祁阳六胡抢","44,45,46","5","2",8,20);
        loadKey("祁阳六胡抢","44,45,46","10","2",12,30);
        loadKey("祁阳六胡抢","47,48,49","5","3",8,20);
        loadKey("祁阳六胡抢","47,48,49","10","3",12,30);
        loadKey("祁阳六胡抢","50,51,52","5","4",8,20);
        loadKey("祁阳六胡抢","50,51,52","10","4",12,30);
//
//        System.out.println(",,,".split(",").length);

//        HashMap
//        System.out.println(GeneralUtil.loadCurrentServerMsg());

//        System.out.println(HttpUtil.getUrlReturnValue("http://localhost:8109/card/change.do?tableId=249273&userCards=1010750,55,65,5,66,76,56,6,16,201,202,50,60,70,80,49,59,69,1,2,3"));
    }

    private static void loadKey(String gameDes,String typesStr,String countsStr,String playersStr,int aaPay,int masterPay){
        String[] types=typesStr.split(",");
        String[] counts=countsStr.split(",");
        String[] players=playersStr.split(",");

        for (String type:types){
            for (String count:counts){
                for (String player:players){
                    System.out.println("#"+gameDes+" 付费配置:玩法"+type+",局数"+count+"，最大人数"+player+",AA支付(0)=耗钻数量");
                    System.out.println("pay_type"+type+"_count"+count+"_player"+player+"_pay0="+aaPay);

                    System.out.println("#"+gameDes+" 付费配置:玩法"+type+",局数"+count+"，最大人数"+player+",房主支付(1)=耗钻数量");
                    System.out.println("pay_type"+type+"_count"+count+"_player"+player+"_pay1="+masterPay);
                }
            }
        }
        System.out.println("");
    }
}
