# HTTP SERVER APPLICATION

Goal: Avoid java.util.concurrent classes!!

## ENGINE
Using ICP, it wants full control over threads and all 3rd party http libs
use their own threading. Therefore, a custom *simple* HTTP server was created.

All persistent data is updated either explicitly or when the server is shutting down.
Ideally, all data should be in a SQL database, but we want to test ICP and use our own
data structures. To keep it simple, when data is updated, it is not immediately saved to disk,
but stored in a data structure until shutdown. The data structures are a 1-1 model of saved
data on disk.

## Frontend