package es.eriktorr.todo
package common.data.refined

import io.github.iltotore.iron.DescribedAs
import io.github.iltotore.iron.constraint.any.Not
import io.github.iltotore.iron.constraint.numeric.{GreaterEqual, LessEqual}
import io.github.iltotore.iron.constraint.string.{Blank, Match}

object Constraints:
  type Between[Min, Max] = GreaterEqual[Min] & LessEqual[Max]

  type NonEmptyString =
    DescribedAs[Not[Blank], "Should contain at least one non-whitespace character"]

  type UrlPathSegment = DescribedAs[Match["^/[0-9a-zA-Z_-]+"], "Should be a valid URL path segment"]
