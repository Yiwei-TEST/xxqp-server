// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/15/2020 09:08:18
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions;

/** ModelCondition class created from MMPM condition IsZuyijuhua. */
public class IsZuyijuhua extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of IsZuyijuhua. */
	public IsZuyijuhua(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.sy599.game.qipai.csmj.robot.conditions.execution.IsZuyijuhua task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.conditions.execution.IsZuyijuhua(
				this, executor, parent);
	}
}