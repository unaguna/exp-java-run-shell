import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StubLoadingProcessTest {
    @Test
    public void test__load_python() throws IOException, InterruptedException {
        Path pythonCodePath = Paths.get("src/python/sample.py");

        Map<String, Object> input = new HashMap<>();
        input.put("a", 1);
        input.put("b", 2);

        StubLoader<Map<String, Object>> process = new StubLoader<>();

        Map<String, Object> output = process.load(pythonCodePath, input);
        return;
    }

    @Test
    public void test__load_json() throws IOException, InterruptedException {
        Path jsonPath = Paths.get("src/python/a.json");

        Map<String, Object> input = new HashMap<>();
        input.put("a", 1);
        input.put("b", 2);

        StubLoader<Map<String, Object>> process = new StubLoader<>();

        Map<String, Object> output = process.load(jsonPath, input);
        return;
    }

    @Test
    public void test__load__stderr() throws IOException, InterruptedException {
        Path jsonPath = Paths.get("src/python/sample_stderr.py");

        Map<String, Object> input = new HashMap<>();
        input.put("a", 1);
        input.put("b", 2);

        StubLoader<Map<String, Object>> process = new StubLoader<>();

        Map<String, Object> output = process.load(jsonPath, input);
        assert output.isEmpty();
    }
}
