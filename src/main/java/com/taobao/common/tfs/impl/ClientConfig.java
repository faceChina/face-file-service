package com.taobao.common.tfs.impl;

import com.taobao.common.tfs.etc.TfsConstant;

public class ClientConfig {
    // segment
    public static int BATCH_COUNT = TfsConstant.MAX_BATCH_COUNT;
    public static int SEGMENT_LENGTH = TfsConstant.MAX_SEGMENT_LENGTH;

    // cache
    public static int CACHEITEM_COUNT = 500000;
    public static int CACHE_TIME = 600000; // 10min

    // timeworker interval
    public static int GC_INTERVAL = 43200000;       // 0.5 day
    public static int CACHEMETRIC_INTERVAL = 60000; // 60s

    // gc expired time
    public static int GC_EXPIRED_TIME = TfsConstant.MIN_GC_EXPIRED_TIME; // 1 day
}