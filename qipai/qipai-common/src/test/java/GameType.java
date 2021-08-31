import com.sy.general.BeanHelper;
import com.sy.general.KV;
import com.sy599.game.util.GameUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GameType {
    public static void main(String[] args) {
        Map<KV<String, Integer>,String> kvs = new TreeMap<>(new Comparator<KV<String, Integer>>() {
            @Override
            public int compare(KV<String, Integer> o1, KV<String, Integer> o2) {
                return o1.getValue()-o2.getValue();
            }
        });
        Set<Field> set = BeanHelper.getAllFields(GameUtil.class);
        for (Field field : set) {
            String name = field.getName();
            String value;
            try {
                if (Modifier.isPublic(field.getModifiers())&&Modifier.isStatic(field.getModifiers())){
                    value = String.valueOf(field.get(name));
                }else{
                    value = null;
                }
            } catch (Exception e) {
                value = null;
            }

            if(NumberUtils.isDigits(value)){
                kvs.put(new KV<>(name,Integer.valueOf(String.valueOf(value))),"1");
            }
        }

        for (Map.Entry<KV<String, Integer>,String> kv:kvs.entrySet()){
            System.out.println(kv.getKey());
        }

    }
}
