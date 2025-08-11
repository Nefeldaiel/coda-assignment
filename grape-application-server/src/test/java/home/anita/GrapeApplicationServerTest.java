package home.anita;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.*;

@SpringBootTest
class GrapeApplicationServerTest {

    @Test
    void testMain() {
        String[] args = {"--port", "8080"};
        
        try (MockedStatic<PortHandler> mockedPortHandler = mockStatic(PortHandler.class)) {
            GrapeApplicationServer.main(args);
            
            mockedPortHandler.verify(() -> 
                PortHandler.startWithPortRange(GrapeApplicationServer.class, args)
            );
        }
    }

    @Test
    void testMainWithNoArgs() {
        String[] args = {};
        
        try (MockedStatic<PortHandler> mockedPortHandler = mockStatic(PortHandler.class)) {
            GrapeApplicationServer.main(args);
            
            mockedPortHandler.verify(() -> 
                PortHandler.startWithPortRange(GrapeApplicationServer.class, args)
            );
        }
    }
}