import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StubLoadingProcessTest {
    @Test
    public void test() throws IOException, InterruptedException {
        Path pythonCodePath = Paths.get("src/python/sample.py");

        Map<String, Object> input = new HashMap<>();
        input.put("a", 1);
        input.put("b", 2);

        StubLoader<Map> process = new StubLoader<>(Map.class);

        Map output = process.load(pythonCodePath, input);
        return;
    }
}
