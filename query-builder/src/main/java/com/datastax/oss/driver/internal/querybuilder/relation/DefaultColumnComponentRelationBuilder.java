package com.datastax.oss.driver.internal.querybuilder.relation;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.relation.ColumnComponentRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.relation.Term;

public class DefaultColumnComponentRelationBuilder implements ColumnComponentRelationBuilder {

  private final CqlIdentifier columnId;
  private final Term index;

  public DefaultColumnComponentRelationBuilder(CqlIdentifier columnId, Term index) {
    this.columnId = columnId;
    this.index = index;
  }

  @Override
  public Relation build(String operator, Term rightHandSide) {
    return new DefaultRelation(new ColumnComponentLeftHandSide(columnId, index), operator, rightHandSide);
  }
}
