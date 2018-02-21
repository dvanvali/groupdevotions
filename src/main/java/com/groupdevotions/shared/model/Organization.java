package com.groupdevotions.shared.model;

import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;

import java.io.Serializable;

/**
 * Created by DanV on 7/19/2016.
 */
public class Organization implements Serializable, KeyMirror, PostLoad {
    private static final long serialVersionUID = -8856564040553162855L;
    @Store(false) public String key;
    public String name;

    public String getKey() {
        return key;
    }


    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void postLoad() {
    }
}
