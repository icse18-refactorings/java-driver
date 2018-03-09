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

import com.datastax.oss.driver.internal.querybuilder.relation.RawTerm;
import com.datastax.oss.driver.internal.querybuilder.relation.TupleTerm;
import java.util.Arrays;

public interface Term {

  // TODO add remaining terms (decide how far we go without having to resort to rawTerm)
  // TODO arithmetics (termAddition, termMultiplication...)
  // TODO function calls (function, 2nd branch of simpleTerm)
  // TODO casts (3rd branch of simpleTerm)
  // TODO literals (value), or is rawTerm(codec.format()) good enough? -- edge case really

  static Term rawTerm(String raw) {
    return new RawTerm(raw);
  }

  static Term tuple(Iterable<? extends Term> components) {
    return new TupleTerm(components);
  }

  /** Var-arg equivalent of {@link #tuple(Iterable)}. */
  static Term tuple(Term... components) {
    return tuple(Arrays.asList(components));
  }

  String asCql(boolean pretty);
}
