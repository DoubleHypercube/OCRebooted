package li.cil.oc.server.component;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HandleValuePersistenceTest {
    @Test
    public void savedHandleClassCanBeRestoredReflectively() throws Exception {
        var restored = Class.forName("li.cil.oc.server.component.HandleValue")
            .getDeclaredConstructor().newInstance();
        assertTrue(restored instanceof HandleValue);
    }
}
