package com.haulmont.scripting.repository.evaluator;

/**
 * Interface to mark classes cancellable - the method is be called on
 * exception during execution. You may use it to close DB connections,
 * sockets, streams etc.
 * It is stronly advised to publish timeout aware bean as a prototype bean to avoid issues
 * with multi-threading in singleton beans.
 */
public interface TimeoutAware {

    /**
     * This method is called to interrupt script evaluation in case of exception.
     */
    void cancel();

}
