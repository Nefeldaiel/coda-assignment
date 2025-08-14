package home.anita;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentHandlerTest {

    @Test
    void testGetIntFromArgsWithValidValue() throws Exception {
        Method method = ArgumentHandler.class.getDeclaredMethod("getIntFromArgs", String[].class, String.class, int.class);
        method.setAccessible(true);
        
        String[] args = {"--port", "8080"};
        int result = (int) method.invoke(null, args, "port", 9001);
        
        assertEquals(8080, result);
    }

    @Test
    void testGetIntFromArgsWithDefaultValue() throws Exception {
        Method method = ArgumentHandler.class.getDeclaredMethod("getIntFromArgs", String[].class, String.class, int.class);
        method.setAccessible(true);
        
        String[] args = {};
        int result = (int) method.invoke(null, args, "port", 9001);
        
        assertEquals(9001, result);
    }

    @Test
    void testGetIntFromArgsWithInvalidValue() throws Exception {
        Method method = ArgumentHandler.class.getDeclaredMethod("getIntFromArgs", String[].class, String.class, int.class);
        method.setAccessible(true);
        
        String[] args = {"--port", "invalid"};
        int result = (int) method.invoke(null, args, "port", 9001);
        
        assertEquals(9001, result);
    }

    @Test
    void testGetBooleanFromArgsWithTrue() throws Exception {
        Method method = ArgumentHandler.class.getDeclaredMethod("getBooleanFromArgs", String[].class, String.class, boolean.class);
        method.setAccessible(true);
        
        String[] args = {"--slow=true"};
        boolean result = (boolean) method.invoke(null, args, "slow", false);
        
        assertTrue(result);
    }

    @Test
    void testGetBooleanFromArgsWithFalse() throws Exception {
        Method method = ArgumentHandler.class.getDeclaredMethod("getBooleanFromArgs", String[].class, String.class, boolean.class);
        method.setAccessible(true);
        
        String[] args = {"--slow=false"};
        boolean result = (boolean) method.invoke(null, args, "slow", true);
        
        assertFalse(result);
    }

    @Test
    void testGetBooleanFromArgsWithDefaultValue() throws Exception {
        Method method = ArgumentHandler.class.getDeclaredMethod("getBooleanFromArgs", String[].class, String.class, boolean.class);
        method.setAccessible(true);
        
        String[] args = {};
        boolean result = (boolean) method.invoke(null, args, "slow", false);
        
        assertFalse(result);
    }
}