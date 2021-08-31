package com.sy599.game.qipai.yywhz.rule;

import java.util.List;

/**
 * @author liuping
 * 鬼胡子门子
 */
public class GuihuziMenzi {

	private List<Integer> menzi;
	
	private int type;// 0普通门子  1对子门子 2二七十门子

	public List<Integer> getMenzi() {
		return menzi;
	}
	
	public GuihuziMenzi(List<Integer> menzi, int type) {
		this.menzi = menzi;
		this.type = type;
	}

	public void setMenzi(List<Integer> menzi) {
		this.menzi = menzi;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String toString() {
		return menzi.get(0) + "&" + menzi.get(1);
	}
	
	public boolean equals(Object o) {
		if(o == this)
			return true;
		if(o instanceof GuihuziMenzi) {
			GuihuziMenzi menzi = (GuihuziMenzi) o;
			if(menzi.getMenzi().containsAll(this.menzi)) {
				return true;
			}
		}
		return false;
	}
}
