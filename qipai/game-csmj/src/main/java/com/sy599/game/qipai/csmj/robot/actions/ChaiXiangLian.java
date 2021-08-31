// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/15/2020 09:08:18
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions;

/** ModelAction class created from MMPM action ChaiXiangLian. */
public class ChaiXiangLian extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of ChaiXiangLian. */
	public ChaiXiangLian(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.sy599.game.qipai.csmj.robot.actions.execution.ChaiXiangLian
	 * task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.actions.execution.ChaiXiangLian(
				this, executor, parent);
	}
}