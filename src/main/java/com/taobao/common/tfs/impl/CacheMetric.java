/*
 * (C) 2007-2010 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */
package com.taobao.common.tfs.impl;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class CacheMetric  extends TimerTask {
    private static final Log log = LogFactory.getLog(CacheMetric.class);

    // consider efficiency and current use,
    // no (need) atomic guarantee. user should be responsible.
    private int hitCount = 0;
    private int removeCount = 0;
    private int missCount = 0;

    public int getHitCount() {
        return hitCount;
    }

    public void addHitCount() {
        hitCount++;
    }

    public int getMissCount() {
        return missCount;
    }

    public void addMissCount() {
        missCount++;
    }

    public int getRemoveCount() {
        return removeCount;
    }

    public void addRemoveCount() {
        removeCount++;
    }

    public void run() {
        log.warn("CACHE STATUS." +
                 " hit: " +  hitCount +
                 " miss: " + missCount +
                 " remove: " + removeCount);
        clear();
    }

    private void clear() {
        hitCount = missCount = removeCount = 0;
    }
}
