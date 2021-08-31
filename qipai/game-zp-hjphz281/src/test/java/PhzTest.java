import java.util.Arrays;

import com.sy599.game.qipai.jhphz281.bean.JhPhzBase;
import com.sy599.game.qipai.jhphz281.util.JhPhzCardResult;
import com.sy599.game.qipai.jhphz281.util.JhPhzHandCards;
import com.sy599.game.qipai.jhphz281.util.JhPhzHuPaiUtils;

public class PhzTest {
    public static void main(String[] args) {
        JhPhzHandCards handCards = new JhPhzHandCards(Arrays.asList(1,11,51,61,63,2,3,4,14,54,64,55,65,75,7,57,67,60,59,58));
//        handCards.PAO.put(10,Arrays.asList(10,20,30,40));
//        handCards.PENG.put(105,Arrays.asList(55,65,75));
//        handCards.CHI_COMMON.add(Arrays.asList(6,7,8));
//        handCards.CHI_COMMON.add(Arrays.asList(18,19,20));

        JhPhzBase phzBase = new JhPhzBase() {
            @Override
            public int loadQihuxi() {
                return 10;
            }

            @Override
            public int loadRedCount() {
                return 10;
            }

            @Override
            public boolean checkRed2Black() {
                return true;
            }

            @Override
            public boolean checkRed2Dian() {
                return true;
            }

            @Override
            public int loadRed2BlackCount() {
                return 16;
            }

            @Override
            public int loadRed2DianCount() {
                return 13;
            }

            @Override
            public int loadBaseTun() {
                return (loadQihuxi()/3)-3;
            }

            @Override
            public int loadCommonTun(int totalHuxi) {
                return (totalHuxi - loadQihuxi())/3;
            }

            @Override
            public int loadScoreTun(int totalHuxi) {
                return loadBaseTun()+loadCommonTun(totalHuxi);
            }

            @Override
            public int loadXingMode() {
                return 0;
            }

            @Override
            public int loadXingTun(int xingCount) {
                return xingCount;
            }

            @Override
            public int loadXingCard(boolean tiqian) {
                return 0;
            }

            @Override
            public int loadHuMode() {
                return 0;
            }

            @Override
            public boolean isTianHu() {
                return false;
            }
        };

        JhPhzCardResult cardResult = JhPhzHuPaiUtils.huPai(handCards,52,phzBase,true,"test",52);
        System.out.println(cardResult.isCanHu());
        System.out.println(cardResult.getTotalHuxi());
        System.out.println(cardResult.getTotalFan());
        System.out.println(cardResult.getFans());
        System.out.println(cardResult.getCardMessageList().size()+":"+cardResult.getCardMessageList());
    }
}
