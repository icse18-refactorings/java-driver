package com.datastax.oss.driver.internal.querybuilder.select;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.select.Selector;

public enum AllSelector implements Selector {
  INSTANCE;

  @Override
  public Selector as(CqlIdentifier alias) {
    throw new IllegalStateException("Can't alias the '*' selector");
  }

  @Override
  public String asCql(boolean pretty) {
    return "*";
  }
}
