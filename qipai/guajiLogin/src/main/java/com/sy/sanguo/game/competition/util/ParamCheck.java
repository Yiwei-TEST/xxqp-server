package com.sy.sanguo.game.competition.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.sy.sanguo.game.competition.model.db.CompetitionApplyDB;

/**
 * @author Guang.OuYang
 * @date 2020/5/22-9:48
 */
public class ParamCheck {

	public static <T, R> R check(T t, ConsumerC c) {
		return t == null ? null : (R) c.invoke();
	}

	public static <T, R> R check(T t, Function<T, R> c) {
		return t == null ? null : (R) c.apply(t);
	}

	public static <T> T defaultVal(T t, T defaultVal) {
		return t == null ? defaultVal : t;
	}

	public static <T> Boolean isNull(ConsumerC c) {
		return c.invoke() == null;
	}

	public static <T> void isNotNullSet(ConsumerC c, Consumer<T> b) {
		if (!isNull(c::invoke)) {
			b.accept((T) c.invoke());
		}
	}

	public static <T> void isTrueSet(ConsumerC c, Runnable trueArg) {
		if ((boolean) c.invoke()) {
			trueArg.run();
		}
	}

	public static <T> void isTrueSet(ConsumerC c, Runnable trueArg, Runnable falseArg) {
		if ((boolean) c.invoke()) {
			trueArg.run();
		}else{
			falseArg.run();
		}
	}

	public static <T> Boolean isMultilOrNull(ConsumerC... c) {
		return Stream.of(c).anyMatch(a -> a.invoke() == null);
	}

	public static interface ConsumerC<T> {
		public T invoke();
	}

	public static void main(String[] args) {

		CompetitionApplyDB competitionApplyDB = new CompetitionApplyDB();


		System.out.println(isNull(competitionApplyDB::getUserId));
	}
}
