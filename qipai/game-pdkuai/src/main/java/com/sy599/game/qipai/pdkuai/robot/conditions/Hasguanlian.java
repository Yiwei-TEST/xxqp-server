// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/15/2020 10:17:59
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.conditions;

/** ModelCondition class created from MMPM condition Hasguanlian. */
public class Hasguanlian extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of Hasguanlian. */
	public Hasguanlian(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.sy599.game.qipai.pdkuai.robot.conditions.execution.Hasguanlian task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.pdkuai.robot.conditions.execution.Hasguanlian(
				this, executor, parent);
	}
}