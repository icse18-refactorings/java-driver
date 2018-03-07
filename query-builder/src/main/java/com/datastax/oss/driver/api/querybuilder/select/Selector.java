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
package com.datastax.oss.driver.api.querybuilder.select;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.internal.querybuilder.select.AllSelector;
import com.datastax.oss.driver.internal.querybuilder.select.ColumnSelector;
import com.datastax.oss.driver.internal.querybuilder.select.CountAllSelector;
import com.datastax.oss.driver.internal.querybuilder.select.RawSelector;

/** A "column" in a SELECT query. */
public interface Selector {

  /** Selects all columns, as in {@code SELECT *}. */
  static Selector all() {
    return AllSelector.INSTANCE;
  }

  /** Selects the count of all returned rows, as in {@code SELECT count(*)}. */
  static Selector countAll() {
    return new CountAllSelector();
  }

  /** Selects a particular column by its CQL identifier. */
  static Selector column(CqlIdentifier columnId) {
    return new ColumnSelector(columnId);
  }

  /** Shortcut for {@link #column(CqlIdentifier) column(CqlIdentifier.fromCql(columnName))} */
  static Selector column(String columnName) {
    return column(CqlIdentifier.fromCql(columnName));
  }

  // TODO add remaining selectors (decide how far we go without having to resort to raw())
  // TODO arithmetic expressions (selectionAddition, selectionMultiplication)
  // TODO UDT fields (selectionGroupWithField)
  // TODO collection elements (selectionList, selectionMapOrSet)
  // TODO collection sub-ranges (collectionSubSelection)
  // TODO casting (selectionTypeHint)
  // TODO tuples (selectionTupleOrNestedSelector)
  // TODO function calls (selectionFunction)
  // TODO literals (can probably skip that one, edge-case + easily covered with raw())

  /**
   * Selects an arbitrary expression expressed as a raw string.
   *
   * <p>The contents be appended to the query as-is, without any syntax checking or escaping. This
   * method should be used with caution, as it's possible to generate invalid CQL that will fail at
   * execution time; on the other hand, it can be used as an "escape hatch" to handle edge cases
   * that are not covered by the query builder.
   */
  static Selector raw(String rawExpression) {
    return new RawSelector(rawExpression);
  }

  /** Aliases the selector, as in {@code SELECT count(*) AS total}. */
  Selector as(CqlIdentifier alias);

  /** Shortcut for {@link #as(CqlIdentifier) as(CqlIdentifier.fromCql(alias))} */
  default Selector as(String alias) {
    return as(CqlIdentifier.fromCql(alias));
  }

  String asCql(boolean pretty);
}
