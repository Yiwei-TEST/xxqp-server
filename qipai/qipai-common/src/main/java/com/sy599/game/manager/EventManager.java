package com.sy599.game.manager;

import com.sy599.game.character.Player;

public class EventManager {
	private static EventManager _inst = new EventManager();

	public static EventManager getInstance() {
		return _inst;
	}

	public void post(Player player, int days, int[] data) {
	}
}
