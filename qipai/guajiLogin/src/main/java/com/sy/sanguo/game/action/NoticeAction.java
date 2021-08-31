package com.sy.sanguo.game.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.struts.StringResultType;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.bean.DBNotice;
import com.sy.sanguo.game.dao.NoticeDaoImpl;

public class NoticeAction extends GameStrutsAction {

	private static final long serialVersionUID = 303652981460926007L;
	public static List<DBNotice> noticeList = new ArrayList<DBNotice>();
	public String result = "";
	private NoticeDaoImpl noticeDaoImpl;

	public void initDBNotice() {
	}

	public String execute() throws Exception {
		String operate = getRequest().getParameter("operate");
		if ("selectNoticeByServerId".equals(operate)) {
			return this.selectNoticeByServerId();
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String selectNoticeByServerId() throws Exception {
		noticeList = noticeDaoImpl.loadNotice();
		int serverId = Integer.parseInt(getRequest().getParameter("serverId"));
		String pf = getRequest().getParameter("pf");
		String sysign = getRequest().getParameter("sysign");
		String sytime = getRequest().getParameter("sytime");
		String md5 = MD5Util.getStringMD5(sytime + "7HGO4K61M8N2D9LARSPU");
		JSONObject json = JSONObject.parseObject("{}");
		if (!sysign.equals(md5)) {
			json.put("code", 1);
			this.result = JacksonUtil.writeValueAsString(json);
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		DBNotice notice = getNoticeByServiceIdAndPf(serverId, pf);
		String content = "暂无公告";
		if (notice != null) {
			content = notice.getContent();
			if (!StringUtils.isBlank(pf)) {
				if (pf.equals("web1758")) {
					content = content.replace("490292138", "280794964");
				} else if (pf.equals("webhd")) {
					content = content.replace("玩家交流群：490292138（进群送礼包）！", "");
				}
			}
		}
		json.put("code", 0);
		json.put("noticeContent", content);
		this.result = JacksonUtil.writeValueAsString(json);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	private DBNotice getNoticeByServiceIdAndPf(int serverId, String pf) {
		DBNotice notice = null;
		DBNotice def = null;
		for (int i = 0; i < noticeList.size(); i++) {
			notice = noticeList.get(i);
			if (notice.getId() == 9999) {
				def = noticeList.get(i);
			}
			String serverIdStr = notice.getServerIds();
			String pfStr = notice.getPfs();
			if (StringUtils.isBlank(serverIdStr)) {
				continue;
			}
			String[] serverIds = serverIdStr.split(",");
			List<String> serverIdList = Arrays.asList(serverIds);

			boolean hasPf = false;// 是否有平台条件
			String[] pfs = null;
			List<String> pfList = null;
			if (!StringUtils.isBlank(pfStr)) {
				pfs = pfStr.split(",");
				pfList = Arrays.asList(pfs);
				hasPf = true;
			}

			if (serverIdList.contains(String.valueOf(serverId)) && hasPf && pfList.contains(pf)) {
				return notice;
			} else if (serverIdList.contains(String.valueOf(serverId)) && !hasPf) {
				return notice;
			}
		}
		if (noticeList.isEmpty()) {
			return null;
		} else {
			if (def != null) {
				return def; //id 9999 默认公告
			} else {
				return noticeList.get(0); // 如果找不到 给第一个

			}
		}
	}

	public String getResult() {
		return result;
	}

	public void setNoticeDaoImpl(NoticeDaoImpl noticeDaoImpl) {
		this.noticeDaoImpl = noticeDaoImpl;
	}

	public NoticeDaoImpl getNoticeDaoImpl() {
		return noticeDaoImpl;
	}
}
