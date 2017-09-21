# Variant 1
When the child is loaded first, the child object contains
two permission fields:

```
icp$42$permissionField = null
ParentExample.icp$42$permissionField = {Permissions$4@966}
```

Two permissions are added (One has ParentExample prefix idk why) because:
1) Child class is loaded
    1) Permission field added
2) Parent class is loaded
    1) Another permission field is added (has ParentExample prefix)
    

What Works:
1) Parent class is loaded
    1) Permission field added
2) Child class is loaded
    1) Same permission field is loaded (from superclass)
    2) Do nothing

# Variant 2 (due to fixing variant 1)
When using the @External annotation on a parent class, and a normal class
(without @External), the child continues up super type chain until it 
reaches `java.lang.Object`. Setting the permission on a null permission field
results in default permanently-thread-safe and you can never reset those.
