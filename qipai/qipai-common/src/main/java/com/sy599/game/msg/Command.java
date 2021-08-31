package com.sy599.game.msg;

import com.sy599.game.gcommand.BaseCommand;

public class Command {
    private final Class<? extends BaseCommand> cmd;
    private final boolean wait;

    public Command(Class<? extends BaseCommand> cmd, boolean wait){
        this.cmd=cmd;
        this.wait=wait;
    }

    public Class<? extends BaseCommand> getCmd() {
        return cmd;
    }

    public boolean isWait() {
        return wait;
    }
}
