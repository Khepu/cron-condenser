# Cron Condenser

This tool aims to to reduce a list of cron expressions down to the minimum
expressions needed to describe the same intervals as the original list.

## When can 2 cron expressions?

Two cron expressions can only be merged when they differ in only one segment.
For example:

```
0 0 0 JAN *
1 0 0 JAN *
```

can be merged into `1,2 0 0 JAN *` since they only differ in the `minute` segment while

```
0 0 0 JAN *
30 10 0 JAN *
```

cannot be merged. Merging them would introduce **2** new trigger times that were
not present in any of the original expressions. `0,30 0,10 0 JAN *` would
include also trigger at `0 10 0 JAN *` as well as `30 0 0 JAN *`. This does raise
questions such as when `n` crons can be merged but this area is still unexplored.
