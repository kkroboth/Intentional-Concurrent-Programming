// $Id$

package icp.core;

/**
 * Exception to be thrown when a permission violation occurs.
 */
final public class IntentError extends RuntimeException
{
  // Runtime Exception is serializable
  private static final long serialVersionUID = 1;

  public IntentError()
  {
  }

  public IntentError(String msg)
  {
    super(msg);
  }
}

