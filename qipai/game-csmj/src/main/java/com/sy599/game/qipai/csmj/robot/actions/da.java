// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/10/2020 09:18:45
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions;

/** ModelAction class created from MMPM action da. */
public class da extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of da. */
	public da(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.sy599.game.qipai.csmj.robot.actions.execution.da task that
	 * is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.actions.execution.da(this,
				executor, parent);
	}
}