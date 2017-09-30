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

## Actual Problem
At the end of the day, we will still have the possibility of classes contains multiple permission fields.
This is due to editing a child class before its parent. The problem should not be fixed in 
editing, but how permission fields are accessed (ICP.setPermission).

Consider the following case:

// TODO: Add code to make this clearer
A parent class with a constructor that sets permission to `this`. The child extends parent. When
the class is instantiated, the child's <init> is called, then the parents <init>, and the null
permission injected will be set. The next line in the parent's constructor sets the permission
on `this`, but it is looking at the child's instance permission field that has not been set.

# Fix
Keep editing classes functionality the same. When getPermission() is called, it will retrieve
the latest permission field up the inheritance chain that exists.
