package com.datastax.oss.driver.internal.querybuilder.select;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import java.util.Objects;

public class CountAllSelector implements Selector {

  private final CqlIdentifier alias;

  public CountAllSelector() {
    this(null);
  }

  public CountAllSelector(CqlIdentifier alias) {
    this.alias = alias;
  }

  @Override
  public Selector as(CqlIdentifier alias) {
    return new CountAllSelector(alias);
  }

  @Override
  public String asCql(boolean pretty) {
    return (alias == null) ? "count(*)" : "count(*) AS " + alias.asCql(pretty);
  }

  public CqlIdentifier getAlias() {
    return alias;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof CountAllSelector) {
      CountAllSelector that = (CountAllSelector) other;
      return Objects.equals(this.alias, that.alias);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return (alias == null) ? 0 : alias.hashCode();
  }
}
