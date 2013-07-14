package org.funcito;

import fj.Effect;
import org.funcito.internal.FuncitoDelegate;
import org.funcito.internal.WrapperType;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static org.funcito.FuncitoFJ.*;
import static org.junit.Assert.*;

/**
 * Copyright 2013 Project Funcito Contributors
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class FuncitoFjEffect_UT {

    public @Rule ExpectedException thrown = ExpectedException.none();
    private Grows CALLS_TO_GROWS = callsTo(Grows.class);

    @After
    public void tearDown() {
        try {
            new FuncitoDelegate().extractInvokableState(WrapperType.FJ_VOID_EFFECT);
        } catch (Throwable t) {}
    }

    public class Grows {
        int i = 0;
        public String incAndReturn() {
            i++;
            return Integer.toString(i);
        }
        public void inc() {
            i++;
        }
        public void dec() {
            i--;
        }
    }

    @Test
    public void testEffectFor_AssignToEffectWithSourceSuperType() {
        Grows grows = new Grows();

        Effect<Object> superTypeRet = effectFor(CALLS_TO_GROWS.incAndReturn()); // Generic type is Object instead of Grows
        assertEquals(0, grows.i);

        superTypeRet.e(grows);
        assertEquals(1, grows.i);
    }

    class Generic<N extends Number> {
        Double number;
        public Generic(N n) { number = n.doubleValue(); }
        public Double incAndGet() {
            return ++number;
        }
        public void voidInc() {
            ++number;
        }
    }

    @Test
    public void testEffectFor_ValidateDetectsMismatchedGenericTypes() {
        Effect<Generic<Float>> floatGenericEffect = effectFor(callsTo(Generic.class).incAndGet());
        Generic<Integer> integerGeneric = new Generic<Integer>(0);

//        The below can't actually be compiled, which proves the test passes: compile time mismatch detection
//        floatGenericEffect.f(integerGeneric);
    }

    @Test
    public void testEffectFor_AllowUpcastToExtensionGenericType() {
        Effect<Generic<? extends Object>> incEffect = effectFor(callsTo(Generic.class).incAndGet());
        Generic<Integer> integerGeneric = new Generic<Integer>(0);
        assertEquals(0, integerGeneric.number, 0.01);

        incEffect.e(integerGeneric);

        assertEquals(1.0, integerGeneric.number, 0.01);
    }

    @Test
    public void testEffectFor_SingleArgBinding() {
        class IncList extends ArrayList<Integer> {
            public int incIndex(int i) {
                int oldVal = this.get(i);
                int newVal = oldVal + 1;
                this.set(i, newVal);
                return newVal;
            }
        }
        IncList callsToIncList = callsTo(IncList.class);
        Effect<IncList> incElem0Func = effectFor(callsToIncList.incIndex(0));
        Effect<IncList> incElem2Func = effectFor(callsToIncList.incIndex(2));
        IncList list = new IncList();
        list.add(0); list.add(100); list.add(1000);

        incElem0Func.e(list);
        incElem2Func.e(list);

        assertEquals(1, list.get(0).intValue());
        assertEquals(100, list.get(1).intValue()); // unchanged
        assertEquals(1001, list.get(2).intValue());
    }

    @Test
    public void testVoidEffect_withPrepare() {
        Grows grows = new Grows();

        prepareVoid(CALLS_TO_GROWS).inc();
        Effect<Grows> normalCall = voidEffect();

        assertEquals(0, grows.i);
        normalCall.e(grows);
        assertEquals(1, grows.i);
    }

    @Test
    public void testVoidEffect_withoutPrepare() {
        Grows grows = new Grows();

        // non-preferred.  Better to use prepare() to help explain
        CALLS_TO_GROWS.inc();
        Effect<Grows> normalCall = voidEffect();

        assertEquals(0, grows.i);
        normalCall.e(grows);
        assertEquals(1, grows.i);
    }

    @Test
    public void testVoidEffect_prepareWithNoMethodCall() {
        prepareVoid(CALLS_TO_GROWS); // did not append any ".methodCall()" after close parenthesis

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("No call to a");
        Effect<Grows> badCall = voidEffect();
    }

    @Test
    public void testVoidEffect_AssignToEffectWithSourceSuperTypeOk() {
        Grows grows = new Grows();

        prepareVoid(CALLS_TO_GROWS).inc();
        Effect<Object> superTypeRet = voidEffect(); // Generic type is Object instead of Grows
        assertEquals(0, grows.i);

        superTypeRet.e(grows);
        assertEquals(1, grows.i);
    }

    @Test
    public void testVoidEffect_unsafeAssignment() {
        prepareVoid(CALLS_TO_GROWS).inc();
        Effect<Integer> unsafe = voidEffect();  // unsafe assignment compiles

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("Method inc() does not exist");
        unsafe.e(3); // invocation target type does not match prepared target type
    }

    @Test
    public void testVoidEffect_typeValidationSucceeds() {
        prepareVoid(CALLS_TO_GROWS).inc();

        Effect<Grows> grows = voidEffect(Grows.class);
    }

    @Test
    public void testVoidEffect_typeValidationSucceedsWithSuperClass() {
        class Grows2 extends Grows{}
        prepareVoid(callsTo(Grows2.class)).inc();

        Effect<Grows> grows = voidEffect(Grows.class);
    }

    @Test
    public void testVoidEffect_typeValidationFails() {
        prepareVoid(CALLS_TO_GROWS).inc();

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("Failed to create Functional Java Effect");
        Effect<?> e = voidEffect(Number.class);  // type validation
    }


    @Test
    public void testVoidEffect_typeValidationFailsButLeavesInvokableStateUnchanged() {
        prepareVoid(CALLS_TO_GROWS).inc();

        try {
            Effect<?> e = voidEffect(Number.class);  // type validation should fail
            fail("should have thrown exception");
        } catch (FuncitoException e) {
            Effect<Grows> g = voidEffect(Grows.class);  // type validation
        }
    }

    @Test
    public void testVoidEffect_badOrderOfPrepares() {
        // First call below requires a subsequent call to voidEffect() before another prepareVoid()
        prepareVoid(CALLS_TO_GROWS).inc();

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("or back-to-back \"prepareVoid()\" calls");
        // bad to call prepareVoid() twice without intermediate voidEffect()
        prepareVoid(CALLS_TO_GROWS).dec();
        // see clean-up in method tearDown()
    }

    @Test
    public void testVoidEffect_interleavedPreparesDifferentSourcesAlsoNotOk() {
        prepareVoid(CALLS_TO_GROWS).inc();

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("or back-to-back \"prepareVoid()\" calls");
        prepareVoid(callsTo(Generic.class)).voidInc();
    }

    @Test
    public void testVoidEffect_preparedForNonVoidMethod() {
        Grows grows = new Grows();
        assertEquals(0, grows.i);

        prepareVoid(CALLS_TO_GROWS).incAndReturn();
        Effect<Grows> e = voidEffect();

        e.e(grows);

        assertEquals(1, grows.i);
    }
}

