package home.anita;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.PortInUseException;

import java.net.BindException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PortHandlerTest {

    @Test
    void testGetPortFromArgsWithValidPort() throws Exception {
        Method method = PortHandler.class.getDeclaredMethod("getPortFromArgs", String[].class, String.class, int.class);
        method.setAccessible(true);
        
        String[] args = {"--port", "8080"};
        int result = (int) method.invoke(null, args, "port", 9001);
        
        assertEquals(8080, result);
    }

    @Test
    void testGetPortFromArgsWithDefaultPort() throws Exception {
        Method method = PortHandler.class.getDeclaredMethod("getPortFromArgs", String[].class, String.class, int.class);
        method.setAccessible(true);
        
        String[] args = {};
        int result = (int) method.invoke(null, args, "port", 9001);
        
        assertEquals(9001, result);
    }

    @Test
    void testGetPortFromArgsWithInvalidPort() throws Exception {
        Method method = PortHandler.class.getDeclaredMethod("getPortFromArgs", String[].class, String.class, int.class);
        method.setAccessible(true);
        
        String[] args = {"--port", "invalid"};
        int result = (int) method.invoke(null, args, "port", 9001);
        
        assertEquals(9001, result);
    }

    @Test
    void testIsPortInUseExceptionWithPortInUseException() throws Exception {
        Method method = PortHandler.class.getDeclaredMethod("isPortInUseException", Exception.class);
        method.setAccessible(true);
        
        PortInUseException exception = new PortInUseException(8080);
        boolean result = (boolean) method.invoke(null, exception);
        
        assertTrue(result);
    }

    @Test
    void testIsPortInUseExceptionWithBindException() throws Exception {
        Method method = PortHandler.class.getDeclaredMethod("isPortInUseException", Exception.class);
        method.setAccessible(true);
        
        BindException exception = new BindException("Address already in use");
        boolean result = (boolean) method.invoke(null, exception);
        
        assertTrue(result);
    }

    @Test
    void testIsPortInUseExceptionWithGenericException() throws Exception {
        Method method = PortHandler.class.getDeclaredMethod("isPortInUseException", Exception.class);
        method.setAccessible(true);
        
        Exception exception = new Exception("Some other error");
        boolean result = (boolean) method.invoke(null, exception);
        
        assertFalse(result);
    }

    @Test
    void testIsPortInUseExceptionWithNestedCause() throws Exception {
        Method method = PortHandler.class.getDeclaredMethod("isPortInUseException", Exception.class);
        method.setAccessible(true);
        
        BindException bindException = new BindException("Address already in use");
        RuntimeException wrapperException = new RuntimeException("Wrapper", bindException);
        
        boolean result = (boolean) method.invoke(null, wrapperException);
        
        assertTrue(result);
    }

    @Test
    void testIsPortInUseExceptionWithMessageContainingAddressInUse() throws Exception {
        Method method = PortHandler.class.getDeclaredMethod("isPortInUseException", Exception.class);
        method.setAccessible(true);
        
        Exception exception = new Exception("Address already in use on port 8080");
        boolean result = (boolean) method.invoke(null, exception);
        
        assertTrue(result);
    }
}