// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.scalastyle.scalariform;

import java.lang.reflect.Constructor
import scalariform.parser.CompilationUnit
import _root_.scalariform.lexer.Tokens._
import org.scalastyle.ScalariformChecker
import org.scalastyle._
import org.scalastyle.FileSpec
import _root_.scalariform.lexer.Token
import _root_.scalariform.parser.FunDefOrDcl

class MethodLengthChecker extends CombinedChecker {
  val errorKey = "method.length"

  import VisitorHelper._
  val DefaultMaximumLength = 50

  case class FunDefOrDclClazz(t: FunDefOrDcl, position: Option[Int], subs: List[FunDefOrDclClazz]) extends Clazz[FunDefOrDcl]()

  def verify(ast: CombinedAst): List[ScalastyleError] = {
    val maxLength = getInt("maxLength", DefaultMaximumLength)

    val it = for (
      t <- localvisit(ast.compilationUnit.immediateChildren(0));
      f <- traverse(t);
      if (matches(f, ast.lines, maxLength))
    ) yield {
      PositionError(t.position.get, List("" + maxLength))
    }

    it.toList
  }

  private def traverse(t: FunDefOrDclClazz): List[FunDefOrDclClazz] = t :: t.subs.map(traverse(_)).flatten

  private def matches(t: FunDefOrDclClazz, lines: Lines, maxLines: Int) = {
    val head = lines.toLineColumn(t.t.defToken.offset)
    val tail = lines.toLineColumn(t.t.tokens.last.offset)
    tail.get.line - head.get.line > maxLines
  }

  private def localvisit(ast: Any): List[FunDefOrDclClazz] = ast match {
    case t: FunDefOrDcl => List(FunDefOrDclClazz(t, Some(t.nameToken.offset), localvisit(t.localDef)))
    case t: Any => visit(t, localvisit)
  }
}
