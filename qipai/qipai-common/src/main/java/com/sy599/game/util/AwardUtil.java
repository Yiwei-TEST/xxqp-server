package com.sy599.game.util;

import com.sy599.game.character.Player;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.msg.AwardMsg;
import com.sy599.game.staticdata.model.Award;

import java.util.HashMap;
import java.util.Map;

public class AwardUtil {
	public static Map<Integer, Integer> buildAwardMap(Award award) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		int cards = award.getCards() + award.getFreeCards();

		if (cards > 0) {
			map.put(-1, cards);
		}
		return map;
	}

	public static AwardMsg changeAward(Player player, Award award) {
		int cards = award.getCards() + award.getFreeCards();
		if (award.getFreeCards() != 0 || award.getCards() != 0) {
			player.changeCards(award.getFreeCards(), award.getCards(), false, CardSourceType.activity_drawLottery);
		}
		// if (award.getCards() > 0) {
		// // player.changeCards(award.getCards(),false);
		// }
		AwardMsg msg = new AwardMsg();
		if (cards > 0) {
			msg.setCards(cards);
			player.writeCardsMessage(cards);
			// player.saveBaseInfo();
		}
		return msg;
	}
}
