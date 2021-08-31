package com.sy.sanguo.game.bean;

public class SyvcFilter {
	/**
	 * 黑名单:首位为关闭的最小id,第二位为最大(与白名单互斥)
	 */
	private int[] blackList;
	/**
	 * 白名单:首位为开启的最小id,第二位为最大(与黑名单互斥)
	 */
	private int[] whiteList;
	/**
	 * 版本号
	 */
	private String syvc;
	/**
	 * 是否显示公告
	 */
	private int useNotice;

	private int isIosAudit;

	private int auditServer;

	public int[] getBlackList() {
		return blackList;
	}

	public void setBlackList(int[] blackList) {
		this.blackList = blackList;
	}

	public int[] getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(int[] whiteList) {
		this.whiteList = whiteList;
	}

	public void setSyvc(String syvc) {
		this.syvc = syvc;
	}

	public String getSyvc() {
		return syvc;
	}

	public void setUseNotice(int useNotice) {
		this.useNotice = useNotice;
	}

	public int getUseNotice() {
		return useNotice;
	}

	public int getIsIosAudit() {
		return isIosAudit;
	}

	public void setIsIosAudit(int isIosAudit) {
		this.isIosAudit = isIosAudit;
	}

	public int getAuditServer() {
		return auditServer;
	}

	public void setAuditServer(int auditServer) {
		this.auditServer = auditServer;
	}

}
