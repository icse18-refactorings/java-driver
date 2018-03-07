/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.api.querybuilder.relation;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultColumnComponentRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultColumnRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultTokenRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.RawRelation;
import com.google.common.collect.Iterables;
import java.util.Arrays;

/** A relation in a WHERE clause. */
public interface Relation {

  /**
   * Builds a relation testing a column.
   *
   * <p>This call must be chained with an operator, for example:
   *
   * <pre>{@code
   * selectFrom("foo").all().where(isColumn("k").eq(bindMarker()));
   * }</pre>
   */
  static ColumnRelationBuilder isColumn(CqlIdentifier id) {
    return new DefaultColumnRelationBuilder(id);
  }

  /** Shortcut for {@link #isColumn(CqlIdentifier) isColumn(CqlIdentifier.fromCql(name))} */
  static ColumnRelationBuilder isColumn(String name) {
    return isColumn(CqlIdentifier.fromCql(name));
  }

  /**
   * Builds a relation testing a component of a column, for example a field in a UDT, or an element
   * in a collection.
   */
  static ColumnComponentRelationBuilder isColumnComponent(CqlIdentifier columnId, Term index) {
    return new DefaultColumnComponentRelationBuilder(columnId, index);
  }

  /**
   * Shortcut for {@link #isColumnComponent(CqlIdentifier, Term)
   * isColumnComponent(CqlIdentifier.fromCql(columnName), index)}
   */
  static ColumnComponentRelationBuilder isColumnComponent(String columnName, Term index) {
    return isColumnComponent(CqlIdentifier.fromCql(columnName), index);
  }

  /** Builds a relation testing a token generated from a set of columns. */
  static TokenRelationBuilder isTokenFromIds(Iterable<CqlIdentifier> identifiers) {
    return new DefaultTokenRelationBuilder(identifiers);
  }

  /** Var-arg equivalent of {@link #isTokenFromIds(Iterable)}. */
  static TokenRelationBuilder isToken(CqlIdentifier... identifiers) {
    return isTokenFromIds(Arrays.asList(identifiers));
  }

  /**
   * Equivalent of {@link #isTokenFromIds(Iterable)} with raw strings; the names are converted with
   * {@link CqlIdentifier#fromCql(String)}.
   */
  static TokenRelationBuilder isToken(Iterable<String> names) {
    return isTokenFromIds(Iterables.transform(names, CqlIdentifier::fromCql));
  }

  /** Var-arg equivalent of {@link #isToken(Iterable)}. */
  static TokenRelationBuilder isToken(String... names) {
    return isToken(Arrays.asList(names));
  }

  // TODO add remaining selectors (decide how far we go without having to resort to isRaw())
  // TODO customIndexExpression (probably fine with isRaw)
  // TODO tupleOfIdentifiers (see 9th branch of relation in grammar)

  /**
   * Builds an arbitrary relation from a raw string.
   *
   * <p>The contents be appended to the query as-is, without any syntax checking or escaping. This
   * method should be used with caution, as it's possible to generate invalid CQL that will fail at
   * execution time; on the other hand, it can be used as an "escape hatch" to handle edge cases
   * that are not covered by the query builder.
   */
  static Relation isRaw(String raw) {
    return new RawRelation(raw);
  }

  String asCql(boolean pretty);
}
