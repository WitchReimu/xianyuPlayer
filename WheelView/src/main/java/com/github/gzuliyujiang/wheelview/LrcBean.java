package com.github.gzuliyujiang.wheelview;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class LrcBean {

    public long time;
    public String lrc;

    public LrcBean(long time, String lrc) {
        this.time = time;
        this.lrc = lrc;
    }

    public LrcBean() {
    }

}
