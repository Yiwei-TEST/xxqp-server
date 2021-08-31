// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/10/2020 09:18:45
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions;

import jbt.execution.core.ExecutionTask;

/** ModelAction class created from MMPM action chi. */
public class chi extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of chi. */
	public chi(jbt.model.core.ModelTask guard) {
		super(guard);
	}


	/**
	 * Returns a com.sy599.game.qipai.csmj.robot.actions.execution.chi task that
	 * is able to run this task.
	 */
	public ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.actions.execution.chi(this,
				executor, parent);
	}
}