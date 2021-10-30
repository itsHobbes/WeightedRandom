# WeightedRandom

A library for working with weighted randomness implementing Vose's Alias method.

## Examples

### Immutable Weighted Collection

A WeightedCollection does not allow you to modify the weights of the collection once it has been created.

#### Normalised

If you already have your item set probabilities normalised you can use the WeightedCollection constructor

```java
var items = Set.of("A", "B", "C", "D");
var probs = List.of(.5d, .25d, .125d, .125d);
var wc = new WeightedCollection<String>(items, probs);

int[] t = new int[4];
for (int i = 0; i < ITER; i++) {
    switch (wc.next()) {
        case "A" -> t[0]++;
        case "B" -> t[1]++;
        case "C" -> t[2]++;
        case "D" -> t[3]++;
    }
}
System.out.println(Arrays.toString(t));
```

Additionally you can create a collection from a Map.

```java
var items = new HashMap<String, Integer>();
items.put("A", .5d);
items.put("B", .25d);
items.put("C", .125d);
items.put("D", .125d);
var wc = new WeightedCollection<String>(items);

int[] t = new int[4];
for (int i = 0; i < ITER; i++) {
    switch (wc.next()) {
        case "A" -> t[0]++;
        case "B" -> t[1]++;
        case "C" -> t[2]++;
        case "D" -> t[3]++;
    }
}
System.out.println(Arrays.toString(t));
```

#### Unnormalised
If your values are unnormalised you can use the static factory methods to create an WeightedCollection from a Map.

You can do this from a Map containing Doubles or Integers.

```java
var items = new HashMap<String, Double>();
items.put("A", 50d);
items.put("B", 25d);
items.put("C", 12.5d);
items.put("D", 12.5d);
var wc = WeightedCollection.fromUnnormalisedDoubleProbability(items);

int[] t = new int[4];
for (int i = 0; i < ITER; i++) {
    switch (wc.next()) {
        case "A" -> t[0]++;
        case "B" -> t[1]++;
        case "C" -> t[2]++;
        case "D" -> t[3]++;
    }
}
System.out.println(Arrays.toString(t));
```

```java
var items = new HashMap<String, Integer>();
items.put("A", 100);
items.put("B", 50);
items.put("C", 25);
items.put("D", 25);
var wc = WeightedCollection.fromUnnormalisedIntegerProbability(items);

int[] t = new int[4];
for (int i = 0; i < ITER; i++) {
    switch (wc.next()) {
        case "A" -> t[0]++;
        case "B" -> t[1]++;
        case "C" -> t[2]++;
        case "D" -> t[3]++;
    }
}
System.out.println(Arrays.toString(t));
```