// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/15/2020 10:17:59
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.actions.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action Jieduizi. */
public class Jieduizi extends jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of Jieduizi that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.Jieduizi.
	 */
	public Jieduizi(
			com.sy599.game.qipai.pdkuai.robot.actions.Jieduizi modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		super(modelTask, executor, parent);

	}

	protected void internalSpawn() {
		/*
		 * Do not remove this first line unless you know what it does and you
		 * need not do it.
		 */
		this.getExecutor().requestInsertionIntoList(
				jbt.execution.core.BTExecutor.BTExecutorList.TICKABLE, this);
		/* TODO: this method's implementation must be completed. */
//		LogUtil.printDebug(this.getClass().getCanonicalName() + " spawned");
		LogUtil.printDebug("接对子");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> disCards = getContext().getVariable("disCards") == null ? null: (List<Integer>)this.getContext().getVariable("disCards");
		if(disCards == null || disCards.size() <= 0){//对手出牌
			LogUtil.printDebug("没找到对手出的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(!CardTypeTool.isDisDuizi(disCards)){
			LogUtil.printDebug("对手出牌不是对子");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		int rivalDanpai=CardTool.loadCardValue(disCards.get(0));
		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (List<Integer>)this.getContext().getVariable("handCards");
		Map<Integer, List<List<Integer>>> allPaixing = CardTypeTool.getAllPaiXing(handCards,false);
		if(allPaixing.isEmpty()){
			LogUtil.printDebug("没有获取到牌型");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<List<Integer>> duizis = allPaixing.get(CardTypeTool.px_duizi);
		if(duizis.isEmpty()  || duizis.get(0).isEmpty()){
			LogUtil.printDebug("没有对子");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		List<List<Integer>> santiaos = allPaixing.get(CardTypeTool.px_santiao);
		List<List<Integer>> lianduis = allPaixing.get(CardTypeTool.px_liandui);
		List<List<Integer>> shunzis = allPaixing.get(CardTypeTool.px_shunzi);
		//对
		for (Integer dz : duizis.get(0)) {
			if(!santiaos.isEmpty()  && !santiaos.get(0).isEmpty() && santiaos.get(0).contains(dz)){//不拆3条
				continue;
			}
			boolean nochai = false;
			if(!lianduis.isEmpty()){
				for (List<Integer> ld:lianduis) {
					if(!ld.isEmpty() && ld.contains(dz)){
						nochai = true;
						break;
					}
				}
			}
			if(!shunzis.isEmpty()){
				for (List<Integer> sz:shunzis) {
					if(!sz.isEmpty() && sz.contains(dz)){
						nochai = true;
						break;
					}
				}
			}
			if(nochai){
				continue;
			}
			if(dz > rivalDanpai){
				List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_duizi, Arrays.asList(dz), handCards);
				if(pais.isEmpty()){
					continue;
				}
				getContext().setVariable("chupai", pais);
				return jbt.execution.core.ExecutionTask.Status.SUCCESS;
			}
		}
		if(!lianduis.isEmpty()){//没独对拆连对
			LogUtil.printDebug("拆连对");
			for (List<Integer> ld : lianduis) {
				if(ld.isEmpty()){
					continue;
				}
				for (Integer ldd : ld) {
					if(ldd > rivalDanpai){
						List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_duizi, Arrays.asList(ldd), handCards);
						if(pais.isEmpty()){
							continue;
						}
						getContext().setVariable("chupai", pais);
						return jbt.execution.core.ExecutionTask.Status.SUCCESS;
					}
				}
			}
		}
		if(!shunzis.isEmpty()){//拆顺子
			LogUtil.printDebug("拆顺子");
			for (List<Integer> szs: shunzis) {
				if(szs.isEmpty()){
					continue;
				}
				for (Integer ss : szs){
					if(!santiaos.isEmpty()  && !santiaos.get(0).isEmpty() && santiaos.get(0).contains(ss)){//不拆3条
						continue;
					}
					if(ss > rivalDanpai && duizis.get(0).contains(ss)){
						List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_duizi, Arrays.asList(ss), handCards);
						if(pais.isEmpty()){
							continue;
						}
						getContext().setVariable("chupai", pais);
						return jbt.execution.core.ExecutionTask.Status.SUCCESS;
					}
				}
			}
		}
		if(!santiaos.isEmpty()  && !santiaos.get(0).isEmpty()){//拆3条
			LogUtil.printDebug("拆3条");
			for (Integer dz : santiaos.get(0)) {
				if(dz > rivalDanpai){
					List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_duizi, Arrays.asList(dz), handCards);
					if(!pais.isEmpty()){
						getContext().setVariable("chupai", pais);
						return jbt.execution.core.ExecutionTask.Status.SUCCESS;
					}
				}
			}
		}
		if(!duizis.isEmpty()  && !duizis.get(0).isEmpty()){//对
			for (Integer dz : duizis.get(0)) {
				if(dz > rivalDanpai){
					List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_duizi, Arrays.asList(dz), handCards);
					if(pais.isEmpty()){
						continue;
					}
					getContext().setVariable("chupai", pais);
					return jbt.execution.core.ExecutionTask.Status.SUCCESS;
				}
			}
		}
		LogUtil.printDebug("接对失败");
		return jbt.execution.core.ExecutionTask.Status.FAILURE;
	}

	protected void internalTerminate() {
		/* TODO: this method's implementation must be completed. */
	}

	protected void restoreState(jbt.execution.core.ITaskState state) {
		/* TODO: this method's implementation must be completed. */
	}

	protected jbt.execution.core.ITaskState storeState() {
		/* TODO: this method's implementation must be completed. */
		return null;
	}

	protected jbt.execution.core.ITaskState storeTerminationState() {
		/* TODO: this method's implementation must be completed. */
		return null;
	}
}