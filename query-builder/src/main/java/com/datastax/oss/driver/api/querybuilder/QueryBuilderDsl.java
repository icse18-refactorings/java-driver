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
package com.datastax.oss.driver.api.querybuilder;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.relation.ColumnComponentRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.ColumnRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.relation.Term;
import com.datastax.oss.driver.api.querybuilder.relation.TokenRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.TupleRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.select.SelectFrom;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.datastax.oss.driver.internal.querybuilder.relation.CustomIndexRelation;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultColumnComponentRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultColumnRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultTokenRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultTupleRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.RawRelation;
import com.datastax.oss.driver.internal.querybuilder.relation.RawTerm;
import com.datastax.oss.driver.internal.querybuilder.relation.TupleTerm;
import com.datastax.oss.driver.internal.querybuilder.select.AllSelector;
import com.datastax.oss.driver.internal.querybuilder.select.ColumnSelector;
import com.datastax.oss.driver.internal.querybuilder.select.CountAllSelector;
import com.datastax.oss.driver.internal.querybuilder.select.DefaultBindMarker;
import com.datastax.oss.driver.internal.querybuilder.select.DefaultSelect;
import com.datastax.oss.driver.internal.querybuilder.select.RawSelector;
import com.google.common.collect.Iterables;
import java.util.Arrays;

/** A Domain-Specific Language to build CQL queries using Java code. */
public interface QueryBuilderDsl {

  /** Starts a SELECT query for a qualified table. */
  static SelectFrom selectFrom(CqlIdentifier keyspace, CqlIdentifier table) {
    return new DefaultSelect(keyspace, table);
  }

