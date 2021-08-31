//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sy599.game.util;

import com.sy599.game.staticdata.KeyValuePair;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

public final class MathUtil {
    public MathUtil() {
    }

    public static double random(double low, double high) {
        return low + (high - low) * Math.random();
    }

    public static boolean shake(double rate) {
        return Math.random() < rate;
    }

    public static int mt_rand(int min, int max) {
        Random r = new SecureRandom();
        return min + r.nextInt(max - min + 1);
    }

    public static int randomRegion(List<Integer> region, List<Integer> values) {
        int total = 0;

        int sum;
        for(Iterator var4 = region.iterator(); var4.hasNext(); total += sum) {
            sum = (Integer)var4.next();
        }

        sum = 0;
        int random = mt_rand(1, total);
        int index = 0;
        int i = 0;

        for(Iterator var8 = region.iterator(); var8.hasNext(); ++i) {
            int data = (Integer)var8.next();
            sum += data;
            if (random <= sum) {
                index = i;
                break;
            }
        }

        return (Integer)values.get(index);
    }

    public static int randomRegion(List<Integer> region, int[] values) {
        int total = 0;

        int sum;
        for(Iterator var4 = region.iterator(); var4.hasNext(); total += sum) {
            sum = (Integer)var4.next();
        }

        sum = 0;
        int random = mt_rand(1, total);
        int index = 0;
        int i = 0;

        for(Iterator var8 = region.iterator(); var8.hasNext(); ++i) {
            int data = (Integer)var8.next();
            sum += data;
            if (random <= sum) {
                index = i;
                break;
            }
        }

        return values[index];
    }

    public static int random(List<KeyValuePair<Integer, Integer>> list) {
        int total = 0;

        KeyValuePair pair;
        for(Iterator var3 = list.iterator(); var3.hasNext(); total += (Integer)pair.getValue()) {
            pair = (KeyValuePair)var3.next();
        }

        int sum = 0;
        int random = mt_rand(1, total);
        int index = 0;
        int i = 0;

        for(Iterator var7 = list.iterator(); var7.hasNext(); ++i) {
            KeyValuePair<Integer, Integer> pair0 = (KeyValuePair)var7.next();
            sum += (Integer)pair0.getValue();
            if (random <= sum) {
                index = i;
                break;
            }
        }

        return (Integer)((KeyValuePair)list.get(index)).getId();
    }

    public static int randomRegion(int[] region, int[] values) {
        int total = 0;
        int[] var6 = region;
        int index = region.length;

        int sum;
        int random;
        for(random = 0; random < index; ++random) {
            sum = var6[random];
            total += sum;
        }

        sum = 0;
        random = mt_rand(1, total);
        index = 0;
        int i = 0;
        int[] var10 = region;
        int var9 = region.length;

        for(int var8 = 0; var8 < var9; ++var8) {
            int data = var10[var8];
            sum += data;
            if (random <= sum) {
                index = i;
                break;
            }

            ++i;
        }

        return values[index];
    }

    public static Integer draw(List<Integer> list) {
        int index = mt_rand(0, list.size() - 1);
        return (Integer)list.get(index);
    }

    public static List<Integer> draw(int n, int m) {
        if (n < m) {
            return Collections.emptyList();
        } else {
            List<Integer> tmp = new ArrayList();

            for(int i = 1; i <= n; ++i) {
                tmp.add(i);
            }

            Collections.shuffle(tmp);
            return tmp.subList(0, m);
        }
    }

    public static int draw(Map<Integer, Long> map) {
        if (map != null && !map.isEmpty()) {
            double total_rate = 0.0D;

            double rate;
            for(Iterator var5 = map.values().iterator(); var5.hasNext(); total_rate += (double)rate) {
                rate = (Long)var5.next();
            }

            double random = Math.random();
            int i = 0;
            for(Iterator var7 = map.keySet().iterator(); var7.hasNext(); random -= rate) {
                int val = (Integer)var7.next();
                ++i;
                rate = (double)(Long)map.get(val) / total_rate;
                if (random < rate && rate != 0.0D) {
                    return val;
                }
            }

            String val = map.keySet().toArray()[0].toString();
            return Integer.parseInt(val);
        } else {
            return 0;
        }
    }

    public static List<Integer> draw(Map<Integer, Long> map, int n) {
        if (map != null && !map.isEmpty()) {
            if (map.size() <= n) {
                return new ArrayList(map.keySet());
            } else {
                List<Integer> list = new ArrayList();

                for(int i = 0; i < n; ++i) {
                    int draw = draw(map);
                    list.add(draw);
                    map.remove(draw);
                }

                return list;
            }
        } else {
            return new ArrayList();
        }
    }

    /**
     * 经纬度均用弧度表示，计算两经纬度点的距离
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @return
     */
    public static double distance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double Lat1 = rad(latitude1); // 纬度
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;//两点纬度之差
        double b = rad(longitude1) - rad(longitude2); //经度之差
        double s = 2 * Math.asin(Math
                .sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));//计算两点距离的公式
        s = s * 6378137.0;//弧长乘地球半径（半径为米）
        s = Math.round(s * 10000d) / 10000d;//精确距离的数值
        return s;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.00; //角度转换成弧度
    }

    /**
     * 格式化信用分
     * @param credit
     * @return
     */
    public static double formatCredit(long credit) {
        return new BigDecimal((credit / 100d)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 取大数
     * @param value1
     * @param value2
     * @return
     */
    public static long bigOne(long value1, long value2) {
        return value1 > value2 ? value1 : value2;
    }

    /**
     * 取小数
     * @param value1
     * @param value2
     * @return
     */
    public static long smallOne(long value1, long value2) {
        return value1 < value2 ? value1 : value2;
    }

    public static void main(String[] args) {
        String latitudes1 = "28.201118,112.913795";
        String[] arr1 = latitudes1.split(",");
        double longitude1 = Double.parseDouble(arr1[0]);
        double latitude1 = Double.parseDouble(arr1[1]);

        String latitudes2 = "28.201575,112.914392";
        String[] arr2 = latitudes2.split(",");
        double longitude2 = Double.parseDouble(arr2[0]);
        double latitude2 = Double.parseDouble(arr2[1]);

        System.out.println("distance:" + distance(longitude1, latitude1, longitude2, latitude2));
        System.out.println(draw((List)(new ArrayList(Arrays.asList(1, 2, 3, 4)))));

    }
}
