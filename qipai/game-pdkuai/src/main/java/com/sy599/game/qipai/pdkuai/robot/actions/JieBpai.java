// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/15/2020 10:17:59
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.actions;

/** ModelAction class created from MMPM action JieBpai. */
public class JieBpai extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of JieBpai. */
	public JieBpai(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.sy599.game.qipai.pdkuai.robot.actions.execution.JieBpai
	 * task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.pdkuai.robot.actions.execution.JieBpai(
				this, executor, parent);
	}
}