  /**
   * Shortcut for {@link #selectFrom(CqlIdentifier, CqlIdentifier)
   * selectFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table))}
   */
  static SelectFrom selectFrom(String keyspace, String table) {
    return selectFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table));
  }

  /** Starts a SELECT query for an unqualified table. */
  static SelectFrom selectFrom(CqlIdentifier table) {
    return selectFrom(null, table);
  }

  /** Shortcut for {@link #selectFrom(CqlIdentifier) selectFrom(CqlIdentifier.fromCql(table))} */
  static SelectFrom selectFrom(String table) {
    return selectFrom(CqlIdentifier.fromCql(table));
  }

  /** Selects all columns, as in {@code SELECT *}. */
  static Selector getAll() {
    return AllSelector.INSTANCE;
  }

  /** Selects the count of all returned rows, as in {@code SELECT count(*)}. */
  static Selector getCountAll() {
    return new CountAllSelector();
  }

  /** Selects a particular column by its CQL identifier. */
  static Selector getColumn(CqlIdentifier columnId) {
    return new ColumnSelector(columnId);
  }

  /**
   * Shortcut for {@link QueryBuilderDsl#getColumn(CqlIdentifier)
   * getColumn(CqlIdentifier.fromCql(columnName))}
   */
  static Selector getColumn(String columnName) {
    return getColumn(CqlIdentifier.fromCql(columnName));
  }

  // TODO add remaining selectors (decide how far we go without having to resort to getRaw)
  // TODO arithmetic expressions (selectionAddition, selectionMultiplication)
  // TODO UDT fields (selectionGroupWithField)
  // TODO collection elements (selectionList, selectionMapOrSet)
  // TODO collection sub-ranges (collectionSubSelection)
  // TODO casting (selectionTypeHint)
  // TODO tuples (selectionTupleOrNestedSelector)
  // TODO function calls (selectionFunction)
  // TODO literals (can probably skip that one, edge-case + easily covered with raw())

  /**
   * Selects an arbitrary expression expressed as a getRaw string.
   *
   * <p>The contents will be appended to the query as-is, without any syntax checking or escaping.
   * This method should be used with caution, as it's possible to generate invalid CQL that will
   * fail at execution time; on the other hand, it can be used as a workaround to handle new
   * Cassandra features if the query builder does not cover them yet.
   */
  static Selector getRaw(String rawExpression) {
    return new RawSelector(rawExpression);
  }

  /**
   * Builds a relation testing a column.
   *
   * <p>This call must be chained with an operator, for example:
   *
   * <pre>{@code
   * selectFrom("foo").getAll().where(isColumn("k").eq(bindMarker()));
   * }</pre>
   */
  static ColumnRelationBuilder isColumn(CqlIdentifier id) {
    return new DefaultColumnRelationBuilder(id);
  }

  /**
   * Shortcut for {@link QueryBuilderDsl#isColumn(CqlIdentifier)
   * isColumn(CqlIdentifier.fromCql(name))}
   */
  static ColumnRelationBuilder isColumn(String name) {
    return isColumn(CqlIdentifier.fromCql(name));
  }

  /**
   * Builds a relation testing a qualified component of collection (as of Cassandra 4, this can only
   * be used for map values).
   */
  static ColumnComponentRelationBuilder isColumnComponent(CqlIdentifier columnId, Term index) {
    return new DefaultColumnComponentRelationBuilder(columnId, index);
  }

  /**
   * Shortcut for {@link QueryBuilderDsl#isColumnComponent(CqlIdentifier, Term)
   * isColumnComponent(CqlIdentifier.fromCql(columnName), index)}
   */
  static ColumnComponentRelationBuilder isColumnComponent(String columnName, Term index) {
    return isColumnComponent(CqlIdentifier.fromCql(columnName), index);
  }

  /** Builds a relation testing a token generated from a set of columns. */
  static TokenRelationBuilder isTokenFromIds(Iterable<CqlIdentifier> identifiers) {
    return new DefaultTokenRelationBuilder(identifiers);
  }

  /** Var-arg equivalent of {@link QueryBuilderDsl#isTokenFromIds(Iterable)}. */
  static TokenRelationBuilder isToken(CqlIdentifier... identifiers) {
    return isTokenFromIds(Arrays.asList(identifiers));
  }

  /**
   * Equivalent of {@link QueryBuilderDsl#isTokenFromIds(Iterable)} with raw strings; the names are
   * converted with {@link CqlIdentifier#fromCql(String)}.
   */
  static TokenRelationBuilder isToken(Iterable<String> names) {
    return isTokenFromIds(Iterables.transform(names, CqlIdentifier::fromCql));
  }

  /** Var-arg equivalent of {@link #isToken(Iterable)}. */
  static TokenRelationBuilder isToken(String... names) {
    return isToken(Arrays.asList(names));
  }

  /** Builds a relation testing a set of columns, as in {@code WHERE (c1, c2, c3) IN ...}. */
  static TupleRelationBuilder isTupleOfIds(Iterable<CqlIdentifier> identifiers) {
    return new DefaultTupleRelationBuilder(identifiers);
  }

  /** Var-arg equivalent of {@link #isTupleOfIds(Iterable)}. */
  static TupleRelationBuilder isTuple(CqlIdentifier... identifiers) {
    return isTupleOfIds(Arrays.asList(identifiers));
  }

  /**
   * Equivalent of {@link #isTupleOfIds(Iterable)} with raw strings; the names are converted with
   * {@link CqlIdentifier#fromCql(String)}.
   */
  static TupleRelationBuilder isTuple(Iterable<String> names) {
    return isTupleOfIds(Iterables.transform(names, CqlIdentifier::fromCql));
  }

  /** Var-arg equivalent of {@link #isTuple(Iterable)}. */
  static TupleRelationBuilder isTuple(String... names) {
    return isTuple(Arrays.asList(names));
  }

  /** Builds a relation on a custom index. */
  static Relation isCustomIndex(CqlIdentifier indexId, Term expression) {
    return new CustomIndexRelation(indexId, expression);
  }

  /**
   * Shortcut for {@link #isCustomIndex(CqlIdentifier, Term)
   * isCustomIndex(CqlIdentifier.fromCql(indexName), expression)}
   */
  static Relation isCustomIndex(String indexName, Term expression) {
    return isCustomIndex(CqlIdentifier.fromCql(indexName), expression);
  }

  /**
   * Builds an arbitrary relation from a raw string.
   *
   * <p>The contents will be appended to the query as-is, without any syntax checking or escaping.
   * This method should be used with caution, as it's possible to generate invalid CQL that will
   * fail at execution time; on the other hand, it can be used as a workaround to handle new CQL
   * features that are not yet covered by the query builder.
   */
  static Relation isRaw(String raw) {
    return new RawRelation(raw);
  }

  /**
   * An ordered set of anonymous terms.
   *
   * <p>For example, this can be used for the right-hand side of {@link
   * QueryBuilderDsl#isTuple(String...)}.
   */
  static Term tuple(Iterable<? extends Term> components) {
    return new TupleTerm(components);
  }

  /** Var-arg equivalent of {@link #tuple(Iterable)}. */
  static Term tuple(Term... components) {
    return tuple(Arrays.asList(components));
  }

  // TODO add remaining terms (decide how far we go without having to resort to raw)
  // TODO arithmetics (termAddition, termMultiplication...)
  // TODO function calls (function, 2nd branch of simpleTerm)
  // TODO casts (3rd branch of simpleTerm)
  // TODO literals (value), or is raw(codec.format()) good enough? -- edge case really

  /**
   * A term from a raw string.
   *
   * <p>The contents will be appended to the query as-is, without any syntax checking or escaping.
   * This method should be used with caution, as it's possible to generate invalid CQL that will
   * fail at execution time; on the other hand, it can be used as a workaround to handle new CQL
   * features that are not yet covered by the query builder.
   */
  static Term raw(String raw) {
    return new RawTerm(raw);
  }

  /** Creates an anonymous bind marker, which appears as {@code ?} in the generated CQL. */
  static BindMarker bindMarker() {
    return bindMarker((CqlIdentifier) null);
  }

  /** Creates a named bind marker, which appears as {@code :id} in the generated CQL. */
  static BindMarker bindMarker(CqlIdentifier id) {
    return new DefaultBindMarker(id);
  }

  /** Shortcut for {@link #bindMarker(CqlIdentifier) bindMarker(CqlIdentifier.fromCql(name))} */
  static BindMarker bindMarker(String name) {
    return bindMarker(CqlIdentifier.fromCql(name));
  }
}
