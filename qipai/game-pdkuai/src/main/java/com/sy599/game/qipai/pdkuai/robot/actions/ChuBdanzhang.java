// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/10/2020 15:32:16
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.actions;

/** ModelAction class created from MMPM action ChuBdanzhang. */
public class ChuBdanzhang extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of ChuBdanzhang. */
	public ChuBdanzhang(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.sy599.game.qipai.pdkuai.robot.actions.execution.ChuBdanzhang task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.sy599.game.qipai.pdkuai.robot.actions.execution.ChuBdanzhang(
				this, executor, parent);
	}
}