package uk.co.markg.weightedrandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class ImmutableWeightedCollectionTest {

  private static final int ITER = 1_000_000;

  @Test
  public void probabilitiesHigherThanOne() {
    var items = List.of("A", "B", "C", "D");
    var probs = List.of(.5d, .33333d, .083333d, .183333d);
    var exception = assertThrows(IllegalArgumentException.class,
        () -> new ImmutableWeightedCollection<String>(items, probs));
    assertEquals("Probabilities sum to greater than 1", exception.getMessage());
  }

  @Test
  public void mismatchItemAndProbabilitySizes() {
    var items = List.of("A", "B", "C", "D", "E");
    var probs = List.of(.5d, .33333d, .083333d, .083333d);
    var exception = assertThrows(IllegalArgumentException.class,
        () -> new ImmutableWeightedCollection<String>(items, probs));
    assertEquals("Items and probabilities must be the same size", exception.getMessage());
  }

  @Test
  public void newInstanceEmptyItemList() {
    var items = new ArrayList<String>();
    var probs = List.of(.5d, .25d, .125d, .125d);
    var exception = assertThrows(IllegalArgumentException.class,
        () -> new ImmutableWeightedCollection<String>(items, probs));
    assertEquals("Items cannot be empty.", exception.getMessage());
  }

  @Test
  public void newInstanceEmptyProbsList() {
    var items = List.of("A", "B", "C", "D");
    var probs = new ArrayList<Double>();
    var exception = assertThrows(IllegalArgumentException.class,
        () -> new ImmutableWeightedCollection<String>(items, probs));
    assertEquals("Items cannot be empty.", exception.getMessage());
  }

  @Test
  public void fromUnnormalisedDoubleEmptyMap() {
    var items = new HashMap<String, Double>();
    var exception = assertThrows(IllegalArgumentException.class,
        () -> ImmutableWeightedCollection.fromUnnormalisedDoubleProbability(items));
    assertEquals("Items cannot be empty.", exception.getMessage());
  }

  @Test
  public void fromUnnormalisedDoubleProbability() {
    var items = new HashMap<String, Double>();
    items.put("A", 50d);
    items.put("B", 25d);
    items.put("C", 12.5d);
    items.put("D", 12.5d);
    var wc = ImmutableWeightedCollection.fromUnnormalisedDoubleProbability(items);

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
    assertEquals(ITER, IntStream.of(t).sum());
  }

  @Test
  public void fromUnnormalisedIntegerProbability() {
    var items = new HashMap<String, Integer>();
    items.put("A", 100);
    items.put("B", 50);
    items.put("C", 25);
    items.put("D", 25);
    var wc = ImmutableWeightedCollection.fromUnnormalisedIntegerProbability(items);

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
    assertEquals(ITER, IntStream.of(t).sum());
  }

  @Test
  public void test() {
    var items = List.of("A", "B", "C", "D");
    var probs = List.of(.5d, .25d, .125d, .125d);
    var wc = new ImmutableWeightedCollection<String>(items, probs);

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
    assertEquals(ITER, IntStream.of(t).sum());
  }

}
