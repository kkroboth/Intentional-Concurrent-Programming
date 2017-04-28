# Running with sbt

```
[ICP] sbt
[info] ...
> test:runMain Test1
[info] Running Test1 
[class path: java.lang.Object.class:]
icp.lib.Thread@73a52957
[success] Total time: 0 s, completed Apr 28, 2017 9:28:53 AM
```

Note that `sbt 'test:runMain Test1'` also works but gives you a nasty warning (they're working on it).
