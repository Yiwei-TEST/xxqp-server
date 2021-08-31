import com.sy599.game.db.bean.DataStatistics;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by pc on 2017/4/10.
 */
public class Test1 {
    public static void main(String[] args) {
       //
        try {

//            String url="http://pay.csl2016.cn:8000/createOrder.e";
//
//            Map<String,String> map=new TreeMap<>();
//            map.put("partner_id","1000100020001505");
//            map.put("app_id","3722");
//            map.put("wap_type","1");
//            map.put("money","100");
//            map.put("subject", "游戏钻石*100");
//            map.put("qn","webzyfdsx");
//            map.put("out_trade_no","testsy1231"+System.currentTimeMillis());
//
//            String key="D4D168332FE6431193008FF833CE0044";
//
//            StringBuilder stringBuilder=new StringBuilder();
//            for (Map.Entry<String,String> kv:map.entrySet()){
//                if (StringUtils.isNotBlank(kv.getValue())){
//                    stringBuilder.append(kv.getKey()).append("=").append(URLEncoder.encode(kv.getValue(),"UTF-8")).append("&");
//                }
//            }
//            stringBuilder.append("key=").append(key);
//
//            map.put("sign", com.sy.sanguo.common.util.request.MD5Util.getMD5String(stringBuilder));
//
//
////            String ret=HttpUtil.getUrlReturnValue(url,"UTF-8","GET",map);
//            System.out.println(stringBuilder.append("&sign="+map.get("sign")));
//            System.out.println(map);
//            System.out.println(ret);

//            System.out.println("dfghfdh46dfh.36ndf0d".replaceAll("\\D",""));
//            ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
//            String[] strs=new String[]{"1","2","3"};
//
//            for (final String str:strs){
//                EXECUTOR_SERVICE.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        System.out.println(str);
//                    }
//                });
//            }
//            EXECUTOR_SERVICE.shutdown();
//            String newPassword = new SecuritConstantImpl().decrypt("4f8751f5202be94d20c3ef06d1513b8c");
//            System.out.println(newPassword);
//            System.out.println(MD5Util.getStringMD5(UUID.randomUUID().toString()));
            System.out.println(loadInsertSql("t_data_statistics", DataStatistics.class,"keyId"));
//            long time1=System.currentTimeMillis();
//            ExecutorService executorService = Executors.newCachedThreadPool();
//            Future<String> mFuture = executorService.submit(new Callable<String>() {
//                @Override
//                public String call() throws Exception {
//                    Thread.sleep(10000);
//                    return HttpUtil.getUrlReturnValue("http://www.baidu.com",HttpUtil.DEFAULT_CHARSET,HttpUtil.POST,2);
//                }
//            });
//            String asynRet=null;
//            try {
//                asynRet = mFuture.get(2, TimeUnit.SECONDS);
//            }catch (Exception e){
//                System.out.println("times(ms):" + (System.currentTimeMillis() - time1) + " " + asynRet);
//            }

//            System.out.println(checkVersion("v1.1.0",1,"v1.1.0",1));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean checkVersion(String serverVersion,int serverIdx,String clientVersion,int clientIdx){
        boolean result = false;
        int idxS1=serverVersion.indexOf(".",serverIdx);
        int idxC1=clientVersion.indexOf(".",clientIdx);
        if (idxS1>0&&idxC1>0){
            int valS1 = NumberUtils.toInt(serverVersion.substring(serverIdx,idxS1),-1);
            int valC1 = NumberUtils.toInt(clientVersion.substring(clientIdx,idxC1),-1);
            if (valS1>=0&&valC1>=0){
                if (valS1>valC1){
                    result=true;
                }else if (valS1<valC1){
                    result=false;
                }else{
                    return checkVersion(serverVersion,idxS1+1,clientVersion,idxC1+1);
                }
            }
        }else if(idxS1>0){
            result=true;
        }else if(idxC1>0){
            result=false;
        }else if(idxS1==-1&&idxC1==-1){
            idxS1=serverVersion.lastIndexOf(".");
            idxC1=clientVersion.lastIndexOf(".");
            if (idxS1>0&&idxC1>0){
                result= NumberUtils.toInt(serverVersion.substring(idxS1+1),-1)> NumberUtils.toInt(clientVersion.substring(idxC1+1),-1);
            }else if (NumberUtils.isDigits(serverVersion)&& NumberUtils.isDigits(clientVersion)){
                result= NumberUtils.toInt(serverVersion,-1)> NumberUtils.toInt(clientVersion,-1);
            }
        }
        return result;
    }

    public static String loadInsertSql(String table,Class cls,String...exclusive){
        Method[] methods = cls.getMethods();
        Method[] var4 = methods;
        int var5 = methods.length;
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("insert into ").append(table).append(" (");

        for(int var6 = 0; var6 < var5; ++var6) {
            Method method = var4[var6];
            String fieldName = method.getName();
            if (!fieldName.equals("getClass") && fieldName.startsWith("get")) {
                fieldName = fieldName.substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                if (!contain(fieldName,exclusive))
                stringBuilder.append(fieldName).append(",");
            }
        }
        stringBuilder.replace(stringBuilder.length()-1,stringBuilder.length(),")");
        stringBuilder.append(" VALUES (");

        for(int var6 = 0; var6 < var5; ++var6) {
            Method method = var4[var6];
            String fieldName = method.getName();
            if (!fieldName.equals("getClass") && fieldName.startsWith("get")) {
                fieldName = fieldName.substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                if (!contain(fieldName,exclusive))
                stringBuilder.append("#").append(fieldName).append("#,");
            }
        }
        stringBuilder.replace(stringBuilder.length()-1,stringBuilder.length(),")");

        return  stringBuilder.toString();
    }

    public static boolean contain(String search,String...src){
        if (src==null||src.length==0){
            return false;
        }else{
            return Arrays.binarySearch(src,search)>=0;
        }
    }
}
