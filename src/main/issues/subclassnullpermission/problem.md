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

