// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 07/16/2020 13:48:22
// ******************************************************* 
package com.sy599.game.qipai.pdkuai.robot.btlibrary;

/**
 * BT library that includes the trees read from the following files:
 * <ul>
 * <li>src/config/paodekuai.xbt</li>
 * </ul>
 */
public class PaodekuaiBTLibrary implements jbt.execution.core.IBTLibrary {
	/** Tree generated from file src/config/paodekuai.xbt. */
	private static jbt.model.core.ModelTask pdk;

	/* Static initialization of all the trees. */
	static {
		pdk = new jbt.model.task.composite.ModelSelector(
				null,
				new jbt.model.task.composite.ModelSequence(
						null,
						new com.sy599.game.qipai.pdkuai.robot.conditions.IsJiePai(
								null),
						new jbt.model.task.composite.ModelSelector(
								null,
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.IsZhadan(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.JieZhadan(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.IsFeiji(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.Jiefeiji(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.IsShunzi(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.JieShunzi(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.IsSanzhang(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.Jiesanzhang(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.IsLiandui(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.Jieliandui(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.IsDuizi(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.Jieduizi(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.IsDanpai(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new jbt.model.task.decorator.ModelInverter(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																		null)),
														new com.sy599.game.qipai.pdkuai.robot.actions.Jiedanpai(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.actions.JieBdanzhang(
																null)))),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.actions.Chuzhadan(
												null)))),
				new jbt.model.task.composite.ModelSequence(
						null,
						new com.sy599.game.qipai.pdkuai.robot.conditions.IsChuPai(
								null),
						new jbt.model.task.composite.ModelSelector(
								null,
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum7duo(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.GetMaxPaixing(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.ChuPaixing(
												null)),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum7(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasFeiji(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.Chufeiji(
																null)),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBShunzi(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBshunzi(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasShunzi(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.ShenNum2(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasJBdanpai(
																										null),
																								new jbt.model.task.decorator.ModelInverter(
																										null,
																										new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																												null)),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chushunzi(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum1(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chushunzi(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chushunzi(
																						null))))),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBSanzhang(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBsanzhang(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasSanzhang(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasJBdanpai(
																						null),
																				new jbt.model.task.decorator.ModelInverter(
																						null,
																						new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																								null)),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chusanzhang(
																						null))))),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBLiandui(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBliandui(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasLiandui(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.ShenNum3(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasJBdanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																										null),
																								new jbt.model.task.decorator.ModelInverter(
																										null,
																										new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																												null)),
																								new jbt.model.task.decorator.ModelInverter(
																										null,
																										new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																												null)),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuMdanpai(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chuliandui(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.ShenNum1(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chuliandui(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chuliandui(
																						null))))),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new jbt.model.task.decorator.ModelInverter(
																		null,
																		new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																				null)),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasJBdanpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																										null),
																								new jbt.model.task.composite.ModelSelector(
																										null,
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																														null)),
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																														null)))),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																										null))),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																						null)))),
														new jbt.model.task.composite.ModelSequence(
																null,
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																						null))))))),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum6(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasFeiji(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.Chufeiji(
																null)),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBShunzi(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBshunzi(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasShunzi(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum1(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chushunzi(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chushunzi(
																						null))))),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBSanzhang(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBsanzhang(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasSanzhang(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuJBdanpai(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chusanzhang(
																						null))))),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBLiandui(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBliandui(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasLiandui(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum2(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasJBdanpai(
																										null),
																								new jbt.model.task.decorator.ModelInverter(
																										null,
																										new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																												null)),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chuliandui(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chuliandui(
																						null))))),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new jbt.model.task.decorator.ModelInverter(
																		null,
																		new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																				null)),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																										null),
																								new jbt.model.task.composite.ModelSelector(
																										null,
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuMduizi(
																														null)),
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.conditions.HasJBdanpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																														null)),
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																														null)))),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasJBdanpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																						null)))),
														new jbt.model.task.composite.ModelSequence(
																null,
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																										null),
																								new jbt.model.task.composite.ModelSelector(
																										null,
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuMduizi(
																														null)),
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																														null),
																												new jbt.model.task.composite.ModelSelector(
																														null,
																														new jbt.model.task.composite.ModelSequence(
																																null,
																																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																																		null),
																																new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																																		null),
																																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																																		null)),
																														new jbt.model.task.composite.ModelSequence(
																																null,
																																new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																																		null),
																																new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																																		null)))),
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																														null)))),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																						null))))))),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum5(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasShunzi(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.Chushunzi(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasSanzhang(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.Chusanzhang(
																null)),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBLiandui(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBliandui(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasLiandui(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chuliandui(
																						null))))),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new jbt.model.task.decorator.ModelInverter(
																		null,
																		new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																				null)),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																										null),
																								new jbt.model.task.composite.ModelSelector(
																										null,
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																														null)),
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																														null)))),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																										null),
																								new jbt.model.task.composite.ModelSelector(
																										null,
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																														null)),
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																														null)))),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																						null)))),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																						null)))),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																		null))))),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum4(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasLiandui(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.Chuliandui(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasSanzhang(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.Chusanzhang(
																null)),
												new jbt.model.task.composite.ModelSelector(
														null,
														new jbt.model.task.composite.ModelSequence(
																null,
																new jbt.model.task.decorator.ModelInverter(
																		null,
																		new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																				null)),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasJBdanpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																										null),
																								new jbt.model.task.composite.ModelSelector(
																										null,
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																														null)),
																										new jbt.model.task.composite.ModelSequence(
																												null,
																												new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																														null),
																												new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																														null)))),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																						null)))),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																		null)),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																		null),
																new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																		null),
																new jbt.model.task.composite.ModelSelector(
																		null,
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																						null)),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.YichuPaixing(
																						null),
																				new jbt.model.task.composite.ModelSelector(
																						null,
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																										null)),
																						new jbt.model.task.composite.ModelSequence(
																								null,
																								new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																										null),
																								new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																										null)))),
																		new jbt.model.task.composite.ModelSequence(
																				null,
																				new com.sy599.game.qipai.pdkuai.robot.actions.Chongzhihandpai(
																						null),
																				new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																						null)))),
														new jbt.model.task.composite.ModelSequence(
																null,
																new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																		null))))),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum3(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasSanzhang(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.Chusanzhang(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDuizi(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuBduizi(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																null),
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new jbt.model.task.decorator.ModelInverter(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																		null)),
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuMdanpai(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																null)))),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum2(
												null),
										new jbt.model.task.composite.ModelSelector(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasDuizi(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuSduizi(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.conditions.HasBDanpai(
																null),
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new jbt.model.task.decorator.ModelInverter(
																null,
																new com.sy599.game.qipai.pdkuai.robot.conditions.Xiabaodan(
																		null)),
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
																null)),
												new jbt.model.task.composite.ModelSequence(
														null,
														new com.sy599.game.qipai.pdkuai.robot.actions.ChuBdanpai(
																null)))),
								new jbt.model.task.composite.ModelSequence(
										null,
										new com.sy599.game.qipai.pdkuai.robot.conditions.HandNum1(
												null),
										new com.sy599.game.qipai.pdkuai.robot.actions.ChuSdanpai(
												null)))));

	}

	/**
	 * Returns a behaviour tree by its name, or null in case it cannot be found.
	 * It must be noted that the trees that are retrieved belong to the class,
	 * not to the instance (that is, the trees are static members of the class),
	 * so they are shared among all the instances of this class.
	 */
	public jbt.model.core.ModelTask getBT(java.lang.String name) {
		if (name.equals("pdk")) {
			return pdk;
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
	public java.util.Iterator<jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>> iterator() {
		return new BTLibraryIterator();
	}

	private class BTLibraryIterator
			implements
			java.util.Iterator<jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>> {
		static final long numTrees = 1;
		long currentTree = 0;

		public boolean hasNext() {
			return this.currentTree < numTrees;
		}

		public jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask> next() {
			this.currentTree++;

			if ((this.currentTree - 1) == 0) {
				return new jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>(
						"pdk", pdk);
			}

			throw new java.util.NoSuchElementException();
		}

		public void remove() {
			throw new java.lang.UnsupportedOperationException();
		}
	}
}
