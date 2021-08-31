// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 08/18/2020 10:39:50
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions;

/** ModelCondition class created from MMPM condition IsTinPaiDa0. */
public class IsTinPaiDa0 extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of IsTinPaiDa0. */
	public IsTinPaiDa0(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.sy599.game.qipai.csmj.robot.conditions.execution.IsTinPaiDa0 task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.conditions.execution.IsTinPaiDa0(
				this, executor, parent);
	}
}