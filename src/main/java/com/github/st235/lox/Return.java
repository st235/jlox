package com.github.st235.lox;

import org.jetbrains.annotations.Nullable;

class Return extends RuntimeException {

    @Nullable
    final Object value;

    public Return(@Nullable Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
