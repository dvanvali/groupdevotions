package com.groupdevotions.mock.model;

import com.groupdevotions.shared.model.Config;

public class MockConfigFactory {
    static public Config buildConfig() {
        return Config.getInstance();
    }
}
