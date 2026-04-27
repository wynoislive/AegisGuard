package com.aegisguard.checks;

import java.lang.annotation.*;

/**
 * Annotation for check metadata — applied to Check implementations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckInfo {
    String name();
    CheckCategory category();
    String configName();
}
