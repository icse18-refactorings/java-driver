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
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import java.util.Arrays;

/**
 * A SELECT query that accepts additional selectors (that is, elements in the SELECT clause to
 * return as columns in the result set, as in: {@code SELECT count(*), sku, price...}).
 */
public interface CanAddSelector {

  /**
   * Adds a selector.
   *
   * <p>To create the argument, use one of the {@code getXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#getColumn(CqlIdentifier) getColumn}. This
   * type also provides shortcuts to create and add the selector in one call, for example {@link
   * #column(CqlIdentifier)} for {@code selector(getColumn(...))}.
   *
   * <p>If you add multiple selectors as once, consider {@link #selectors(Iterable)} as a more
   * efficient alternative.
   */
  Select selector(Selector selector);

  /**
   * Adds multiple selectors at once.
   *
   * <p>This is slightly more efficient than adding the selectors one by one (since the underlying
   * implementation of this object is immutable).
   *
   * <p>To create the arguments, use one of the {@code getXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#getColumn(CqlIdentifier) getColumn}.
   *
   * @throws IllegalArgumentException if one of the selectors is {@link QueryBuilderDsl#getAll()}
   *     ({@code *} can only be used on its own).
   * @see #selector(Selector)
   */
  Select selectors(Iterable<Selector> additionalSelectors);

  /** Var-arg equivalent of {@link #selectors(Iterable)}. */
  default Select selectors(Selector... additionalSelectors) {
    return selectors(Arrays.asList(additionalSelectors));
  }

  /**
   * Selects all columns, as in {@code SELECT *}.
   *
   * <p>This will clear any previously configured selector. Similarly, if any other selector is
   * added later, it will cancel this one.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getAll())}.
   *
   * @see QueryBuilderDsl#getAll()
   */
  default Select all() {
    return selector(QueryBuilderDsl.getAll());
  }

  /**
   * Selects the count of all returned rows, as in {@code SELECT count(*)}.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getCountAll())}.
   *
   * @see QueryBuilderDsl#getCountAll()
   */
  default Select countAll() {
    return selector(QueryBuilderDsl.getCountAll());
  }

  /**
   * Selects a particular column by its CQL identifier.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getColumn(columnId))}.
   *
   * @see QueryBuilderDsl#getColumn(CqlIdentifier)
   */
  default Select column(CqlIdentifier columnId) {
    return selector(QueryBuilderDsl.getColumn(columnId));
  }

  /** Shortcut for {@link #column(CqlIdentifier) column(CqlIdentifier.fromCql(columnName))} */
  default Select column(String columnName) {
    return column(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Selects the sum of two terms, as in {@code SELECT col1 + col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getSum(left,
   * right))}.
   *
   * @see QueryBuilderDsl#getSum(Selector, Selector)
   */
  default Select sum(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getSum(left, right));
  }

  /**
   * Selects the difference of two terms, as in {@code SELECT col1 - col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getDifference(left, right))}.
   *
   * @see QueryBuilderDsl#getDifference(Selector, Selector)
   */
  default Select difference(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getDifference(left, right));
  }

  /**
   * Selects the product of two terms, as in {@code SELECT col1 * col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getProduct(left,
   * right))}.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link QueryBuilderDsl#getSum}
   * or {@link QueryBuilderDsl#getDifference}. If they are raw selectors, you might have to
   * parenthesize them yourself.
   *
   * @see QueryBuilderDsl#getProduct(Selector, Selector)
   */
  default Select product(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getProduct(left, right));
  }

  /**
   * Selects the divider of two terms, as in {@code SELECT col1 / col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getDivider(left,
   * right))}.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link QueryBuilderDsl#getSum}
   * or {@link QueryBuilderDsl#getDifference}. If they are raw selectors, you might have to
   * parenthesize them yourself.
   *
   * @see QueryBuilderDsl#getDivider(Selector, Selector)
   */
  default Select divider(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getDivider(left, right));
  }

  /**
   * Selects the remainder of two terms, as in {@code SELECT col1 % col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getRemainder(left, right))}.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link QueryBuilderDsl#getSum}
   * or {@link QueryBuilderDsl#getDifference}. If they are raw selectors, you might have to
   * parenthesize them yourself.
   *
   * @see QueryBuilderDsl#getRemainder(Selector, Selector)
   */
  default Select remainder(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getRemainder(left, right));
  }

  /**
   * Selects the opposite of a term, as in {@code SELECT -col1}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getOpposite(argument))}.
   *
   * <p>The argument will be parenthesized if it is an instance of {@link QueryBuilderDsl#getSum} or
   * {@link QueryBuilderDsl#getDifference}. If it is a raw selector, you might have to parenthesize
   * it yourself.
   *
   * @see QueryBuilderDsl#getOpposite(Selector)
   */
  default Select opposite(Selector argument) {
    return selector(QueryBuilderDsl.getOpposite(argument));
  }

  /**
   * Selects a field inside of a UDT column, as in {@code SELECT user.name}.
   *
   * @see QueryBuilderDsl#getField(Selector, CqlIdentifier)
   */
  default Select field(Selector udt, CqlIdentifier fieldId) {
    return selector(QueryBuilderDsl.getField(udt, fieldId));
  }

  /**
   * Shortcut for {@link #field(Selector, CqlIdentifier) field(udt,
   * CqlIdentifier.fromCql(fieldName))}.
   */
  default Select field(Selector udt, String fieldName) {
    return field(udt, CqlIdentifier.fromCql(fieldName));
  }

  /**
   * Shortcut to select a UDT field when the UDT is a simple column (as opposed to a more complex
   * selection, like a nested UDT).
   *
   * @see QueryBuilderDsl#getField(CqlIdentifier, CqlIdentifier)
   */
  default Select field(CqlIdentifier udtColumnId, CqlIdentifier fieldId) {
    return selector(QueryBuilderDsl.getField(udtColumnId, fieldId));
  }

  /**
   * Shortcut for {@link #field(CqlIdentifier, CqlIdentifier)
   * field(CqlIdentifier.fromCql(udtColumnName), CqlIdentifier.fromCql(fieldName))}.
   */
  default Select field(String udtColumnName, String fieldName) {
    return selector(QueryBuilderDsl.getField(udtColumnName, fieldName));
  }

  /**
   * Selects an arbitrary expression expressed as a raw string.
   *
   * <p>The contents will be appended to the query as-is, without any syntax checking or escaping.
   * This method should be used with caution, as it's possible to generate invalid CQL that will
   * fail at execution time; on the other hand, it can be used as a workaround to handle new CQL
   * features that are not yet covered by the query builder.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getRaw(rawExpression))}.
   */
  default Select raw(String rawExpression) {
    return selector(QueryBuilderDsl.getRaw(rawExpression));
  }

  /**
   * Aliases the last added selector, as in {@code SELECT count(*) AS total}.
   *
   * <p>It is the caller's responsibility to ensure that this method is called at most once after
   * each selector, and that this selector can legally be aliased:
   *
   * <ul>
   *   <li>if it is called multiple times ({@code countAll().as("total1").as("total2")}), the last
   *       alias will override the previous ones.
   *   <li>if it is called before any selector was set, or after {@link #all()}, an {@link
   *       IllegalStateException} is thrown.
   *   <li>if it is called after a {@link #raw(String)} selector that already defines an alias, the
   *       query will fail at runtime.
   * </ul>
   */
  Select as(CqlIdentifier alias);

  /** Shortcut for {@link #as(CqlIdentifier) as(CqlIdentifier.fromCql(alias))} */
  default Select as(String alias) {
    return as(CqlIdentifier.fromCql(alias));
  }
}
