The cube is stored as a 1-dimensional array, NxNxN in length. The data for the cube on
face `F`, with coordinates `X` and `Y`, is at `F.ordinal + X * FACES + Y * FACES * SIZE`.

But how do the coordinates map to the cube? I used the standard "+" cube unwrapping, giving
the position `0, 0` on a face where the `*` is on the following diagram:

``` 
   *.. 
   .U.
   ...
*..*..*..
.L..F..R.
.........
   *..
   .D.
   ...
   *..
   .B.
   ...
```

As you can see, the origin point is prescribed as the point closest to the global origin on the
unwrapped cube. This means that the order of faces should be `up, left, front, right, down, back`,
to maintain this same order in memory.
