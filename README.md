# Running tests with sbt

## Tests as main programs

```
[ICP] sbt
[info] ...
> test:runMain Test1
[info] Running Test1 
[class path: java.lang.Object.class:]
icp.lib.Thread@73a52957
[success] Total time: 0 s, completed ...
```

Note that `sbt 'test:runMain Test1'` also works but gives you a nasty warning (they're working on it).

## JUnit tests

From the `sbt` shell, one can also run JUnit tests:

```
> test
[error] Test DemoSuite.aTimeoutTest failed: java.lang.Exception: test timed out after 500 milliseconds, took 0.502 sec
[error]     at java.lang.Object.wait(Native Method)
[error]     at java.lang.Object.wait(Object.java:502)
[error]     at DemoSuite.aTimeoutTest(DemoSuite.java:28)
[error]     ...
[error] Test DemoSuite.aFailedTest failed: expected same:<400> was not:<400>, took 0.0 sec
[error] Failed: Total 4, Failed 2, Errors 0, Passed 2
[error] Failed tests:
[error] 	DemoSuite
[error] (test:test) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 1 s, completed ...
```

The following commands can be used:

  - `test`: runs all the unit tests
  - `testOnly`: runs only some test classes; wildcards possible, e.g., `testOnly Demo*`
  - `testOnly` with options: can be used to run only some methods, e.g., `testOnly DemoSuite -- --tests=.*SuccessfulTest`
  
    Note that method selection uses regular expressions, not wildcards.
