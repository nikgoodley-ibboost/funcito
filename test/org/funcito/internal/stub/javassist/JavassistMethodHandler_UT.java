package org.funcito.internal.stub.javassist;

import org.funcito.internal.FuncitoDelegate;
import org.funcito.internal.WrapperType;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Stack;

import static org.junit.Assert.assertNull;

public class JavassistMethodHandler_UT {

    private JavassistMethodHandler handler = new JavassistMethodHandler();
    private FuncitoDelegate delegate = new FuncitoDelegate();

    @After
    public void tearDown() {
        // pop last method pushed, between each test
        delegate.getInvokable(WrapperType.GUAVA_FUNCTION);
    }

    @Test
    public void testInvoke_noExceptionForObject() throws Throwable {
        Method objMethod = Stack.class.getDeclaredMethod("pop");

        Object fakeObj = handler.invoke(new Stack(), objMethod, null, null);

        assertNull(fakeObj);
    }

}
