// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/16/2020 13:48:14
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.actions.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;

/** ExecutionAction class created from MMPM action JieBdanzhang. */
public class JieBdanzhang extends
		jbt.execution.task.leaf.action.ExecutionAction {

	/**
	 * Constructor. Constructs an instance of JieBdanzhang that is able to run a
	 * com.sy599.game.qipai.pdkuai.robot.actions.JieBdanzhang.
	 */
	public JieBdanzhang(
			com.sy599.game.qipai.pdkuai.robot.actions.JieBdanzhang modelTask,
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
		LogUtil.printDebug("接最大的单张");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		List<Integer> disCards = getContext().getVariable("disCards") == null ? null: (List<Integer>)this.getContext().getVariable("disCards");
		if(disCards == null || disCards.size() <= 0){//对手出牌
			LogUtil.printDebug("没找到对手出的牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(!CardTypeTool.isDisDanpai(disCards)){
			LogUtil.printDebug("对手出牌不是单牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		

		List<Integer> handCards = getContext().getVariable("handCards") == null ? new ArrayList<>(): (ArrayList<Integer>)this.getContext().getVariable("handCards");
		if(handCards.isEmpty()){
			LogUtil.printDebug("没有获取到手牌");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		int rivalDanpai=CardTool.loadCardValue(disCards.get(0));
		List<Integer> myCardVal = CardTool.getCardsVals(handCards);
		Collections.sort(myCardVal);
		int  myMaxDp= myCardVal.get(myCardVal.size()-1);
		int num = CardTool.getCardNum(handCards, myMaxDp);
		if(num == 4){//炸弹
			LogUtil.printDebug("出最大单牌不拆炸弹");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		if(myMaxDp > rivalDanpai){
			List<Integer> pais = CardTypeTool.getPaisByPaiXing(CardTypeTool.px_danpai, Arrays.asList(myMaxDp), handCards);
			if(pais.isEmpty()){
				LogUtil.printDebug("没有拿到牌");
				return jbt.execution.core.ExecutionTask.Status.FAILURE;
			}
			getContext().setVariable("chupai", pais);
			return jbt.execution.core.ExecutionTask.Status.SUCCESS;
		}else{
			LogUtil.printDebug("手上的打牌接不住");
			return jbt.execution.core.ExecutionTask.Status.FAILURE;
		}
		
		
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