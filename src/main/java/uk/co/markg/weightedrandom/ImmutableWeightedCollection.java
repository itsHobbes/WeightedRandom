package uk.co.markg.weightedrandom;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ImmutableWeightedCollection<T> {

  private final Map<Integer, T> map;
  private final int[] alias;
  private final double[] prob;

  public static <T> ImmutableWeightedCollection<T> fromUnnormalisedDoubleProbability(
      Map<T, Double> itemsWithProbability) {
    if (itemsWithProbability.isEmpty()) {
      throw new IllegalArgumentException("Items cannot be empty.");
    }

    double sum = itemsWithProbability.values().stream().collect(Collectors.summingDouble(d -> d));

    for (Entry<T, Double> item : itemsWithProbability.entrySet()) {
      item.setValue(item.getValue() / sum);
    }

    return new ImmutableWeightedCollection<T>(itemsWithProbability);
  }

  public static <T> ImmutableWeightedCollection<T> fromUnnormalisedIntegerProbability(
      Map<T, Integer> itemsWithProbability) {
    if (itemsWithProbability.isEmpty()) {
      throw new IllegalArgumentException("Items cannot be empty.");
    }

    long sum = itemsWithProbability.values().stream().collect(Collectors.summingInt(i -> i));

    Map<T, Double> items = new HashMap<>(itemsWithProbability.size());
    for (Entry<T, Integer> item : itemsWithProbability.entrySet()) {
      items.put(item.getKey(), (double) item.getValue() / sum);
    }

    return new ImmutableWeightedCollection<T>(items);
  }

  public ImmutableWeightedCollection(Map<T, Double> itemsWithProbability) {
    this(itemsWithProbability.keySet(), new ArrayList<Double>(itemsWithProbability.values()));
  }

  public ImmutableWeightedCollection(Set<T> items, List<Double> probabilities) {
    if (items.isEmpty() || probabilities.isEmpty()) {
      throw new IllegalArgumentException("Items cannot be empty.");
    }
    if (items.size() != probabilities.size()) {
      throw new IllegalArgumentException("Items and probabilities must be the same size");
    }
    double sum = probabilities.stream().collect(Collectors.summingDouble(d -> d));
    if (sum > 1.0d) {
      throw new IllegalArgumentException("Probabilities sum to greater than 1");
    }

    map = new HashMap<>(items.size());
    prob = new double[probabilities.size()];
    alias = new int[probabilities.size()];

    // probabilities are copied as the list will be modified during bucket build
    buildBuckets(new ArrayList<T>(items), new ArrayList<Double>(probabilities));
  }

  public T next() {
    int column = ThreadLocalRandom.current().nextInt(prob.length);
    boolean coinToss = ThreadLocalRandom.current().nextDouble() < prob[column];
    return map.get(coinToss ? column : alias[column]);
  }

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
