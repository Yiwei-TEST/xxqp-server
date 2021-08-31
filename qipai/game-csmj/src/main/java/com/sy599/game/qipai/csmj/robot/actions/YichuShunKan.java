// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 08/06/2020 10:08:31
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions;

/** ModelAction class created from MMPM action YichuShunKan. */
public class YichuShunKan extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of YichuShunKan. */
	public YichuShunKan(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.sy599.game.qipai.csmj.robot.actions.execution.YichuShunKan
	 * task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.actions.execution.YichuShunKan(
				this, executor, parent);
	}
}