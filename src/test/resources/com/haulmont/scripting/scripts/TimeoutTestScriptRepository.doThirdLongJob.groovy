package com.haulmont.scripting.scripts

try {
    println("Sleeping for $timeMillis ms")
    Thread.sleep(Long.MAX_VALUE);
} finally {
    println("Finally block")
}