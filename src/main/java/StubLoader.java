import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class StubLoader<Out> {
    private final Class<Out> outClz;
    private final ObjectMapper mapper = new ObjectMapper();

    public StubLoader(Class<Out> outClz) {
        this.outClz = outClz;
    }

    public Out load(Path stubFilePath, Object inputObject) throws IOException, InterruptedException {
        String interpreter = getInterpreter(stubFilePath);

        if(interpreter.equals("json")) {
            // JSON はそのまま読み込む
            return this.mapper.readValue(stubFilePath.toFile(), this.outClz);
        } else {
            // スクリプトを実行してその標準出力を読み込む
            return exec(interpreter, stubFilePath, inputObject);
        }
    }

    private Out exec(String interpreter, Path scriptPath, Object inputObject) throws IOException, InterruptedException {

        ProcessBuilder processBuilder = constructScriptProcessBuilder(interpreter, scriptPath);
        Process process = processBuilder.start();

        // 子プロセスの標準入力へ JSON 文字列を入力
        this.mapper.writeValue(process.getOutputStream(), inputObject);

        // 子プロセスの終了を待つ
        int exitCode = process.waitFor();

        if(exitCode == 0) {
            return this.mapper.readValue(process.getInputStream(), this.outClz);
        } else {
            return null;
        }

    }

    private ProcessBuilder constructScriptProcessBuilder(String interpreter, Path scriptPath) {
        // 実行するコマンド
        String[] command = new String[]{interpreter, scriptPath.getFileName().toString()};

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // 子プロセスのワークディレクトリを実行するスクリプトファイルがあるディレクトリへ変更
        processBuilder.directory(scriptPath.getParent().toFile());

        // 子プロセスの標準エラー出力を親プロセスの標準エラー出力へ
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        return processBuilder;
    }

    private String getInterpreter(Path scriptPath) {
        String scriptName = scriptPath.getFileName().toString();

        if(scriptName.endsWith(".py")) {
            return "python";
        } else {
            throw new IllegalArgumentException();
        }
    }
}
