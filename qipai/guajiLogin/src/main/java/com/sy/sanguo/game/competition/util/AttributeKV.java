package com.sy.sanguo.game.competition.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AttributeKV<T> {
	private T key;

	private T value;
}