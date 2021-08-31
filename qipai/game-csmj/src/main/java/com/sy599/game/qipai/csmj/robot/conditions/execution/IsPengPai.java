// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/10/2020 09:18:45
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions.execution;

import com.sy599.game.qipai.csmj.robot.jbtAI.MajiangAITool;
import com.sy599.game.qipai.csmj.rule.CsMj;

import java.util.List;

/** ExecutionCondition class created from MMPM condition IsPengPai. */
public class IsPengPai extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsPengPai that is able to run a
	 * com.sy599.game.qipai.csmj.robot.conditions.IsPengPai.
	 */
	public IsPengPai(
			com.sy599.game.qipai.csmj.robot.conditions.IsPengPai modelTask,
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
		System.out.println(this.getClass().getCanonicalName() + " spawned");
	}

	protected Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		boolean canAction = (boolean) getContext().getVariable("peng");
		List<CsMj> sypx  = (List<CsMj>) getContext().getVariable("canPengMj");
	    System.out.println(getContext().getVariable("userName")+" action:peng="+canAction);
		if(null!=sypx && sypx.size()>0){
			CsMj deskpai = (CsMj) getContext().getVariable("deskpai");
			System.out.println("移除顺砍对牌型后能碰=====sypx:"+sypx+" ==deskpai:"+deskpai);
			if(MajiangAITool.CanPeng(sypx,deskpai)){
				getContext().setVariable("action","peng");
				return Status.SUCCESS;
			}else{
				return Status.FAILURE;
			}
		}else{
			return Status.FAILURE;
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