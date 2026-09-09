package net.swofty.anticheat.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
// Marks a method as an anticheat event handler to be discovered and invoked by SwoftyEventHandler.
public @interface ListenerMethod {

}