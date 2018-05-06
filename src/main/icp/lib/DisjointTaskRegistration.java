package icp.lib;

import icp.core.IntentError;
import icp.core.TaskLocal;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Allows disjoint tasks to be registered for a task-based intent specified by the
 * constructor. Tasks cannot be re-registered on the same intent or another one.
 */
public class DisjointTaskRegistration {
  private final String[] intents;
  private final TaskLocal<String> registeredTasks = Utils.newTaskLocal(null);

  /**
   * List of concrete intent string that tasks must register to.
   *
   * @param firstIntent required at least one intent
   * @param moreIntents varargs of additional intents
   */
  protected DisjointTaskRegistration(String firstIntent, String... moreIntents) {
    Objects.requireNonNull(firstIntent);
    if (moreIntents == null) {
      intents = new String[]{firstIntent};
    } else {
      intents = new String[moreIntents.length + 1];
      intents[0] = firstIntent;
      System.arraycopy(moreIntents, 0, intents, 1, moreIntents.length);
    }
  }

  /**
   * Assert intent is in the list of register intents.
   *
   * @param intent intent ot check
   * @throws IntentError if intent is not in the list
   */
  private void isIntent(String intent) {
    if (Stream.of(intents).noneMatch(i -> i.equals(intent))) {
      throw new IntentError("'" + intent + "'" + " is not a registered intent for synchronizer");
    }
  }

  /**
   * Register current task's intent as {@code intent} type.
   * Current task must not have previous intent and cannot re-register to
   * the same intent.
   *
   * @param intent intent to register current task to
   * @throws IntentError if task could not be registered to intent
   */
  protected final void registerTaskIntent(String intent) {
    isIntent(intent);
    String prevIntent = registeredTasks.get();
    if (prevIntent != null) {
      if (prevIntent.equals(intent))
        throw new IntentError("Cannot re-register same task");
      else
        throw new IntentError("Task must not be registered with another intent");
    }

    registeredTasks.set(intent);
  }

  /**
   * Check if the current task is of {@code intent} type or throw
   * an intent error.
   *
   * @param intent intent to check on task
   * @throws IntentError thrown if task not registered as {@code intent}
   */
  protected final void checkTaskRegistered(String intent) {
    if (!isTaskRegistered(intent))
      throw new IntentError("Task not registered as " + intent);
  }

  /**
   * Return true if task is registered as {@code intent}.
   *
   * @param intent intent to check current task against
   * @return true if task is registered with intent
   * @throws IntentError if task does not have an intent
   */
  protected final boolean isTaskRegistered(String intent) {
    String taskIntent = registeredTasks.get();
    if (taskIntent == null)
      throw new IntentError("Task has no registered intent");
    return taskIntent.equals(intent);
  }

}
