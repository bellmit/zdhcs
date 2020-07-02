package net.northking.atp.utils;

import java.util.UUID;

/**
 * UUID 工具类
 */
public class UUIDUtil {

    public static String getUUIDWithoutDash() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
