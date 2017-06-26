# Random notes (before I forget)

- Turned off instrumenting of `testng` and `sbt` classes as it led to errors (subtype relations broken, serialization issues).

- Test classes are instrumented.  This is a problem because the testing framework will call their methods from non-ICP threads.  With simple tests, the calling thread is _main_ and things miraculously work.  However, some features of TestNG (most notably parallel runs and timeouts) cannot be used because they involve calling methods from other threads.

- Lambdas don't seem to be instrumented, resulting in a rather mystifying NPE when trying to set their permission.

- Final fields can be read by other threads, even under a private permission.  Is that desirable?

- icp.core.ICPLoader.class produced a version of ICPLoader that is instrumented!  It is loaded by another ICPLoader class, which is not instrumented.

- main thread cannot instantiate class that extends abstract?

- why not edit abstract classes?

- need to purge / prune class pool?

- as currently set up, core classes are loaded by the default loader.  Any class they use is loaded by the same loader.  It is important that core classes don't use lib classes, or those won't be instrumented.

- it'd be nice to write tests in an icp.core package so they can access private classes/methods, but it doesn't work because classes from the icp.core package---including test classes---are loaded by the system loader.

- bug caught: added an owner field to the reentrant lock, which was frozen.

- bug caught: was resetting owner of reentrant lock to null *after* the internal `unlock`, which led to a race in the Pi program.

- bug caught: created a thread from a private, non-lambda runnable: call to run fails.

- conceptually, a task could acquire a lock during a first run and release it in a second run (no good reason to do that, though).  The current implementation won't work if the thread has changed between runs because the underlying `java.util.concurrent` lock checks that *threads* own the lock they release.

- core classes could be instrumented by adding a permission field explicitly in the source.

- classes with no permission field are permanently thread-safe.  This is implemented by producing a permission out of thin air when no permission field exists.

- if class `B` extends class `A` and an instance of `B` is created, class `B` starts to load and is edited to add the permission field.  This loading triggers the loading of `A`, which is also edited to add the permisson field.  The object ends up with two permission fields, one of which is never used.

    If, on the other hand, class `A` is loaded first (e.g., by creating an instance of `A` first), no field is added to class `B` because the inherite field is detected.  However, inehrited fields are not searched when looking for permissions, so the permission on a `B` instance is never found.

- we might be better off *without* a thread class.

- is `x` is an instrumented object but the read (or write) `x.field` appears in a non-instrumented class, it is not being checked.