package com.terheyden.event;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Declares the following defaults for the entire package:
 * Methods don't return null.
 * Method params are not null.
 * Fields are never set to null.
 * <p>
 * Local vars can be null, sure, that's internal biz.
 * <p>
 * This code was copied and modified from {@link ParametersAreNonnullByDefault}.
 * <p>
 * See also:
 * package-info.java
 * https://stackoverflow.com/questions/13310994/set-findbugs-notnull-as-default-for-all-classes-under-a-package
 * https://stackoverflow.com/questions/3550292/what-do-java-annotation-elementtype-constants-mean
 */
@Documented
@Nonnull
@Retention(RetentionPolicy.RUNTIME)
public @interface NonnullByDefault {
    // Empty.
}
