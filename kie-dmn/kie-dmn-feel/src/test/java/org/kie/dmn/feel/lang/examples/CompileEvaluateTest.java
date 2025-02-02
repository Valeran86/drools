package org.kie.dmn.feel.lang.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.dmn.api.feel.runtime.events.FEELEvent;
import org.kie.dmn.api.feel.runtime.events.FEELEvent.Severity;
import org.kie.dmn.api.feel.runtime.events.FEELEventListener;
import org.kie.dmn.feel.FEEL;
import org.kie.dmn.feel.lang.CompiledExpression;
import org.kie.dmn.feel.lang.CompilerContext;
import org.kie.dmn.feel.lang.impl.MapBackedType;
import org.kie.dmn.feel.lang.types.BuiltInType;
import org.kie.dmn.feel.lang.types.GenListType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;
import static org.kie.dmn.feel.util.DynamicTypeUtils.entry;
import static org.kie.dmn.feel.util.DynamicTypeUtils.prototype;

public class CompileEvaluateTest {
    private static final Logger LOG = LoggerFactory.getLogger(CompileEvaluateTest.class);
    private static final FEEL feel = FEEL.newInstance();
    private List<FEELEvent> errors;
    private FEELEventListener errorsCountingListener;
    
    static {
        feel.addListener(evt -> { 
            if (evt.getSeverity() == FEELEvent.Severity.ERROR) { 
                LOG.error("{}", evt);
                if ( evt.getSourceException().getCause() != null ) {
                    Throwable c = evt.getSourceException().getCause();
                    while (c != null) {
                        LOG.error(" caused by: {} {}", c.getClass(), c.getMessage() != null ? c.getMessage() : "");
                        c = c.getCause();
                    }
                    LOG.error(" [stacktraces omitted.]");
                }
            } else if (evt.getSeverity() == FEELEvent.Severity.WARN) { 
                LOG.warn("{}", evt);
            }
        } );
    }
    
    @Before
    public void before() {
        errors = new ArrayList<>();
        errorsCountingListener = evt -> { if ( evt.getSeverity() == Severity.ERROR ) { errors.add(evt); } };
        feel.addListener( errorsCountingListener );
    }
    
    @After
    public void after() {
        feel.removeListener( errorsCountingListener );
    }
    
    @Test
    public void test_isDynamicResolution() {
        CompilerContext ctx = feel.newCompilerContext();
        ctx.addInputVariableType( "Person List", BuiltInType.LIST);
        
        CompiledExpression compiledExpression = feel.compile( "Person List[My Variable 1 = \"A\"]", ctx );
        
        assertThat(errors.toString(), errors.size(), is(0) );

        Map<String, Object> inputs = new HashMap<>();
        List<Map<String, ?>> pList = new ArrayList<>();
        inputs.put("Person List", pList);
        pList.add(prototype(entry("Full Name",    "Edson Tirelli"),
                            entry("My Variable 1","A"))
                );
        pList.add(prototype(entry("Full Name",    "Matteo Mortari"),
                            entry("My Variable 1","B"))
                );
        
        Object result = feel.evaluate(compiledExpression, inputs);
        
        assertThat( result, instanceOf( List.class ) );
        assertThat( (List<?>) result, hasSize(1) );
        assertThat( ((Map<?, ?>) ((List<?>) result).get(0)).get("Full Name"), is("Edson Tirelli") );
    }        
    
    @Test
    public void test2() {
        CompilerContext ctx = feel.newCompilerContext();
        ctx.addInputVariableType( "MyPerson", new MapBackedType().addField( "FullName", BuiltInType.STRING ) );
        
        CompiledExpression compiledExpression = feel.compile( "MyPerson.fullName", ctx );
        
        assertThat(errors.toString(), errors.size(), is(1) );
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put( "MyPerson", prototype(entry("FullName", "John Doe")) );
        
        Object result = feel.evaluate(compiledExpression, inputs);
        
        assertThat(result, nullValue());
    }

    @Test
    public void test2OK() {
        CompilerContext ctx = feel.newCompilerContext();
        ctx.addInputVariableType( "MyPerson", new MapBackedType().addField( "FullName", BuiltInType.STRING ) );
        
        CompiledExpression compiledExpression = feel.compile( "MyPerson.FullName", ctx );
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put( "MyPerson", prototype(entry("FullName", "John Doe")) );
        
        Object result = feel.evaluate(compiledExpression, inputs);
        
        assertThat(result, is("John Doe"));
    }
    
    @Test
    public void testHyphenInProperty() {
        CompilerContext ctx = feel.newCompilerContext();
        ctx.addInputVariableType( "input", new MapBackedType().addField( "Primary-Key", BuiltInType.STRING ).addField( "Value", BuiltInType.STRING ) );
        CompiledExpression compiledExpression = feel.compile( "input.Primary-Key", ctx );
        assertTrue(errors.isEmpty());
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put( "input", prototype(entry("Primary-Key", "k987")) );
        Object result = feel.evaluate(compiledExpression, inputs);
        assertThat(result, is("k987"));
        assertTrue(errors.isEmpty());
    }
    
    @Test
    public void testHyphenInPropertyOfCollectionForProjection() {
        MapBackedType compositeType = new MapBackedType().addField( "Primary-Key", BuiltInType.STRING ).addField( "Value", BuiltInType.STRING );
        CompilerContext ctx = feel.newCompilerContext();
        ctx.addInputVariableType( "input", new GenListType(compositeType) );
        CompiledExpression compiledExpression = feel.compile( "input.Primary-Key", ctx );
        assertTrue(errors.isEmpty());
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put( "input", Arrays.asList(prototype(entry("Primary-Key", "k987"))) );
        Object result = feel.evaluate(compiledExpression, inputs);
        assertThat(result, is(Arrays.asList("k987")));
        assertTrue(errors.isEmpty());
    }
    
    @Test
    public void testHyphenInPropertyOfCollectionForAccessor() {
        MapBackedType compositeType = new MapBackedType().addField( "Primary-Key", BuiltInType.STRING ).addField( "Value", BuiltInType.STRING );
        CompilerContext ctx = feel.newCompilerContext();
        ctx.addInputVariableType( "my input", new GenListType(compositeType) );
        CompiledExpression compiledExpression = feel.compile( "my input[1].Primary-Key", ctx );
        assertTrue(errors.isEmpty());
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put( "my input", Arrays.asList(prototype(entry("Primary-Key", "k987"))) );
        Object result = feel.evaluate(compiledExpression, inputs);
        assertThat(result, is("k987"));
        assertTrue(errors.isEmpty());
    }
}
