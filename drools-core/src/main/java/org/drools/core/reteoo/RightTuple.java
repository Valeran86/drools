/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.drools.core.reteoo;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.spi.PropagationContext;
import org.drools.core.spi.Tuple;

public interface RightTuple extends Tuple {

    LeftTuple getBlocked();
    void setBlocked( LeftTuple leftTuple );
    void addBlocked( LeftTuple leftTuple );
    void removeBlocked( LeftTuple leftTuple );

    LeftTuple getTempBlocked();
    void setTempBlocked( LeftTuple tempBlocked );

    RightTuple getTempNextRightTuple();
    void setTempNextRightTuple( RightTuple tempNextRightTuple );

    InternalFactHandle getFactHandleForEvaluation();

    void retractTuple( PropagationContext context, ReteEvaluator reteEvaluator );

    void setExpired( ReteEvaluator reteEvaluator, PropagationContext pctx );
}