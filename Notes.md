# Random notes (before I forget)

- Lambdas don't seem to be instrumented, resulting in a rather mystifying NPE when trying to set their permission.

- Final fields can be read by other threads, even under a private permission.  Is that desirable?