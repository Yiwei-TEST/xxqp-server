// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/10/2020 09:18:44
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions;

/** ModelAction class created from MMPM action peng. */
public class peng extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of peng. */
	public peng(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.sy599.game.qipai.csmj.robot.actions.execution.peng task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.actions.execution.peng(this,
				executor, parent);
	}
}