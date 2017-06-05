// $Id$

package icp.lib;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides task-local variables.
 *
 * It is largely derived from java.lang.ThreadLocal.
 */
public class TaskLocal<T> {

    /**
     * Returns the current tasks's "initial value" for this
     * task-local variable.  This method will be invoked the first
     * time a task accesses the variable with the {@link #get}
     * method, unless the task previously invoked the {@link #set}
     * method, in which case the {@code initialValue} method will not
     * be invoked for the task.  Normally, this method is invoked at
     * most once per task, but it may be invoked again in case of
     * subsequent invocations of {@link #remove} followed by {@link #get}.
     *
     * <p>This implementation simply returns {@code null}; if the
     * programmer desires task-local variables to have an initial
     * value other than {@code null}, {@code TaskLocal} must be
     * subclassed, and this method overridden. Typically, an
     * anonymous inner class will be used. In this case, initialValue
     * will need to be annotated with {@code @Shared("ThreadSafe")},
     * and registered with the {@code ThreadSafe} psudo-synchronizer.
     * If the task-local variable is a static member of a class,
     * registration with {@code ThreadSafe} must be done in a static
     * initializer block.
     *
     * @return the initial value for this task-local
     */
    protected T initialValue() {
        return null;
    }


    /**
     * Returns the value in the current task's copy of this
     * task-local variable.  If the variable has no value for the
     * current thread, it is first initialized to the value returned
     * by an invocation of the {@link #initialValue} method.
     *
     * @return the current task's value of this task-local
     */
    public T get() {
        Task t = Task.currentTask();
        TaskLocalMap map = getMap(t);
        if(map != null){
            Object value = map.get(this);
            if(value != null){
                @SuppressWarnings("unchecked")
                T toRtn = (T)value;
                return toRtn;
            }
        }
        return setInitialValue();
    }

    /**
     * Sets the current task's copy of this task-local variable
     * to the specified value.
     *
     * @param value the value to be stored in the current task's copy of
     *        this task-local.
     */
    public void set(T value) {
        Task t = Task.currentTask();
        TaskLocalMap map = getMap(t);
        if(map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }

    /**
     * Removes the current task's value for this task-local
     * variable.  If this task-local variable is subsequently
     * {@linkplain #get read} by the current task, its value will be
     * reinitialized by invoking its {@link #initialValue} method,
     * unless its value is {@linkplain #set set} by the current task
     * in the interim.  This may result in multiple invocations of the
     * {@code initialValue} method in the current task.
     *
     */
    public void remove() {
        Task t = Task.currentTask();
        TaskLocalMap map = getMap(t);
        if(map != null)
            map.remove(this);
    }

    /**
     * Variant of set() to establish initialValue. Used instead
     * of set() in case user has overridden the set() method.
     *
     * @return the initial value
     */
    private T setInitialValue(){
        T value = initialValue();
        Task t = Task.currentTask();
        TaskLocalMap map = getMap(t);
        if(map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }

    /**
     * Create the map associated with a TaskLocal.
     *
     * @param t the current task
     * @param firstValue value for the initial entry of the map
     */
    void createMap(Task t, T firstValue){
        t.taskLocals = new TaskLocalMap(this, firstValue);
    }

    /**
     * Get the map associated with a TaskLocal.
     *
     * @param  t the current task
     * @return the map
     */
    private TaskLocalMap getMap(Task t){
       return t.taskLocals;
    }

    /**
     * TaskLocalMap is a hash map for maintaining task local values.
     * No operations are exported outside of the TaskLocal class.
     * The class is package private to allow declaration of fields in class
     * TaskFactory.AbstractTask
     */
    static class TaskLocalMap{

        private Map<TaskLocal<?>, Object> map;

        TaskLocalMap(TaskLocal<?> k, Object v){
            map = new HashMap<>();
            map.put(k, v);
        }

        private Object get(TaskLocal<?> key) {
            return map.get(key);
        }

        private Object set(TaskLocal<?> key, Object value) {
            return map.put(key, value);
        }

        private Object remove(TaskLocal<?> key) {
            return map.remove(key);
        }
    }
}
