package uk.co.markg.weightedrandom;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MutableWeightedCollection<T> {

  private final Map<Integer, T> map;
  private final Map<T, Double> originalProbabilities;
  private final int[] alias;
  private final double[] prob;

  /**
   * Static factory method to create a new {@link MutableWeightedCollection} from a {@link Map} of
   * unnormalised {@link Double} probabilities
   * 
   * @param <T> The type of the elements that the probability matches to
   * @param itemsWithProbability The map of items and their unnormalised probabilities
   * @return An {@link MutableWeightedCollection} representing the items and their probabilities
   */
  public static <T> MutableWeightedCollection<T> fromUnnormalisedDoubleProbability(
      Map<T, Double> itemsWithProbability) {
    if (itemsWithProbability.isEmpty()) {
      throw new IllegalArgumentException("Items cannot be empty.");
    }

    double sum = itemsWithProbability.values().stream().collect(Collectors.summingDouble(d -> d));

    for (Entry<T, Double> item : itemsWithProbability.entrySet()) {
      item.setValue(item.getValue() / sum);
    }

    return new MutableWeightedCollection<T>(itemsWithProbability);
  }

  /**
   * Static factory method to create a new {@link MutableWeightedCollection} from a {@link Map} of
   * unnormalised {@link Integer} probabilities
   * 
   * @param <T> The type of the elements that the probability matches to
   * @param itemsWithProbability The map of items and their unnormalised probabilities
   * @return An {@link MutableWeightedCollection} representing the items and their probabilities
   */
  public static <T> MutableWeightedCollection<T> fromUnnormalisedIntegerProbability(
      Map<T, Integer> itemsWithProbability) {
    if (itemsWithProbability.isEmpty()) {
      throw new IllegalArgumentException("Items cannot be empty.");
    }

    long sum = itemsWithProbability.values().stream().collect(Collectors.summingInt(i -> i));

    Map<T, Double> items = new HashMap<>(itemsWithProbability.size());
    for (Entry<T, Integer> item : itemsWithProbability.entrySet()) {
      items.put(item.getKey(), (double) item.getValue() / sum);
    }

    return new MutableWeightedCollection<T>(items);
  }

  /**
   * Creates a new {@link MutableWeightedCollection} from a {@link Map} of items with normalised
   * probabilities
   * 
   * @param itemsWithProbability The {@link Map} of items with normalised probabilities
   */
  public MutableWeightedCollection(Map<T, Double> itemsWithProbability) {
    this(new ArrayList<T>(itemsWithProbability.keySet()),
        new ArrayList<Double>(itemsWithProbability.values()));
  }

  /**
   * Creates a new {@link MutableWeightedCollection} from a {@link List} of items and {@link List}
   * of probabilities. Note that the order of the items in the List should match the order of the
   * probabilities
   * 
   * @param items The {@link List} of items
   * @param probabilities The {@link List} of probabilities
   */
  public MutableWeightedCollection(List<T> items, List<Double> probabilities) {
    if (items.isEmpty() || probabilities.isEmpty()) {
      throw new IllegalArgumentException("Items cannot be empty.");
    }
    if (items.size() != probabilities.size()) {
      throw new IllegalArgumentException("Items and probabilities must be the same size");
    }
    double sum = probabilities.stream().mapToDouble(i -> i).sum();
    if (sum > 1.0d) {
      throw new IllegalArgumentException("Probabilities sum to greater than 1");
    }
    originalProbabilities = new HashMap<>(items.size());
    for (int i = 0; i < items.size(); i++) {
      originalProbabilities.put(items.get(i), probabilities.get(i));
    }

    map = new HashMap<>(items.size());
    prob = new double[probabilities.size()];
    alias = new int[probabilities.size()];

    // probabilities are copied as the list will be modified during bucket build
    buildBuckets(new ArrayList<T>(items), new ArrayList<Double>(probabilities));
  }

  /**
   * Generate a new random number and return it
   * 
   * @return the random number
   */
  public T next() {
    int column = ThreadLocalRandom.current().nextInt(prob.length);
    boolean coinToss = ThreadLocalRandom.current().nextDouble() < prob[column];
    return map.get(coinToss ? column : alias[column]);
  }

  public void set(T item, double newProbability) {
    originalProbabilities.put(item, newProbability);
    buildBuckets(new ArrayList<T>(originalProbabilities.keySet()),
        new ArrayList<Double>(originalProbabilities.values()));
  }

  public void setAll(Map<T, Double> itemsWithProbability) {
    for (Entry<T, Double> e : itemsWithProbability.entrySet()) {
      originalProbabilities.put(e.getKey(), e.getValue());
    }
    buildBuckets(new ArrayList<T>(originalProbabilities.keySet()),
        new ArrayList<Double>(originalProbabilities.values()));
  }

  /**
   * Implementation of Vose's Alias Method
   * 
   * @param items The {@link List} of items
   * @param probabilities The {@link List} of probabilities
   */
  private void buildBuckets(List<T> items, List<Double> probabilities) {
    double average = 1.0 / probabilities.size();

    var small = new ArrayDeque<Integer>();
    var large = new ArrayDeque<Integer>();

    for (int i = 0; i < probabilities.size(); ++i) {
      map.put(i, items.get(i));
      if (probabilities.get(i) >= average) {
        large.add(i);
      } else {
        small.add(i);
      }
    }

    while (!small.isEmpty() && !large.isEmpty()) {
      int l = small.removeLast();
      int g = large.removeLast();

      prob[l] = probabilities.get(l) * probabilities.size();
      alias[l] = g;

      probabilities.set(g, (probabilities.get(g) + probabilities.get(l)) - average);

      if (probabilities.get(g) >= 1.0 / probabilities.size()) {
        large.add(g);
      } else {
        small.add(g);
      }
    }

    while (!small.isEmpty()) {
      prob[small.removeLast()] = 1.0;
    }
    while (!large.isEmpty()) {
      prob[large.removeLast()] = 1.0;
    }
  }
}
