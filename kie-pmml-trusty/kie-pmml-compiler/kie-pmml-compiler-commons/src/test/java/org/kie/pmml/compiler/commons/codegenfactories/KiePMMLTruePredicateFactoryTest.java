/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.pmml.compiler.commons.codegenfactories;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.dmg.pmml.True;
import org.junit.Test;
import org.kie.pmml.commons.model.predicates.KiePMMLTruePredicate;
import org.kie.pmml.compiler.commons.utils.JavaParserUtils;

import static org.junit.Assert.assertTrue;
import static org.kie.pmml.compiler.commons.testutils.CodegenTestUtils.commonValidateCompilationWithImports;

public class KiePMMLTruePredicateFactoryTest {

    @Test
    public void getTruePredicateVariableDeclaration() {
        String variableName = "variableName";
        BlockStmt retrieved = KiePMMLTruePredicateFactory.getTruePredicateVariableDeclaration(variableName, new True());
        Statement expected = JavaParserUtils.parseBlock(String.format("{" +
                                                                                  "KiePMMLTruePredicate %1$s = new " +
                                                                                  "KiePMMLTruePredicate(\"%1$s\", " +
                                                                                  "Collections" +
                                                                                  ".emptyList());" +
                                                                                  "}", variableName));
        assertTrue(JavaParserUtils.equalsNode(expected, retrieved));
        List<Class<?>> imports = Arrays.asList(KiePMMLTruePredicate.class, Collections.class);
        commonValidateCompilationWithImports(retrieved, imports);
    }
}