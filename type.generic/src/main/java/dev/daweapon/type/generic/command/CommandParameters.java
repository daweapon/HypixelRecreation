package dev.daweapon.type.generic.command;

import dev.daweapon.type.generic.user.categories.Rank;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
// Annotation declaring a HypixelCommand's labels, permission, usage, and console-execution eligibility.
public @interface CommandParameters {
    String description() default "";

    String labels() default "";

    String usage();

    Rank permission();

    boolean allowsConsole();
}
