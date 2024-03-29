package net.bytebuddy.agent.builder;

import com.sun.tools.javac.util.List;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.test.utility.MockitoRule;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import net.bytebuddy.utility.JavaModule;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AgentBuilderRedefinitionStrategyResubmissionStrategyTest {

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private Instrumentation instrumentation;

    @Mock
    private AgentBuilder.RedefinitionStrategy.ResubmissionScheduler resubmissionScheduler;

    @Mock
    private AgentBuilder.LocationStrategy locationStrategy;

    @Mock
    private AgentBuilder.Listener listener;

    @Mock
    private AgentBuilder.CircularityLock circularityLock;

    @Mock
    private AgentBuilder.RawMatcher rawMatcher;

    @Mock
    private ElementMatcher<? super Throwable> matcher;

    @Mock
    private Throwable error;

    @Mock
    private AgentBuilder.RedefinitionStrategy.BatchAllocator redefinitionBatchAllocator;

    @Mock
    private AgentBuilder.RedefinitionStrategy.Listener redefinitionListener;

    @Test
    @SuppressWarnings("unchecked")
    public void testRetransformation() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(true);
        when(matcher.matches(error)).thenReturn(true);
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.RETRANSFORMATION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verify(instrumentation).isModifiableClass(Foo.class);
        verify(instrumentation).retransformClasses(Foo.class);
        verifyNoMoreInteractions(instrumentation);
        verify(rawMatcher).matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain());
        verifyNoMoreInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>singletonList(Foo.class));
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRedefinition() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(true);
        when(matcher.matches(error)).thenReturn(true);
        ClassFileLocator classFileLocator = mock(ClassFileLocator.class);
        when(locationStrategy.classFileLocator(Foo.class.getClassLoader(), JavaModule.ofType(Foo.class))).thenReturn(classFileLocator);
        when(classFileLocator.locate(Foo.class.getName())).thenReturn(new ClassFileLocator.Resolution.Explicit(new byte[]{1,2,3}));
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.REDEFINITION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verify(instrumentation).isModifiableClass(Foo.class);
        verify(instrumentation).redefineClasses(Mockito.argThat(new BaseMatcher<ClassDefinition[]>() {
            @Override
            public boolean matches(Object o) {
                return ((ClassDefinition) o).getDefinitionClass() == Foo.class
                        && Arrays.equals(((ClassDefinition) o).getDefinitionClassFile(), new byte[]{1,2,3});
            }

            @Override
            public void describeTo(Description description) {
            }
        }));
        verifyNoMoreInteractions(instrumentation);
        verify(rawMatcher).matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain());
        verifyNoMoreInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>singletonList(Foo.class));
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRetransformationNonModifiable() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(false);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(true);
        when(matcher.matches(error)).thenReturn(true);
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.RETRANSFORMATION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verify(instrumentation).isModifiableClass(Foo.class);
        verifyNoMoreInteractions(instrumentation);
        verifyZeroInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRedefinitionNonModifiable() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(false);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(true);
        when(matcher.matches(error)).thenReturn(true);
        ClassFileLocator classFileLocator = mock(ClassFileLocator.class);
        when(locationStrategy.classFileLocator(Foo.class.getClassLoader(), JavaModule.ofType(Foo.class))).thenReturn(classFileLocator);
        when(classFileLocator.locate(Foo.class.getName())).thenReturn(new ClassFileLocator.Resolution.Explicit(new byte[]{1,2,3}));
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.REDEFINITION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verify(instrumentation).isModifiableClass(Foo.class);
        verifyNoMoreInteractions(instrumentation);
        verifyZeroInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRetransformationNonMatched() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(false);
        when(matcher.matches(error)).thenReturn(true);
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.RETRANSFORMATION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verify(instrumentation).isModifiableClass(Foo.class);
        verifyNoMoreInteractions(instrumentation);
        verify(rawMatcher).matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain());
        verifyNoMoreInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRedefinitionNonMatched() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(false);
        when(matcher.matches(error)).thenReturn(true);
        ClassFileLocator classFileLocator = mock(ClassFileLocator.class);
        when(locationStrategy.classFileLocator(Foo.class.getClassLoader(), JavaModule.ofType(Foo.class))).thenReturn(classFileLocator);
        when(classFileLocator.locate(Foo.class.getName())).thenReturn(new ClassFileLocator.Resolution.Explicit(new byte[]{1,2,3}));
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.REDEFINITION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verify(instrumentation).isModifiableClass(Foo.class);
        verifyNoMoreInteractions(instrumentation);
        verify(rawMatcher).matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain());
        verifyNoMoreInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRetransformationAlreadyLoaded() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(true);
        when(matcher.matches(error)).thenReturn(false);
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.RETRANSFORMATION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), true, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verifyZeroInteractions(instrumentation);
        verifyZeroInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), true, error);
        verifyNoMoreInteractions(listener);
        verifyZeroInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRedefinitionAlreadyLoaded() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(true);
        when(matcher.matches(error)).thenReturn(false);
        ClassFileLocator classFileLocator = mock(ClassFileLocator.class);
        when(locationStrategy.classFileLocator(Foo.class.getClassLoader(), JavaModule.ofType(Foo.class))).thenReturn(classFileLocator);
        when(classFileLocator.locate(Foo.class.getName())).thenReturn(new ClassFileLocator.Resolution.Explicit(new byte[]{1,2,3}));
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.REDEFINITION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), true, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verifyZeroInteractions(instrumentation);
        verifyZeroInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), true, error);
        verifyNoMoreInteractions(listener);
        verifyZeroInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRetransformationNonMatchedError() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(true);
        when(matcher.matches(error)).thenReturn(false);
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.RETRANSFORMATION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verifyZeroInteractions(instrumentation);
        verifyZeroInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRedefinitionNonMatchedError() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenReturn(true);
        when(matcher.matches(error)).thenReturn(false);
        ClassFileLocator classFileLocator = mock(ClassFileLocator.class);
        when(locationStrategy.classFileLocator(Foo.class.getClassLoader(), JavaModule.ofType(Foo.class))).thenReturn(classFileLocator);
        when(classFileLocator.locate(Foo.class.getName())).thenReturn(new ClassFileLocator.Resolution.Explicit(new byte[]{1,2,3}));
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.REDEFINITION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verifyZeroInteractions(instrumentation);
        verifyZeroInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRetransformationError() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        RuntimeException runtimeException = new RuntimeException();
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenThrow(runtimeException);
        when(matcher.matches(error)).thenReturn(true);
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.RETRANSFORMATION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verify(instrumentation).isModifiableClass(Foo.class);
        verifyNoMoreInteractions(instrumentation);
        verify(rawMatcher).matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain());
        verifyNoMoreInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), true, runtimeException);
        verify(listener).onComplete(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), true);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRedefinitionError() throws Exception {
        when(instrumentation.isModifiableClass(Foo.class)).thenReturn(true);
        when(redefinitionBatchAllocator.batch(Mockito.any(List.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Collections.singleton(invocationOnMock.getArgumentAt(0, List.class));
            }
        });
        RuntimeException runtimeException = new RuntimeException();
        when(rawMatcher.matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain())).thenThrow(runtimeException);
        when(matcher.matches(error)).thenReturn(true);
        ClassFileLocator classFileLocator = mock(ClassFileLocator.class);
        when(locationStrategy.classFileLocator(Foo.class.getClassLoader(), JavaModule.ofType(Foo.class))).thenReturn(classFileLocator);
        when(classFileLocator.locate(Foo.class.getName())).thenReturn(new ClassFileLocator.Resolution.Explicit(new byte[]{1,2,3}));
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy resubmissionStrategy = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled(resubmissionScheduler, matcher);
        resubmissionStrategy.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.REDEFINITION,
                redefinitionBatchAllocator,
                redefinitionListener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(resubmissionScheduler).schedule(argumentCaptor.capture());
        argumentCaptor.getValue().run();
        verify(instrumentation).isModifiableClass(Foo.class);
        verifyNoMoreInteractions(instrumentation);
        verify(rawMatcher).matches(new TypeDescription.ForLoadedType(Foo.class),
                Foo.class.getClassLoader(),
                JavaModule.ofType(Foo.class),
                Foo.class,
                Foo.class.getProtectionDomain());
        verifyNoMoreInteractions(rawMatcher);
        verify(redefinitionBatchAllocator).batch(Collections.<Class<?>>emptyList());
        verifyNoMoreInteractions(redefinitionBatchAllocator);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), false, error);
        verify(listener).onError(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), true, runtimeException);
        verify(listener).onComplete(Foo.class.getName(), Foo.class.getClassLoader(), JavaModule.ofType(Foo.class), true);
        verifyNoMoreInteractions(listener);
        verify(matcher).matches(error);
        verifyNoMoreInteractions(matcher);
    }

    @Test
    public void testDisabled() throws Exception {
        assertThat(AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Disabled.INSTANCE.onInstall(instrumentation,
                locationStrategy,
                this.listener,
                circularityLock,
                rawMatcher,
                AgentBuilder.RedefinitionStrategy.REDEFINITION,
                redefinitionBatchAllocator,
                redefinitionListener), sameInstance(listener));
    }

    @Test
    public void testLookupKeyBootstrapLoaderReference() throws Exception {
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey key = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(ClassLoadingStrategy.BOOTSTRAP_LOADER);
        assertThat(key.hashCode(), is(0));
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey other = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(new URLClassLoader(new URL[0]));
        System.gc();
        assertThat(key, not(is(other)));
        assertThat(key, is(new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(ClassLoadingStrategy.BOOTSTRAP_LOADER)));
        assertThat(key, is((Object) new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(ClassLoadingStrategy.BOOTSTRAP_LOADER)));
        assertThat(key, not(is((Object) new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(new URLClassLoader(new URL[0])))));
        assertThat(key, is(key));
        assertThat(key, not(is(new Object())));
    }

    @Test
    public void testLookupKeyNonBootstrapReference() throws Exception {
        ClassLoader classLoader = new URLClassLoader(new URL[0]);
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey key = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(classLoader);
        assertThat(key, is(new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(classLoader)));
        assertThat(key.hashCode(), is(classLoader.hashCode()));
        assertThat(key, not(is(new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(ClassLoadingStrategy.BOOTSTRAP_LOADER))));
        assertThat(key, not(is((Object) new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(new URLClassLoader(new URL[0])))));
        assertThat(key, is(key));
        assertThat(key, not(is(new Object())));
    }

    @Test
    public void testStorageKeyBootstrapLoaderReference() throws Exception {
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey key = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(ClassLoadingStrategy.BOOTSTRAP_LOADER);
        assertThat(key.isBootstrapLoader(), is(true));
        assertThat(key.hashCode(), is(0));
        assertThat(key.get(), nullValue(ClassLoader.class));
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey other = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(new URLClassLoader(new URL[0]));
        System.gc();
        assertThat(other.get(), nullValue(ClassLoader.class));
        assertThat(key, not(is(other)));
        assertThat(key, is(new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(ClassLoadingStrategy.BOOTSTRAP_LOADER)));
        assertThat(key, is((Object) new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(ClassLoadingStrategy.BOOTSTRAP_LOADER)));
        assertThat(key, not(is((Object) new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(new URLClassLoader(new URL[0])))));
        assertThat(key, is(key));
        assertThat(key, not(is(new Object())));
    }

    @Test
    public void testStorageKeyNonBootstrapReference() throws Exception {
        ClassLoader classLoader = new URLClassLoader(new URL[0]);
        AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey key = new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(classLoader);
        assertThat(key.isBootstrapLoader(), is(false));
        assertThat(key, is(new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(classLoader)));
        assertThat(key.hashCode(), is(classLoader.hashCode()));
        assertThat(key.get(), is(classLoader));
        classLoader = null; // Make GC eligible.
        System.gc();
        assertThat(key.get(), nullValue(ClassLoader.class));
        assertThat(key, not(is(new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.StorageKey(ClassLoadingStrategy.BOOTSTRAP_LOADER))));
        assertThat(key, not(is((Object) new AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.LookupKey(new URLClassLoader(new URL[0])))));
        assertThat(key, is(key));
        assertThat(key, not(is(new Object())));
        assertThat(key.isBootstrapLoader(), is(false));
    }

    @Test
    public void testJobHandlerAtFixedRate() throws Exception {
        ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
        Runnable runnable = mock(Runnable.class);
        new AgentBuilder.RedefinitionStrategy.ResubmissionScheduler.AtFixedRate(scheduledExecutorService, 42L, TimeUnit.SECONDS).schedule(runnable);
        verify(scheduledExecutorService).scheduleAtFixedRate(runnable, 42L, 42L, TimeUnit.SECONDS);
    }

    @Test
    public void testJobHandlerWithFixedDelay() throws Exception {
        ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
        Runnable runnable = mock(Runnable.class);
        new AgentBuilder.RedefinitionStrategy.ResubmissionScheduler.WithFixedDelay(scheduledExecutorService, 42L, TimeUnit.SECONDS).schedule(runnable);
        verify(scheduledExecutorService).scheduleWithFixedDelay(runnable, 42L, 42L, TimeUnit.SECONDS);
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Enabled.class).apply();
        ObjectPropertyAssertion.of(AgentBuilder.RedefinitionStrategy.ResubmissionStrategy.Disabled.class).apply();
        ObjectPropertyAssertion.of(AgentBuilder.RedefinitionStrategy.ResubmissionScheduler.AtFixedRate.class).apply();
        ObjectPropertyAssertion.of(AgentBuilder.RedefinitionStrategy.ResubmissionScheduler.WithFixedDelay.class).apply();
    }

    private static class Foo {
        /* empty */
    }

    private static class Bar {
        /* empty */
    }
}
