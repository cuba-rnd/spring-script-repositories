package com.haulmont.scripting.repository.provider;

/**
 * Wrapper for script source search result representation. Contains script text (if search succeeded), search status and search error (in any).
 */
public class ScriptSource {

   private final String source;

   private final SourceStatus status;

   private final Throwable error;

   public ScriptSource(String source, SourceStatus status, Throwable error) {
      this.source = source;
      this.status = status;
      this.error = error;
   }

   public String getSource() {
      return source;
   }

   public SourceStatus getStatus() {
      return status;
   }

   public Throwable getError() {
      return error;
   }
}
