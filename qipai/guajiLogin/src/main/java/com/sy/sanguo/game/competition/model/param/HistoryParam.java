package com.sy.sanguo.game.competition.model.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Guang.OuYang
 * @date 2020/8/6-14:01
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryParam {
	private String total;
	private String one;
	private String two;
	private String three;
}
