// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 08/06/2020 10:08:32
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.conditions;

/** ModelCondition class created from MMPM condition IsDuoJiangDui. */
public class IsDuoJiangDui extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of IsDuoJiangDui. */
	public IsDuoJiangDui(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.sy599.game.qipai.csmj.robot.conditions.execution.IsDuoJiangDui task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.conditions.execution.IsDuoJiangDui(
				this, executor, parent);
	}
}