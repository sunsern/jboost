package jboost.exceptions;

/**
 * An exception that indicates a problem in instrumenting a ComplexLearner
 * 
 * @author Nigel Duffy
 */
public class InstrumentException extends Exception {

  public InstrumentException(String m) {
    message = m;
  }

  public String getMessage() {
    return (message);
  }

  private String message;
}
