// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/16/2020 13:48:14
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.actions;

/** ModelAction class created from MMPM action JieBdanzhang. */
public class JieBdanzhang extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of JieBdanzhang. */
	public JieBdanzhang(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.sy599.game.qipai.pdkuai.robot.actions.execution.JieBdanzhang task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.pdkuai.robot.actions.execution.JieBdanzhang(
				this, executor, parent);
	}
}