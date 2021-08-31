// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/07/2020 15:21:51
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.conditions;

/** ModelCondition class created from MMPM condition IsShunzi. */
public class IsShunzi extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of IsShunzi. */
	public IsShunzi(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.sy599.game.qipai.pdkuai.robot.conditions.execution.IsShunzi
	 * task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.pdkuai.robot.conditions.execution.IsShunzi(
				this, executor, parent);
	}
}