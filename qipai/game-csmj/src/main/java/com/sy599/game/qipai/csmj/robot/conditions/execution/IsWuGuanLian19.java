// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 07/15/2020 09:08:18
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions.execution;

import com.sy599.game.qipai.csmj.robot.jbtAI.MajiangAITool;
import com.sy599.game.qipai.csmj.rule.CsMj;

import java.util.List;

/** ExecutionCondition class created from MMPM condition IsWuGuanLian19. */
public class IsWuGuanLian19 extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsWuGuanLian19 that is able to run
	 * a com.sy599.game.qipai.csmj.robot.conditions.IsWuGuanLian19.
	 */
	public IsWuGuanLian19(
			com.sy599.game.qipai.csmj.robot.conditions.IsWuGuanLian19 modelTask,
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
		//System.out.println(this.getClass().getCanonicalName() + " spawned");
	}

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */
		boolean result  = (boolean) getContext().getVariable("YichuPaixing");
		System.out.println(this.getClass().getCanonicalName()+" YichuPaixing:"+result);
		if(result){
		 	List<CsMj> list = (List<CsMj>) getContext().getVariable("YichuPaixingCard");
			System.out.println("list==="+list);
		 	List<CsMj> res =MajiangAITool.IsWuGuanLian19(list);
			System.out.println("======res:"+res);
			if(res.size()>0){
				getContext().setVariable("action","da");
				getContext().setVariable("chup",res.get(0));
				System.out.println(this.getClass().getCanonicalName()+"====出牌:"+res.get(0));
				return jbt.execution.core.ExecutionTask.Status.SUCCESS;
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