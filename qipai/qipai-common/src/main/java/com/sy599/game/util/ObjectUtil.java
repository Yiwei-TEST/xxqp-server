package com.sy599.game.util;

public final class ObjectUtil {
    private ObjectUtil() {
    }

    public final static <T> T newInstance(Class<T> cls) throws Exception {
        return cls.getConstructor().newInstance();
    }
}
