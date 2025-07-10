package com.chatapp.synk.util;

import java.util.UUID;

public class RandomUUIDGenerater {

    public static StringBuilder getId(String alias) {
        StringBuilder id = new StringBuilder();
        id.append(UUID.randomUUID());
        id.append("_");
        id.append(alias != null ? alias : "random");
        return id;
    }
}
