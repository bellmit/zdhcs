package net.northking.atp.utils;

import net.northking.atp.entity.RequestWrapper;

/**
 * 用于获取分布式锁的请求对象
 */
public class LockEntityUtil {

    /**
     * 获取锁的请求对象，不需要等待时间
     * @param name
     * @return
     */
    public static RequestWrapper getRequestWrapper(String name) {
        return getRequestWrapper(name, null);
    }

    public static RequestWrapper getRequestWrapper(String name, String waitLockTime) {
        if (name == null) {
            return null;
        }
        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setName(name);
        requestWrapper.setValue(UUIDUtil.getUUIDWithoutDash());
        if (waitLockTime != null && !"".equals(waitLockTime)) {
            requestWrapper.setWaitLockTime(waitLockTime);
        }
        return requestWrapper;
    }

}
