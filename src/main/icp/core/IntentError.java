// $Id$

package icp.core;

/**
 * Exception to be thrown when a permission violation occurs.
 */
final public class IntentError extends RuntimeException
{
  public IntentError()
  {
  }

  public IntentError(String msg)
  {
    super(msg);
  }
}

