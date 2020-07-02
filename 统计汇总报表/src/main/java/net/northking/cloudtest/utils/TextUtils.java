package net.northking.cloudtest.utils;

/**
 * 字符串工具类
 */
public class TextUtils {
    /**
     * 判断字符串是否为空，注：多空格字符串也为空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        }
        return false;
    }
}
