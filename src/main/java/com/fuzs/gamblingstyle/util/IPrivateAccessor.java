/*
** 2016 Juni 19
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package com.fuzs.gamblingstyle.util;

import static com.fuzs.gamblingstyle.GamblingStyle.LOGGER;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface IPrivateAccessor {
    
    /**
     * Invokes a method that may or may not exist
     * 
     * @param objClass the instance class
     * @param instance the instance object for the method - ignored if static method
     * @param method the name of the method
     * @param logError whether to log errors - should be false if invoking methods without knowing if the method exists
     * @param argClasses an array of the argument classes
     * @param args a varargs of the arguments passed to the method
     * @return the object returned by the method, if applicable
     */
    default Object invoke(Class<?> objClass, Object instance, String method, boolean logError, Class<?>[] argClasses, Object... args) {
        try {
            objClass.getMethod(method, argClasses).setAccessible(true);
            return objClass.getMethod(method, argClasses).invoke(instance, args);
        } catch (Exception e) {
            if (logError) LOGGER.error("method " + method + " of class " + objClass.getName() + " throws error", e);
            return null;
        }
    }

    /**
     * Get a field that may or may not exist
     * 
     * @param <T> the type of the field to be returned
     * @param objClass the instance class
     * @param instance the instance of the field - ignored if static field
     * @param field the name of the field
     * @param defaultValue the default value to return if the field does not exist - this also determines the field type, so cast your nulls!
     * @param logError whether to log errors - should be false if getting fields without knowing if the field exists
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> T getField(Class<?> objClass, Object instance, String field, T defaultValue, boolean logError) {
        try {
            objClass.getField(field).setAccessible(true);
            return (T) objClass.getField(field).get(instance);
        } catch (Exception e) {
            if (logError) LOGGER.error("field " + field + " of class " + objClass.getName() + " throws error", e);
            return defaultValue;
        }
    }

    /**
     * Set a field that may or may not exist
     * 
     * @param objClass the instance class
     * @param instance the instance of the field - ignored if static field
     * @param field the name of the field
     * @param setValue the value to set the field to
     * @param logError whether to log errors - should be false if setting fields without knowing if the field exists
     */
    default void setField(Class<?> objClass, Object instance, String field, Object setValue, boolean logError) {
        try {
            objClass.getField(field).setAccessible(true);
            objClass.getField(field).set(instance, setValue);
        } catch (Exception e) {
            if (logError) LOGGER.error("field " + field + " of class " + objClass.getName() + " throws error", e);
        }
    }
}
