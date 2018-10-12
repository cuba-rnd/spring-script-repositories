package com.haulmont.scripting.repository.provider;

public class ScriptSource {

   private String source;

   private SourceStatus status;

   private Throwable error;

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
