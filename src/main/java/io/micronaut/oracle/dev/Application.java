package io.micronaut.oracle.dev;

import io.micronaut.runtime.Micronaut;

public final class Application {

  private Application() {
  }

  public static void main(String[] args) {
    Micronaut.run(Application.class, args);
  }
}
