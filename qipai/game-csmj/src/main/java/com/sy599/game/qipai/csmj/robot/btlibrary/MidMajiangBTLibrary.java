// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 08/18/2020 10:40:05
// ******************************************************* 
package com.sy599.game.qipai.csmj.robot.btlibrary;

/**
 * BT library that includes the trees read from the following files:
 * <ul>
 * <li>src/config/midMajianXbt.xbt</li>
 * </ul>
 */
public class MidMajiangBTLibrary implements jbt.execution.core.IBTLibrary {
	/** Tree generated from file src/config/midMajianXbt.xbt. */
	private static jbt.model.core.ModelTask midMajiang;

	/* Static initialization of all the trees. */
	static {
		midMajiang = new jbt.model.task.composite.ModelSelector(
				null,
				new jbt.model.task.composite.ModelSequence(
						null,
						new com.sy599.game.qipai.csmj.robot.conditions.IsMoPai(
								null),
						new jbt.model.task.composite.ModelSelector(
								null,
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsHuPai(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.hu(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsGangPai(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.gang(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsBuPai(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.bu(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsWuCaoZuo(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.conditions.IsTinPai(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.DaMoreTin(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsWuGuanLian19(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.Da1or9(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsKaBianZhang(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.Da1or9(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsWuGuanLian3467(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.Da3or4or6or7(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsWuGuanLian258(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.Da2or5or8(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsXiangLianJiaKa(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.DaXiangLianWuGuan(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsXiangLianJiaDui(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.ChaiDui(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsBianZhangJiaDui(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.ChaiDui(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsKaZhangJiaDui(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.ChaiDui(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsDanDui(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.ChaiDui(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsBianZhang(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.Da1or9(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsKaZhang(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.ChaiKaZhang(
																null),
														new jbt.model.task.composite.ModelSelector(
																null,
																new jbt.model.task.composite.ModelSequence(
																		null,
																		new com.sy599.game.qipai.csmj.robot.conditions.IsYou19(
																				null),
																		new com.sy599.game.qipai.csmj.robot.actions.Da1or9(
																				null)),
																new jbt.model.task.composite.ModelSequence(
																		null,
																		new com.sy599.game.qipai.csmj.robot.conditions.IsYou3467(
																				null),
																		new com.sy599.game.qipai.csmj.robot.actions.Da3or4or6or7(
																				null)),
																new jbt.model.task.composite.ModelSequence(
																		null,
																		new com.sy599.game.qipai.csmj.robot.conditions.IsYou258(
																				null),
																		new com.sy599.game.qipai.csmj.robot.actions.Da2or5or8(
																				null)))),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsXiangLian(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.ChaiXiangLian(
																null),
														new jbt.model.task.composite.ModelSelector(
																null,
																new jbt.model.task.composite.ModelSequence(
																		null,
																		new com.sy599.game.qipai.csmj.robot.conditions.IsYou3467(
																				null),
																		new com.sy599.game.qipai.csmj.robot.actions.Da3or4or6or7(
																				null)),
																new jbt.model.task.composite.ModelSequence(
																		null,
																		new com.sy599.game.qipai.csmj.robot.conditions.IsYou258(
																				null),
																		new com.sy599.game.qipai.csmj.robot.actions.Da2or5or8(
																				null)))))))),
				new jbt.model.task.composite.ModelSequence(
						null,
						new com.sy599.game.qipai.csmj.robot.conditions.IsJiePai(
								null),
						new jbt.model.task.composite.ModelSelector(
								null,
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsHuPai(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.hu(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsGangPai(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.gang(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsTinPaiDa0(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.guo(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsBuPai(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.bu(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.conditions.IsPengPai(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.conditions.IsJiang(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.YichuShunKan(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsDuoJiangDui(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.peng(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.csmj.robot.actions.ChongzhiShouPai(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
																null),
														new com.sy599.game.qipai.csmj.robot.conditions.IsPengPai(
																null),
														new com.sy599.game.qipai.csmj.robot.actions.peng(
																null)))),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.csmj.robot.actions.ChongzhiShouPai(
												null),
										new com.sy599.game.qipai.csmj.robot.conditions.IsChiPai(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.YichuPaixing(
												null),
										new com.sy599.game.qipai.csmj.robot.conditions.IsChiPai(
												null),
										new com.sy599.game.qipai.csmj.robot.actions.chi(
												null)))));

	}

	/**
	 * Returns a behaviour tree by its name, or null in case it cannot be found.
	 * It must be noted that the trees that are retrieved belong to the class,
	 * not to the instance (that is, the trees are static members of the class),
	 * so they are shared among all the instances of this class.
	 */
	public jbt.model.core.ModelTask getBT(String name) {
		if (name.equals("midMajiang")) {
			return midMajiang;
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
						"midMajiang", midMajiang);
			}

			throw new java.util.NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
