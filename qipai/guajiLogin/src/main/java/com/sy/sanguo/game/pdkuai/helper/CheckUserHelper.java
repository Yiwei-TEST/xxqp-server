package com.sy.sanguo.game.pdkuai.helper;

import java.util.List;

import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.game.bean.SystemMessage;
import com.sy.sanguo.game.bean.VersionCheck;
import com.sy.sanguo.game.service.SysInfManager;
import org.apache.commons.lang3.math.NumberUtils;

public class CheckUserHelper {
	
//	/**
//	 *
//	 * @param extend
//	 * @param gamevc
//	 * @return
//	 */
//	public static boolean checkGameVersion(String extend,String gamevc) {
//
//		VersionCheck versionCheck = SysInfManager.versionCheck;
//		if (versionCheck == null) {
//			return false;
//		}
//
//		JsonWrapper wrapper=new JsonWrapper(extend);
//		int userVc=wrapper.getInt(4, 0);
//
//		if((gamevc==null || gamevc.equals(versionCheck.getVersionStr())) && (userVc==0 || userVc==versionCheck.getVersion())) {
//			return true;
//		}
//
//		return false;
//	}

	public static boolean checkVersion(String serverVersion,int serverIdx,String clientVersion,int clientIdx){
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
				result=NumberUtils.toInt(serverVersion.substring(idxS1+1),-1)>NumberUtils.toInt(clientVersion.substring(idxC1+1),-1);
			}else if (NumberUtils.isDigits(serverVersion)&&NumberUtils.isDigits(clientVersion)){
				result=NumberUtils.toInt(serverVersion,-1)>NumberUtils.toInt(clientVersion,-1);
			}
		}
		return result;
	}
	
	public static String getSystemMessage(List<SystemMessage> systemMessages) {
		
		if (systemMessages == null || systemMessages.isEmpty()) {
			return null;
		}
		
		for (SystemMessage systemMessage : systemMessages) {
			if (systemMessage.getId() == 1) {
				return systemMessage.getContent();
			}
		}
		
		return null;
	}
	
}
