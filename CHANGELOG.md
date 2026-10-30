# Change Log

## 2.4

Minor release of GDSC Core.

### Add

- No changes yet.

### Update

- No changes yet.

### Fix

- `AlignImagesFft.createBounds` includes the maximum X and Y shift in the bounds.
  Previously the maximum shift was excluded from the alignment search, and a range
  with equal minimum and maximum shift did not search any translation.
- `AlignImagesFft.createHalfMaxBounds` creates symmetric bounds that include the
  maximum positive translation. Previously the positive translation range was one
  pixel shorter than the negative range. This bounds the default alignment search.

## Earlier Versions

Changes in earlier versions are described by the
[git history](https://github.com/aherbert/gdsc-core/commits/master) of each
[release tag](https://github.com/aherbert/gdsc-core/tags).
