package com.wtls.blog_server.utils;

import com.wtls.blog_server.exception.BusinessValidateException;
import java.util.Collection;
import java.util.Map;

public class Assert {
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new BusinessValidateException(message);
        }
    }
    
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BusinessValidateException(message);
        }
    }

    public static void notEmpty(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessValidateException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessValidateException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new BusinessValidateException(message);
        }
    }
}
