package net.bytebuddy.description.method;

import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Test;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import static org.mockito.Mockito.mock;

public class ParameterDescriptionForLoadedParameterDispatcherTest {

    private static final int FOO = 42;

    @Test(expected = IllegalStateException.class)
    public void testLegacyVmGetName() throws Exception {
        ParameterDescription.ForLoadedParameter.Dispatcher.ForLegacyVm.INSTANCE.getName(mock(AccessibleObject.class), FOO);
    }

    @Test(expected = IllegalStateException.class)
    public void testLegacyVmGetModifiers() throws Exception {
        ParameterDescription.ForLoadedParameter.Dispatcher.ForLegacyVm.INSTANCE.getModifiers(mock(AccessibleObject.class), FOO);
    }

    @Test(expected = IllegalStateException.class)
    public void testLegacyVmIsNamePresent() throws Exception {
        ParameterDescription.ForLoadedParameter.Dispatcher.ForLegacyVm.INSTANCE.isNamePresent(mock(AccessibleObject.class), FOO);
    }

    @Test
    public void testObjectProperties() throws Exception {
        final Iterator<Method> methods = Arrays.asList(Object.class.getDeclaredMethods()).iterator();
        ObjectPropertyAssertion.of(ParameterDescription.ForLoadedParameter.Dispatcher.ForJava8CapableVm.class).create(new ObjectPropertyAssertion.Creator<Method>() {
            @Override
            public Method create() {
                return methods.next();
            }
        }).apply();
        ObjectPropertyAssertion.of(ParameterDescription.ForLoadedParameter.Dispatcher.ForLegacyVm.class).apply();
    }
}