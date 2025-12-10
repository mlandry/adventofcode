# Current Approach vs Ray Casting

## Current Approach Issues

Your current approach:
1. Counts corners and edges separately in each direction (above, below, left, right)
2. Checks if `(corners/2 + edges) % 2 === 0` in ALL 4 directions
3. Requires all directions to have odd crossings

**Problems:**
- **Requires checking all 4 directions**: Ray casting only needs ONE direction
- **Complex corner/edge logic**: The division by 2 and separate counting is error-prone
- **Not standard ray casting**: Standard ray casting counts crossings along a single ray, not separate counts per direction
- **Edge case handling**: The logic for when corners count as 0.5 walls may not handle all polygon shapes correctly

## Standard Ray Casting Algorithm

**Basic principle:**
1. Cast a ray from the point in ONE direction (typically horizontal to the right)
2. Count how many times the ray crosses the polygon boundary
3. **Odd crossings = inside, Even crossings = outside**

**Key differences:**
- Only needs ONE direction check (not 4)
- Counts actual crossings along the ray, not separate corner/edge counts
- Handles corners correctly by tracking edge state (entering/exiting polygon)

## Implementation Comparison

### Your Current Approach
```typescript
// Checks all 4 directions separately
if ((cornersAbove / 2 + edgesAbove) % 2 === 0) return false;
if ((cornersBelow / 2 + edgesBelow) % 2 === 0) return false;
if ((cornersLeft / 2 + edgesLeft) % 2 === 0) return false;
if ((cornersRight / 2 + edgesRight) % 2 === 0) return false;
return true;
```

### Ray Casting Approach
```typescript
// Cast ray to the right, count crossings
let crossings = 0;
let inWall = false;
for (let x = p.x + 1; x <= maxX; x++) {
  const tile = getTile(x, p.y);
  if (inWall) {
    if (tile === "corner") {
      inWall = false;
      crossings++;
    }
  } else {
    if (tile === "corner") {
      inWall = true;
    } else if (tile === "edge") {
      crossings++;
    }
  }
}
return crossings % 2 === 1;
```

## Why Your Approach Might Fail

1. **Corner handling**: Your logic assumes corners always come in pairs, but at polygon boundaries this may not be true
2. **Direction independence**: Requiring all 4 directions to be odd is stricter than necessary - a point is inside if ANY ray has odd crossings
3. **Edge cases**: The division by 2 logic may fail when corners don't pair up correctly at polygon boundaries


