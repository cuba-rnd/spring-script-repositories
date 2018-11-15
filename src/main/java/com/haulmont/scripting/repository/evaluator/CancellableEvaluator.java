package com.haulmont.scripting.repository.evaluator;

import org.springframework.scripting.ScriptEvaluator;

public interface CancellableEvaluator extends ScriptEvaluator {

    void cancel();

}
