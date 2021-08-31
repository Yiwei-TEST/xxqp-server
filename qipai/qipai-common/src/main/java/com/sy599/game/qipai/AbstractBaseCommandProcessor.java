package com.sy599.game.qipai;

import com.sy599.game.character.Player;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public abstract class AbstractBaseCommandProcessor<T extends Player>{
	public static AbstractBaseCommandProcessor getInstance() {
		return null;
	}

	public abstract void process(T player, MessageUnit message);
}
