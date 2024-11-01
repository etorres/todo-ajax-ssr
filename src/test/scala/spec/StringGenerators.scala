package es.eriktorr.todo
package spec

import org.scalacheck.Gen

object StringGenerators:
  private val defaultMaxLength = 200

  def alphaNumericStringBetween(minLength: Int, maxLength: Int): Gen[String] =
    stringBetween(minLength, maxLength, Gen.alphaNumChar)

  def alphaStringOf(size: Int): Gen[String] = stringOfLength(size, Gen.alphaChar)

  def alphaUpperStringOf(size: Int): Gen[String] = stringOfLength(size, Gen.alphaUpperChar)

  def nonEmptyAlphaNumericStringShorterThan(maxLength: Int): Gen[String] =
    nonEmptyStringShorterThan(maxLength, Gen.alphaNumChar)

  val nonEmptyAlphaStringGen: Gen[String] =
    nonEmptyStringShorterThan(defaultMaxLength, Gen.alphaChar)

  private def nonEmptyStringShorterThan(maxLength: Int, charGen: Gen[Char]): Gen[String] =
    stringBetween(1, maxLength, charGen)

  private def stringBetween(minLength: Int, maxLength: Int, charGen: Gen[Char]): Gen[String] =
    for
      stringLength <- Gen.choose(minLength, maxLength)
      string <- stringOfLength(stringLength, charGen)
    yield string

  private def stringOfLength(length: Int, charGen: Gen[Char]): Gen[String] =
    Gen.listOfN(length, charGen).map(_.mkString)
