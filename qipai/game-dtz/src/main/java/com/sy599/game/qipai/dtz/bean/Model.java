package com.sy599.game.qipai.dtz.bean;

import java.util.List;
import java.util.Vector;

public class Model {
	/** 手数 */
	int count;//手数
	/** 手数 */
	int value;//权值
	//一组牌
	/** 单张 */
	public List<String> a1=new Vector<String>(); //单张
	/** 对子 */
	public List<String> a2=new Vector<String>(); //对子
	/** 3带 */
	public List<String> a3=new Vector<String>(); //3带
	/** 连子 */
	public List<String> a123=new Vector<String>(); //连子
	/** 连牌 */
	public List<String> a112233=new Vector<String>(); //连牌
	/** 飞机 */
	public List<String> a111222=new Vector<String>(); //飞机
	/** 炸弹 */
	public List<String> a4=new Vector<String>(); //炸弹
}
