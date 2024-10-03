package org.jlab.btm.persistence.projection;

import org.jlab.smoothness.persistence.enumeration.Hall;

public class HallPriority implements Comparable<HallPriority> {

  private final Hall hall;
  private final Integer priority;

  public HallPriority(Hall hall, Integer priority) {
    this.hall = hall;
    this.priority = priority;
  }

  @Override
  public int compareTo(HallPriority o) {
    return priority.compareTo(o.priority);
  }

  public Hall getHall() {
    return hall;
  }

  public Integer getPriority() {
    return priority;
  }
}
