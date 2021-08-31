package com.sy599.game.db.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sy599.game.util.StringUtil;
/**
 * 累计胜利奖励配置数据表
 * @author admin
 *
 */
public class TWinReward implements Serializable{
	private static final long serialVersionUID = 1L;
	private int id;
    private int winCount;
    private String goldenBean;
    private String baijinBean;
    
    private List<Integer> gBeanList ;
    private List<Integer> jBeanList ;
    
    
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getWinCount() {
		return winCount;
	}
	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}
	public String getGoldenBean() {
		return goldenBean;
	}
	public void setGoldenBean(String goldenBean) {
		this.goldenBean = goldenBean;
	}
	public String getBaijinBean() {
		return baijinBean;
	}
	public void setBaijinBean(String baijinBean) {
		this.baijinBean = baijinBean;
	}
	
	public void initGoldenBeanList(String goldenBean){
		gBeanList = StringUtil.explodeToIntList(goldenBean,"_");
		
	}

	public void initBaijinBeanList(String baijinBean){
		jBeanList = StringUtil.explodeToIntList(baijinBean,"_");
	}
	public List<Integer> getgBeanList() {
		return gBeanList;
	}
	public void setgBeanList(List<Integer> gBeanList) {
		this.gBeanList = gBeanList;
	}
	public List<Integer> getjBeanList() {
		return jBeanList;
	}
	public void setjBeanList(List<Integer> jBeanList) {
		this.jBeanList = jBeanList;
	}
	
	

}
