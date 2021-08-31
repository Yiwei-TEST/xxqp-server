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
import com.sy599.game.util.LogUtil;

import java.util.List;

/** ExecutionCondition class created from MMPM condition IsChiPai. */
public class IsChiPai extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsChiPai that is able to run a
	 * com.sy599.game.qipai.csmj.robot.conditions.IsChiPai.
	 */
	public IsChiPai(
			com.sy599.game.qipai.csmj.robot.conditions.IsChiPai modelTask,
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
		LogUtil.printDebug(this.getClass().getCanonicalName() + " spawned");
	}

	protected Status internalTick() {
		/*
		 * TODO: this method's implementation must be completed. This function
		 * should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING.
		 * No other values are allowed.
		 */

		List<CsMj> sypx  = (List<CsMj>) getContext().getVariable("canChiMj");
		CsMj deskMj = (CsMj) getContext().getVariable("deskpai");
		List<CsMj> chilist = MajiangAITool.IsZuyijuhua2(sypx,deskMj);
		LogUtil.printDebug(getContext().getVariable("userName")+" action:chi= "+chilist+" ===出牌："+deskMj);
		LogUtil.printDebug("sypx："+ sypx);
		if(null!=deskMj && null!=chilist && chilist.size()==2) {

			if(null!=chilist && chilist.size()>0){
				//用正确的牌去吃deskMj 8筒 ；[6筒, 7筒] b= [7筒, 9筒]
				getContext().setVariable("chiCards",chilist);
			}
			getContext().setVariable("chi",true);
			return Status.SUCCESS;
		}else{
			getContext().setVariable("chi",false);
			getContext().setVariable("action","guo");
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