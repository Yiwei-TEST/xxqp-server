// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 08/08/2020 10:52:54
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.actions;

/** ModelAction class created from MMPM action ChongzhiShouPai. */
public class ChongzhiShouPai extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of ChongzhiShouPai. */
	public ChongzhiShouPai(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.sy599.game.qipai.csmj.robot.actions.execution.ChongzhiShouPai task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.csmj.robot.actions.execution.ChongzhiShouPai(
				this, executor, parent);
	}
}