package com.outlook.hxsw.settlements.engine.data.codecs;

import java.util.function.Supplier;

public final class DecodeException extends RuntimeException implements Supplier<String> {
    public final String message;
    public DecodeException(String message) {
        this.message = message;
    }

    @Override
    public String get() {
        return message;
    }
}