package com.sy599.game.util;

import com.sy599.game.msg.serverPacket.ComMsg.ComRes;

import java.util.ArrayList;
import java.util.List;

public class SendMsgUtil {
	/**
	 * @param code
	 * @param params
	 * @return ComRes.Builder
	 */
	public static ComRes.Builder buildComRes(int code, Object... params) {
		ComRes.Builder res = ComRes.newBuilder();
		res.setCode(code);
		if (params != null) {
			List<Integer> ints = new ArrayList<>();
			List<String> strs = new ArrayList<>();

			for (Object o : params) {
				if (o instanceof String) {
					String temp = o.toString();
					if(GameConfigUtil.groupToQinYouQuan == 1) {
						if(temp.contains("俱乐部"))
							temp = temp.replace("俱乐部", "亲友圈");
						if(temp.contains("军团"))
							temp = temp.replace("军团", "亲友圈");
					}
					strs.add(temp);
				} else if (o instanceof Integer) {
					ints.add((Integer) o);
				} else if (o instanceof List) {
					for (Object l : (List)o) {
						if (l instanceof String) {
							String temp = l.toString();
							if(GameConfigUtil.groupToQinYouQuan == 1) {
								if(temp.contains("俱乐部"))
									temp = temp.replace("俱乐部", "亲友圈");
								if(temp.contains("军团"))
									temp = temp.replace("军团", "亲友圈");
							}
							strs.add(temp);
						} else if (l instanceof Integer) {
							ints.add((Integer) l);
						}
					}
				}
			}
			if (!ints.isEmpty()) {
				res.addAllParams(ints);
			}
			if (!strs.isEmpty()) {
				res.addAllStrParams(strs);
			}
		}
		return res;
	}
}
