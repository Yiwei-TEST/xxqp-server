// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/10/2020 09:19:01
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.btlibrary;

import com.sy599.game.qipai.csmj.robot.actions.*;
import com.sy599.game.qipai.csmj.robot.conditions.*;

/**
 * 初级麻将AI
 * BT library that includes the trees read from the following files:
 * <ul>
 * <li>src/config/majiang.xbt</li>
 * </ul>
 */
public class MajiangBTLibrary implements jbt.execution.core.IBTLibrary {
	/** Tree generated from file src/config/majiang.xbt. */
	private static jbt.model.core.ModelTask majiang;

	/* Static initialization of all the trees. */
	static {
		majiang = new jbt.model.task.composite.ModelSelector(
				null,
				new jbt.model.task.composite.ModelSequence(
						null,
						new IsMoPai(
								null),
						new jbt.model.task.composite.ModelSelector(
								null,
								new jbt.model.task.composite.ModelSequence(
										null,
										new IsHuPai(
												null),
										new hu(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new IsGangPai(
												null),
										new gang(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new IsBuPai(
												null),
										new bu(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new da(
												null)))),
				new jbt.model.task.composite.ModelSequence(
						null,
						new IsJiePai(
								null),
						new jbt.model.task.composite.ModelSelector(
								null,
								new jbt.model.task.composite.ModelSequence(
										null,
										new IsHuPai(
												null),
										new hu(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new IsGangPai(
												null),
										new gang(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new IsBuPai(
												null),
										new bu(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new IsPengPai(
												null),
										new peng(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new IsChiPai(
												null),
										new guo(
												null)))));

	}

	/**
	 * Returns a behaviour tree by its name, or null in case it cannot be found.
	 * It must be noted that the trees that are retrieved belong to the class,
	 * not to the instance (that is, the trees are static members of the class),
	 * so they are shared among all the instances of this class.
	 */
	public jbt.model.core.ModelTask getBT(String name) {
		if (name.equals("majiang")) {
			return majiang;
		}
		return null;
	}

	/**
	 * Returns an Iterator that is able to iterate through all the elements in
	 * the library. It must be noted that the iterator does not support the
	 * "remove()" operation. It must be noted that the trees that are retrieved
	 * belong to the class, not to the instance (that is, the trees are static
	 * members of the class), so they are shared among all the instances of this
	 * class.
	 */
	public java.util.Iterator<jbt.util.Pair<String, jbt.model.core.ModelTask>> iterator() {
		return new BTLibraryIterator();
	}

	private class BTLibraryIterator
			implements
			java.util.Iterator<jbt.util.Pair<String, jbt.model.core.ModelTask>> {
		static final long numTrees = 1;
		long currentTree = 0;

		public boolean hasNext() {
			return this.currentTree < numTrees;
		}

		public jbt.util.Pair<String, jbt.model.core.ModelTask> next() {
			this.currentTree++;

			if ((this.currentTree - 1) == 0) {
				return new jbt.util.Pair<String, jbt.model.core.ModelTask>(
						"majiang", majiang);
			}

			throw new java.util.NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
