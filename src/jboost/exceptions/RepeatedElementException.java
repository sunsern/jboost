package jboost.exceptions;

public class RepeatedElementException extends Exception {

  public RepeatedElementException(String message) {
    this.message = message;
  }

  public String getMessage() {
    return (message);
  }

  private String message;
}
