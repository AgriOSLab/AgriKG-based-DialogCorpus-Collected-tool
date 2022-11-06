package com.lychee.dialog.Neo4jPack.Annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Select {
    String value();
}
