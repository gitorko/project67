package com.demo.project67.service;

import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;

public class HelperUtil {

    @SneakyThrows
    public static void delay(long inSeconds) {
        TimeUnit.SECONDS.sleep(inSeconds);
    }
}
