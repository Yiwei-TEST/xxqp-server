package com.sy.sanguo.game.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.sy.sanguo.game.bean.*;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.CsvReader;
import com.sy.sanguo.common.util.LoginCacheContext;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.msg.MonitorMsg;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class SysInfManager {

//    private List<Server> servers = new ArrayList<Server>();
    private static Map<Integer, Server> serverMap = new ConcurrentHashMap<>();
    private List<Notice> notices = new ArrayList<Notice>();
    private List<Roster> whiteList = new ArrayList<Roster>();
    private List<String> ipBlackList = new ArrayList<String>();
    private List<String> macBlackList = new ArrayList<String>();
    private List<String> dcBlackList = new ArrayList<String>();
    private List<String> flatIdBlackList = new ArrayList<>();
    private List<String> maskWordList = new ArrayList<String>();
    private Map<Integer, MonitorMsg> monitorMap = new HashMap<Integer, MonitorMsg>();
    private List<ServerFilter> serverFilter = new ArrayList<ServerFilter>();
    private List<SyvcFilter> syvcFilter = new ArrayList<SyvcFilter>();
    private Map<String, Version> versionMap = new HashMap<String, Version>();
    private Map<String, SdkUrlConfig> sdkConfigMap = new LinkedHashMap<String, SdkUrlConfig>();
    private Map<String, CdkAward> CdkAwards = new HashMap<String, CdkAward>();
    private Map<Integer, List<String>> ipConfigList = new HashMap<>();
    private Map<Integer, String> spareIpMap = new HashMap<>();
    public static String baseDir = "";
    public static String baseUrl = "";
    public static Map<String,VersionCheck> versionCheckMap = new HashMap<>();
    private List<Lottery> lotteries = new ArrayList<>();

    private static SysInfManager _inst = new SysInfManager();

    private SysInfManager() {
        GameBackLogger.SYS_LOG.info("-------SysInfManager init start----------");
        baseDir = this.getClass().getClassLoader().getResource("").getPath().toString();
        // baseDir=baseDir.replace("/WEB-INF/classes/ ", "");
        // baseDir = baseDir.replace("/WEB-INF/classes/ ", "");
        baseDir = baseDir.substring(0, baseDir.length() - ("/WEB-INF/classes/".length()));

        GameBackLogger.SYS_LOG.info("pf config count="+PfCommonStaticData.countPfConfig());

        loadIpConfig();
        initNotice();
        initVersion();
        initWhiteList();
        // initBlackList();
        initServerFilter();
        loadSdkConfig();
        initSyvcFilter();
        initCdkAward();
        initVersionCheck();
        GameBackLogger.SYS_LOG.info("--------SysInfManager init end----------- " + LoginCacheContext.gameName);
        GameBackLogger.SYS_LOG.info("--------LoginCacheContext.userMaxCount-----------" + LoginCacheContext.userMaxCount);
        GameBackLogger.SYS_LOG.info("--------baseDir-----------" + baseDir);
    }

    private void initVersionCheck() {
        List<String[]> list = readCSV(getPath("versionCheck.csv"), false);

        for (String[] values : list) {
            if (NumberUtils.toInt(values[0],0)>0) {
                VersionCheck versionCheck = new VersionCheck();
                versionCheck.setId(Integer.parseInt(values[0]));
                versionCheck.setVersion(Integer.parseInt(values[1]));
                versionCheck.setVersionStr(values[2]);
                if (values.length>=4){
                    versionCheck.setPf(values[3]);
                }else{
                    versionCheck.setPf("all");
                }
                versionCheckMap.put(versionCheck.getPf(),versionCheck);
            }
        }
    }

    private void initCdkAward() {
        List<String[]> list = readCSV(getPath("cdkaward_new.csv"), false);
        CdkAwards.clear();
        for (String[] values : list) {
            CdkAward cdk = new CdkAward();

            cdk.setPlatform(values[0]);
            cdk.setName(values[1]);
            cdk.setId(Integer.parseInt(values[2]));
            cdk.setCdkcount(Integer.parseInt(values[3]));
            cdk.setAwardId(Integer.parseInt(values[4]));
            cdk.setType(Integer.parseInt(values[5]));
            cdk.setCdkType(Integer.parseInt(values[6]));
            cdk.setCdk(values[7]);
            cdk.setCdkKey(values[8]);
            String regTime = StringUtil.getValue(values, 9);
            if (!StringUtils.isBlank(regTime)) {
                String date = regTime.replace("/", "-");
                date += " 00:00:00";
                cdk.setRegTime(date);
            }
            String endTime = StringUtil.getValue(values, 10);
            if (!StringUtils.isBlank(endTime)) {
                String date = endTime.replace("/", "-");
                date += " 23:59:59";
                cdk.setEndTime(date);
            }
            CdkAwards.put(cdk.getCdkKey(), cdk);
        }
    }

    public void test() {
        System.out.println("11111111111");
    }

    /**
     * sdk配置
     */
    private void loadSdkConfig() {
        String path = getPath("sdkconfig.csv");
        File file = new File(path);
        if (!file.exists()) {
            GameBackLogger.SYS_LOG.info("--------sdkconfig.csv is not exists-----------");
            return;
        }

        List<String[]> list = readCSV(path, true);
        int j = 0;
        int length = 0;
        for (String[] values : list) {
            j++;
            if (j == 1) {
                // 第一行
                length = values.length;
                GameBackLogger.SYS_LOG.info("sdkconfig.csv values length-->" + j + ":" + length + "");
                continue;
            }
            if (values.length != length) {
                GameBackLogger.SYS_LOG.info("sdkconfig.csv values length-->" + j + ":" + length + "");
            }
            SdkUrlConfig bean = new SdkUrlConfig();
            bean.setPf(values[0]);
            bean.setUrl(values[1]);
            sdkConfigMap.put(bean.getPf(), bean);
        }

    }

    private void initServerFilter() {
        List<String[]> list = readCSV(getPath("serverFilter.csv"), false);
        serverFilter.clear();
        for (String[] values : list) {

            ServerFilter serverFilter = new ServerFilter();
            if (!StringUtils.isBlank(values[0])) {
                serverFilter.setPfCid(values[0]);
            }
            if (!StringUtils.isBlank(values[1])) {
                String[] blcaks = values[1].split(",");
                int[] blcakList = new int[]{Integer.parseInt(blcaks[0]), Integer.parseInt(blcaks[1])};
                serverFilter.setBlackList(blcakList);
            }
            if (!StringUtils.isBlank(values[2])) {
                String[] white = values[2].split(",");
                int[] whiteList = new int[]{Integer.parseInt(white[0]), Integer.parseInt(white[1])};
                serverFilter.setWhiteList(whiteList);
            }
            if (!StringUtils.isBlank(values[3])) {
                serverFilter.setEquals(Integer.parseInt(values[3]));
            }
            if (!StringUtils.isBlank(values[4])) {
                serverFilter.setUseNotice(Integer.parseInt(values[4]));
            }
            serverFilter.setNoticeId(StringUtil.getIntValue(values, 5));
            this.serverFilter.add(serverFilter);
        }
    }

    private void initSyvcFilter() {
        String path = getPath("syvcFilter.csv");
        File file = new File(path);
        if (!file.exists()) {
            GameBackLogger.SYS_LOG.info("--------syvcFilter.csv is not exists-----------");
            return;
        }
        List<String[]> list = readCSV(path, false);
        syvcFilter.clear();
        for (String[] values : list) {
            int i = 0;
            SyvcFilter bean = new SyvcFilter();
            bean.setSyvc(StringUtil.getValue(values, i++));
            bean.setIsIosAudit(StringUtil.getIntValue(values, i++));
            bean.setAuditServer(StringUtil.getIntValue(values, i++));
            GameBackLogger.SYS_LOG.info("syvc-->" + bean.getSyvc() + " iosaudit:" + bean.getIsIosAudit() + " server:" + bean.getAuditServer());
            this.syvcFilter.add(bean);
        }
    }

    // private void initBlackList() {
    // List<String[]> list = readCSV(getPath("blackList.csv"), false);
    // ipBlackList.clear();
    // macBlackList.clear();
    // dcBlackList.clear();
    // for (String[] values : list) {
    // if (!StringUtils.isBlank(values[0])) {
    // ipBlackList.add(values[0]);
    // }
    // if (!StringUtils.isBlank(values[1])) {
    // macBlackList.add(values[1]);
    // }
    // if (!StringUtils.isBlank(values[2])) {
    // dcBlackList.add(values[2]);
    // }
    // }
    // }

    private void initWhiteList() {
        List<String[]> list = readCSV(getPath("whiteList.csv"), false);
        whiteList.clear();
        for (String[] values : list) {
            Roster roster = new Roster();
            roster.setPf(values[0]);
            roster.setFlatId(values[1]);
            whiteList.add(roster);
        }
    }

    private void initNotice() {
        List<String[]> list = readCSV(getPath("notice.csv"), false);
        notices.clear();
        for (String[] values : list) {
            Notice notice = new Notice();
            notice.setId(parseInt(values[0]));
            notice.setTitle(values[1]);
            notice.setContent(values[2]);
            try {
                notice.setViewDate(new SimpleDateFormat("yy-MM-dd HH:mm:ss").parse(values[3]));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String pf = getValue(values, 4);
            if (!StringUtils.isBlank(pf)) {
                notice.setPf(pf);
            }
            notices.add(notice);
            GameBackLogger.SYS_LOG.info(notice);
        }

    }

    private void initVersion() {
        List<String[]> list = readCSV(getPath("version.csv"), false);
        versionMap.clear();
        for (String[] values : list) {
            Version ver = new Version();
            ver.setPf(values[0]);
            ver.setVersionId(parseInt(values[1]));
            ver.setDownloadUrl(values[2]);

            versionMap.put(ver.getPf(), ver);
        }
    }

    private void loadIpConfig() {
        List<String[]> list = readCSV(getPath("ipConfig.csv"), false);
        ipConfigList.clear();
        for (String[] values : list) {
            int i = 0;
            String ip = StringUtil.getValue(values, i++);
            int ipConfing = StringUtil.getIntValue(values, i++);

            if (ipConfigList.containsKey(ipConfing)) {
                ipConfigList.get(ipConfing).add(ip);
            } else {
                List<String> ipList = new ArrayList<>();
                ipList.add(ip);
                ipConfigList.put(ipConfing, ipList);
            }

        }
    }

    public static Collection<Server> loadServers(){
        return serverMap.values();
    }

    public static Server loadServer(int serverId){
        return serverMap.get(serverId);
    }

    public static Server loadServer(int type,int serverType){
        return loadServer(type, serverType,false);
    }

    public static Server loadServer(String pf, int serverType){
        Server tempServer = null;
        int tempOnlineCount=Integer.MAX_VALUE;
        for (Map.Entry<Integer,Server> kv: serverMap.entrySet()){
            if (kv.getValue().getServerType()==serverType && StringUtils.contains(kv.getValue().getExtend(),"|"+pf+"|")){
                if (kv.getValue().getOnlineCount()==0){
                    tempServer=kv.getValue();
                    break;
                }else if (kv.getValue().getOnlineCount()<tempOnlineCount){
                    tempOnlineCount=kv.getValue().getOnlineCount();
                    tempServer=kv.getValue();
                }
            }
        }
        if (tempServer==null){
            for (Map.Entry<Integer,Server> kv: serverMap.entrySet()){
                if (kv.getValue().getServerType()==serverType){
                    if (kv.getValue().getOnlineCount()==0){
                        tempServer=kv.getValue();
                        break;
                    }else if (kv.getValue().getOnlineCount()<tempOnlineCount){
                        tempOnlineCount=kv.getValue().getOnlineCount();
                        tempServer=kv.getValue();
                    }
                }
            }
        }
        if (tempServer!=null){
            synchronized (tempServer){
                tempServer.setOnlineCount(tempServer.getOnlineCount()+1);
            }
        }

        return tempServer;
    }

    public static Server loadServer(int type,int serverType,boolean isMatch){
        Server tempServer=null;
        int tempOnlineCount=Integer.MAX_VALUE;

        if (type<=0){
            for (Map.Entry<Integer,Server> kv: serverMap.entrySet()){
                if (kv.getValue().getServerType()==serverType){
                    if (kv.getValue().getOnlineCount()==0){
                        tempServer=kv.getValue();
                        break;
                    }else if (kv.getValue().getOnlineCount()<tempOnlineCount){
                        tempOnlineCount=kv.getValue().getOnlineCount();
                        tempServer=kv.getValue();
                    }
                }
            }
        }else{
            if (isMatch){
                for (Map.Entry<Integer,Server> kv: serverMap.entrySet()){
                    if (kv.getValue().getServerType()==serverType&&(kv.getValue().getMatchType().contains(type))){
                        if (kv.getValue().getOnlineCount()==0){
                            tempServer=kv.getValue();
                            break;
                        }else if (kv.getValue().getOnlineCount()<tempOnlineCount){
                            tempOnlineCount=kv.getValue().getOnlineCount();
                            tempServer=kv.getValue();
                        }
                    }
                }
            }else{
                for (Map.Entry<Integer,Server> kv: serverMap.entrySet()){
                    if (kv.getValue().getServerType()==serverType&&kv.getValue().getGameType().contains(type)){
                        if (kv.getValue().getOnlineCount()==0){
                            tempServer=kv.getValue();
                            break;
                        }else if (kv.getValue().getOnlineCount()<tempOnlineCount){
                            tempOnlineCount=kv.getValue().getOnlineCount();
                            tempServer=kv.getValue();
                        }
                    }
                }
            }

            if (tempServer==null){
                for (Map.Entry<Integer,Server> kv: serverMap.entrySet()){
                    if (kv.getValue().getServerType()==serverType){
                        if (kv.getValue().getOnlineCount()==0){
                            tempServer=kv.getValue();
                            break;
                        }else if (kv.getValue().getOnlineCount()<tempOnlineCount){
                            tempOnlineCount=kv.getValue().getOnlineCount();
                            tempServer=kv.getValue();
                        }
                    }
                }
            }
        }

        if (tempServer!=null){
            synchronized (tempServer){
                tempServer.setOnlineCount(tempServer.getOnlineCount()+1);
            }
        }

        return tempServer;
    }

    public static void initServer() {
//        List<String[]> list = readCSV(getPath("server.csv"), false);
//        servers.clear();
//        serverMap.clear();
//        for (String[] values : list) {
//            Server ser = new Server();
//            int i = 0;
//            ser.setId(StringUtil.getIntValue(values, i++));
//            ser.setName(getValue(values, i++));
//            ser.setIsOpen(StringUtil.getIntValue(values, i++));
//            ser.setStatus(StringUtil.getValue(values, i++));
//            ser.setHost(StringUtil.getValue(values, i++));
//            ser.setPort(StringUtil.getIntValue(values, i++));
//            ser.setChathost(StringUtil.getValue(values, i++));
//            ser.setIntranet(StringUtil.getValue(values, i++));
//            if (StringUtils.isBlank(ser.getIntranet())) {
//                ser.setIntranet(ser.getHost());
//            }
//            String gameTypeStr = getValue(values, i++);
//            if (!StringUtils.isBlank(gameTypeStr)) {
//                ser.setGameType(StringUtil.explodeToIntList(gameTypeStr));
//
//            } else {
//                ser.setGameType(new ArrayList<Integer>());
//            }
//
//            String matchTypeStr = getValue(values, i++);
//            if (!StringUtils.isBlank(matchTypeStr)) {
//                ser.setMatchType(StringUtil.explodeToIntList(matchTypeStr));
//
//            } else {
//                ser.setMatchType(new ArrayList<Integer>());
//
//            }
//            ser.setIpConifg(StringUtil.getIntValue(values, i++));
//
//            ser.setCheck(NumberUtils.toInt(getValue(values, i++), 1));
//
//            ser.setServerType(NumberUtils.toInt(getValue(values, i++), 1));
//
//            servers.add(ser);
//            serverMap.put(ser.getId(), ser);
//        }

        List<ServerConfig> list = ServerDaoImpl.getInstance().queryAllServer();
        Map<Integer, Server> serverMap = new ConcurrentHashMap<>();
        if (list!=null){
            for (ServerConfig serverConfig:list){
                Server server=changeEntity(serverConfig);
                serverMap.put(server.getId(),server);
            }
        }
        SysInfManager.serverMap = serverMap;
    }

    public Server getSimilarGameBest(int serverId){
        if (serverId>0){
            Server server0=loadServer(serverId);
            if (server0!=null){
                Server tempServer=null;
                int tempOnlineCount=Integer.MAX_VALUE;

                if (server0.getGameType().size()>0){
                    for (Map.Entry<Integer, Server> kv : serverMap.entrySet()) {
                        if (kv.getValue().getServerType() == server0.getServerType()&&kv.getValue().getGameType().containsAll(server0.getGameType())) {
                            if (kv.getValue().getOnlineCount() == 0) {
                                tempServer = kv.getValue();
                                break;
                            } else if (kv.getValue().getOnlineCount() < tempOnlineCount) {
                                tempOnlineCount = kv.getValue().getOnlineCount();
                                tempServer = kv.getValue();
                            }
                        }
                    }
                }else if (server0.getMatchType().size()>0){
                    for (Map.Entry<Integer, Server> kv : serverMap.entrySet()) {
                        if (kv.getValue().getServerType() == server0.getServerType()&&kv.getValue().getMatchType().containsAll(server0.getMatchType())) {
                            if (kv.getValue().getOnlineCount() == 0) {
                                tempServer = kv.getValue();
                                break;
                            } else if (kv.getValue().getOnlineCount() < tempOnlineCount) {
                                tempOnlineCount = kv.getValue().getOnlineCount();
                                tempServer = kv.getValue();
                            }
                        }
                    }
                }

                if (tempServer!=null){
                    synchronized (tempServer){
                        tempServer.setOnlineCount(tempServer.getOnlineCount()+1);
                    }
                }

                return tempServer;
            }
        }

        return null;
    }

    private static Server changeEntity(ServerConfig serverConfig){
        Server server=new Server();
        server.setServerType(serverConfig.getServerType());
        server.setChathost(serverConfig.getChathost());
        server.setGameType(str2IntList(serverConfig.getGameType()));
        server.setId(serverConfig.getId());
        server.setMatchType(str2IntList(serverConfig.getMatchType()));
        server.setOnlineCount(serverConfig.getOnlineCount());
        server.setExtend(serverConfig.getExtend());
        server.setHost(serverConfig.getHost());
        server.setIntranet(serverConfig.getIntranet());
        server.setName(serverConfig.getName());
        server.setTmpGameType(str2IntList(serverConfig.getTmpGameType()));
        return server;
    }

    private static List<Integer> str2IntList(String str){
        if (org.apache.commons.lang3.StringUtils.isBlank(str)){
            return new ArrayList<>();
        }else{
            List<Integer> list=new ArrayList<>();
            String[] strs=str.split(",");
            for (String temp:strs){
                if (NumberUtils.isDigits(temp)){
                    list.add(Integer.valueOf(temp));
                }
            }
            return list;
        }
    }

    public List<String> getIpList(int ipConfig) {
        return ipConfigList.get(ipConfig);
    }

    public static SysInfManager getInstance() {
        return _inst;
    }

    private String getPath(String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getClassLoader().getResource("").getPath());
        sb.append("csv/");
        sb.append(fileName);
        return sb.toString();
    }

    public static String loadRootUrl(Server server){
        if (server==null){
            return null;
        }else{
            String url=server.getIntranet();
            if (StringUtils.isBlank(url)){
                url=server.getHost();
            }

            if (StringUtils.isNotBlank(url)) {
                int idx = url.indexOf(".");
                if (idx > 0) {
                    idx = url.indexOf("/", idx);
                    if (idx > 0) {
                        url = url.substring(0, idx);
                    }
                }
                return url;
            }
        }
        return null;
    }

    public static String loadRootUrl(int serverId){
        return loadRootUrl(loadServer(serverId));
    }

    public Server getServer(int serverId) {
        return serverMap.get(serverId);
    }

    public Map<String, Version> getVersionMap() {
        return versionMap;
    }

    public void setIpBlackList(List<String> ipBlackList) {
        this.ipBlackList = ipBlackList;
    }

    public List<String> getIpBlackList() {
        return ipBlackList;
    }

    public void setMacBlackList(List<String> macBlackList) {
        this.macBlackList = macBlackList;
    }

    public List<String> getMacBlackList() {
        return macBlackList;
    }

    public void setDcBlackList(List<String> dcBlackList) {
        this.dcBlackList = dcBlackList;
    }

    public List<String> getDcBlackList() {
        return dcBlackList;
    }

    public List<Roster> getWhiteList() {
        return whiteList;
    }

    public List<String> getFlatIdBlackList() {
        return flatIdBlackList;
    }

    public void setFlatIdBlackList(List<String> flatIdBlackList) {
        this.flatIdBlackList = flatIdBlackList;
    }

    public List<Notice> getNotices() {
        for (int i = 0; i < notices.size(); i++) {
            long curMillis = System.currentTimeMillis();
            if (notices.get(i).getViewDate().getTime() < curMillis) {
                notices.remove(i);
                i--;
            }

        }

        return notices;
    }

    /**
     * getValue读取csv
     *
     * @param values
     * @param index
     * @return
     */
    private static String getValue(String[] values, int index) {
        if (index >= values.length) {
            GameBackLogger.SYS_LOG.error("getValue index > lenght-->" + index + ":" + values.toString());
            return "";
        }
        return values[index];
    }

    public List<Notice> getNoticesById(int id) {
        List<Notice> list = new ArrayList<Notice>();
        List<Notice> no_find_list = new ArrayList<Notice>();
        List<Notice> notices = getNotices();
        for (Notice bean : notices) {
            if (bean.getId() == id) {
                list.add(bean);
                break;

            } else {
                if (no_find_list.isEmpty()) {
                    no_find_list.add(bean);
                }
            }
        }
        if (!list.isEmpty()) {
            return list;
        } else {
            return no_find_list;
        }
    }

    public List<Notice> getNoticesByPf(String pf) {
        List<Notice> list = new ArrayList<Notice>();
        List<Notice> no_find_list = new ArrayList<Notice>();
        List<Notice> notices = getNotices();
        for (Notice bean : notices) {
            if (!StringUtils.isBlank(bean.getPf()) && pf.startsWith(bean.getPf())) {
                list.add(bean);
                break;

            } else {
                if (no_find_list.isEmpty()) {
                    no_find_list.add(bean);
                }
            }
        }
        if (!list.isEmpty()) {
            return list;
        } else {
            return no_find_list;
        }
    }

    public boolean isCorrectServer(int sId) {
        boolean result = false;
        for (Server server : loadServers()) {
            if (server.getId() == sId) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static String parseString(String value) {
        if (StringUtils.isBlank(value)) {
            return "0";
        } else {
            return value;
        }
    }

    public static int parseInt(String value) {
        if (StringUtils.isBlank(value)) {
            return 0;
        } else {
            return Integer.parseInt(value);
        }
    }

    /**
     * 读取csv文件
     *
     * @param filePath      csv目录下的子文件夹目录名/csv的文件名
     * @param includeHeader list是否包含第一行
     * @return List<String[]> String[]的每个值依次为csv文件每一行从左到右的单元格的值
     */
    private static List<String[]> readCSV(String filePath, boolean includeHeader) {
        List<String[]> list = new ArrayList<String[]>();
        if (!new File(filePath).exists()) {
            GameBackLogger.SYS_LOG.error("没有找到csv文件:" + filePath);
            return list;
        }
        CsvReader reader = null;
        try {
            reader = new CsvReader(filePath, ',', Charset.forName("UTF-8"));
            /** csv的第一行 * */
            reader.readHeaders();
            String[] headers = reader.getHeaders();
            if (includeHeader) {
                // 读取UTF-8格式有bug 需去掉第一个字符的空格
                headers[0] = headers[0].substring(1);
                list.add(headers);
            }
            /** 从第二行开始读 * */
            while (reader.readRecord()) {
                String[] values = reader.getValues();
                // if (values.length != 0 && !StringUtils.isBlank(values[0])) {
                if (values.length != 0) {
                    list.add(values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /** 关闭reader * */
            if (reader != null) {
                reader.close();
            }
        }
        return list;
    }

    public List<ServerFilter> getServerFilter() {
        return serverFilter;
    }

    public void setSdkConfigMap(Map<String, SdkUrlConfig> sdkConfigMap) {
        this.sdkConfigMap = sdkConfigMap;
    }

    public Map<String, SdkUrlConfig> getSdkConfigMap() {
        return sdkConfigMap;
    }

    public SdkUrlConfig getSdkConfig(String pf) {
        return sdkConfigMap.get(pf);
    }

    public void setSyvcFilter(List<SyvcFilter> syvcFilter) {
        this.syvcFilter = syvcFilter;
    }

    public List<SyvcFilter> getSyvcFilter() {
        return syvcFilter;
    }

    /**
     * 是否芈月传版本
     *
     * @return
     */
    public static boolean isMyz() {
        return LoginCacheContext.gameName != null && LoginCacheContext.gameName.equals("myz");
    }

    /**
     * 是否奇迹版本
     *
     * @return
     */
    public static boolean isQiji() {
        return LoginCacheContext.gameName != null && LoginCacheContext.gameName.equals("qiji");
    }

    public static boolean isTtgcq() {
        return LoginCacheContext.gameName == null || LoginCacheContext.gameName.equals("ttgcq");
    }

    public List<String> getMaskWordList() {
        return maskWordList;
    }

    public void setMaskWordList(List<String> maskWordList) {
        this.maskWordList = maskWordList;
    }

    public Map<Integer, MonitorMsg> getMonitorMap() {
        return monitorMap;
    }

    public Map<Integer, String> getSpareIpMap() {
        return spareIpMap;
    }

    public void refreshMonitor(MonitorMsg msg) {
        if (msg != null)
            monitorMap.put(msg.getServerId(), msg);
        // LogUtil.i("刷新monitor-->" + JacksonUtil.writeValueAsString(msg));
    }

//    public Server getBestMonitorServer(int gameType) {
//        return getBestMonitorServer(gameType, 1,false);
//    }
//
//    public Server getBestMonitorServer(int gameType,int serverType) {
//        return getBestMonitorServer(gameType, serverType,false);
//    }
//
//    public Server getBestMonitorServer(int gameType, boolean isMatch) {
//        return getBestMonitorServer(gameType, 1,isMatch);
//    }

//    /**
//     * @param gameType
//     * @param isMatch  是否比赛场
//     * @return
//     */
//    public Server getBestMonitorServer(int gameType,int serverType, boolean isMatch) {
//        // 过滤游戏类型服务器
//        List<Server> copy = new ArrayList<>(servers);
//
//        Iterator<Server> iterator = copy.iterator();
//        while (iterator.hasNext()) {
//            Server server = iterator.next();
//            if (isMatch) {
//                // 去掉不是比赛场的
//                if (server.getMatchType().isEmpty()) {
//                    iterator.remove();
//                    continue;
//                }
//            } else {
//                // 去掉是比赛场的
//                if (!server.getMatchType().isEmpty()) {
//                    iterator.remove();
//                    continue;
//                }
//            }
//
//            List<Integer> containsList = null;
//            if (!isMatch) {
//                containsList = server.getGameType();
//            } else {
//                containsList = server.getMatchType();
//            }
//            if (gameType != 0) {
//                if (!containsList.contains(gameType)) {
//                    iterator.remove();
//                }
//            } else {
//                if (!containsList.isEmpty()) {
//                    iterator.remove();
//                }
//            }
//
//        }
//
//        if (gameType != 0 && copy.isEmpty()) {
//            for (Server server : servers) {
//                if (isMatch) {
//                    if (!server.getMatchType().isEmpty()) {
//                        copy.add(server);
//                    }
//                } else {
//                    if (server.getMatchType().isEmpty() && server.getGameType().isEmpty()) {
//                        copy.add(server);
//                    }
//                }
//
//            }
//        }
//
//        if (copy.isEmpty()) {
//            return getBest(servers,serverType);
//        } else {
//            return getBest(copy,serverType);
//        }
//    }

//    /**
//     * 获得相同玩法最好的服
//     *
//     * @param serverId
//     * @return
//     */
//    public Server getSimilarGameBest(int serverId) {
//        Server bestServer = null;
//
//        if (serverId == 0) {
//            bestServer = getBestMonitorServer();
//            return bestServer;
//        }
//
//        // 获取上次玩的服
//        Server lastServer = serverMap.get(serverId);
//
//        if (lastServer == null) {
//            return null;
//        }
//
//        // 根据服id获得这个服的玩法
//        List<Integer> gameType = lastServer.getGameType();
//        boolean isMatch = false;
//        if (!gameType.isEmpty()) {
//            bestServer = getBestMonitorServer(gameType.get(0),lastServer.getServerType(), isMatch);
//        }
//
//        if (bestServer == null) {
//            bestServer = getBestMonitorServer();
//        }
//        return bestServer;
//    }
//
//    public Server getBest(List<Server> servers,int serverType) {
//        int best = 0;
//        List<Integer> audServerList = new ArrayList<>();
//        for (SyvcFilter filter : syvcFilter) {
//            int audserver = filter.getAuditServer();
//            audServerList.add(audserver);
//            // 审核版本
//        }
//
//        int minPlayerCount = 0;
//        for (Server server : servers) {
//            if (server.getServerType()!=serverType||audServerList.contains(server.getId())) {
//                // 如果是审核版本
//                continue;
//            }
//            if (best == 0) {
//                best = server.getId();
//            }
//            MonitorMsg msg = monitorMap.get(server.getId());
//            if (msg == null) {
//                continue;
//            }
////            if (msg.getOnlineCount() < LoginCacheContext.userMaxCount) {
////                best = msg.getServerId();
////                break;
////            }
//
//            if (minPlayerCount == 0 || msg.getOnlineCount() < minPlayerCount) {
//                // 对比哪个服的人比较少
//                minPlayerCount = msg.getOnlineCount();
//                best = msg.getServerId();
//            }
//        }
//        Server bestServer = serverMap.get(best);
//        if (bestServer == null) {
//            bestServer = servers.get(0);
//        }
//        return bestServer;
//    }

//    public Server getBestMonitorServer() {
//        return getBestMonitorServer(0);
//    }

//    public void checkServerIp(int serverId, String connectHost) {
//        Server server = serverMap.get(serverId);
//        if (server == null || server.getCheck() == 0) {
//            return;
//        }
//
//        if (spareIpMap.containsKey(serverId)) {
//            String temp = spareIpMap.get(serverId);
//            if (!temp.equals(connectHost)) {
//                return;
//            }
//        }
//
//        // 看看能不能连接到
//        boolean isConn = GameUtil.tryConnToServer(server);
//        if (isConn) {
//            // 已经恢复正常了
//            spareIpMap.remove(server.getId());
//            return;
//        }
//
//        String ip = GameUtil.findSpareConnIp(server);
//        if (StringUtils.isBlank(ip)) {
//            return;
//        }
//
//        spareIpMap.put(serverId, ip);
//        GameBackLogger.SYS_LOG.info("checkServerIp serverId:" + serverId + "spare -->" + ip);
//    }

//    public String getSpareIp(int serverId) {
//        return spareIpMap.get(serverId);
//    }

//    /**
//     * 获取现在连接的外网地址
//     *
//     * @param server
//     * @return
//     */
//    public String getServerHost(Server server) {
//        if (spareIpMap.containsKey(server.getId())) {
//            return spareIpMap.get(server.getId());
//        }
//        return server.getHost();
//    }

    public List<Lottery> initLottery() {
        if (lotteries.isEmpty()){
            synchronized (this){
                if (lotteries.isEmpty()) {
                    List<String[]> list = readCSV(getPath("lottery.csv"), false);
                    for (String[] values : list) {
                        Lottery ser = new Lottery();
                        int i = 0;
                        ser.setIndex(StringUtil.getIntValue(values, i++));
                        ser.setName(getValue(values, i++));
                        ser.setChance(StringUtil.getFloatValue(values, i++));
                        ser.setRoomCard(StringUtil.getIntValue(values, i++));
                        String state=StringUtil.getValue(values, i++);
                        ser.setState(NumberUtils.isDigits(state)?Integer.parseInt(state):1);
                        lotteries.add(ser);
                    }
                }
            }
        }
        return new ArrayList<>(lotteries);
    }

    public Map<String, CdkAward> getCdkAwards() {
        return CdkAwards;
    }

    public void setCdkAwards(Map<String, CdkAward> cdkAwards) {
        CdkAwards = cdkAwards;
    }

    public static void main(String[] args) {
        // String path =
        // "/F:/apache-tomcat-6.0.37/webapps/guajiLogin/WEB-INF/classes/";
        // System.out.println(path.substring(0, path.length() -
        // ("/WEB-INF/classes/".length())));
        // System.out.println(path.replace("/WEB-INF/classes/", ""));
//        Server server = SysInfManager.getInstance().getBestMonitorServer(15, 1,true);
//        System.out.println(JacksonUtil.writeValueAsString(server));
    }
}
