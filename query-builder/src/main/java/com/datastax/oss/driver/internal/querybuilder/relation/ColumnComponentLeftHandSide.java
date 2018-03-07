package com.datastax.oss.driver.internal.querybuilder.relation;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.relation.Term;

public class ColumnComponentLeftHandSide implements LeftHandSide {

  private final CqlIdentifier columnId;
  private final Term index;

  public ColumnComponentLeftHandSide(CqlIdentifier columnId, Term index) {
    this.columnId = columnId;
    this.index = index;
  }

  @Override
  public String asCql(boolean pretty) {
    return columnId.asCql(pretty) + "[" + index.asCql(pretty) + "]";
  }
}
