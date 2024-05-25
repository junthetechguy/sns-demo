package com.fastcampus.sns.util;

import java.util.Optional;

public class ClassUtils {
    // clazz가 null이 아니고 Object o의 instance라면 casting을 하도록 하여 safe-casting을 진행하는 method
    public static <T> Optional<T> getSafeCastInstance(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? Optional.of(clazz.cast(o)) : Optional.empty();
    }
}
