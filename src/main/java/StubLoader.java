import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

/**
 * スタブ用ファイル読み込みクラス
 *
 * スタブ用ファイルを、その形式に応じて適切に読み込むためのクラス。
 *
 * @param <Out> スタブ用ファイルから読み込む出力Beanの型
 */
public class StubLoader<Out> {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * スタブ用ファイル読み込み
     *
     * スタブ用ファイルを読み込み、JSONオブジェクトを作成する。
     *
     * スタブ用ファイルとして JSON ファイルを受け渡した場合は、その内容をパースしたオブジェクトを返却する。
     * python ファイルを受け渡した場合は、そのスクリプトを実行してその標準出力をJSONとしてパースしたオブジェクトを返却する。
     * スクリプトを実行する際は、引数の inputBean をJSON化した文字列を標準入力として入力するため、
     * スクリプト内で inputBean の内容を利用することができる。
     *
     * @param stubFilePath スタブ用ファイルのパス
     * @param inputBean 入力Bean
     * @return スタブ用ファイルから読み込んだオブジェクト
     * @throws IOException スタブ用ファイルの読み込みや実行に失敗した場合
     * @throws InterruptedException スタブ用ファイルの実行中に割り込みが発生した場合
     */
    public Out load(Path stubFilePath, Object inputBean) throws IOException, InterruptedException {
        String interpreter = getInterpreter(stubFilePath);

        if(interpreter.equals("json")) {
            // JSON はそのまま読み込む
            return this.mapper.readValue(stubFilePath.toFile(), new TypeReference<Out>(){ });
        } else {
            // スクリプトを実行してその標準出力を読み込む
            return exec(interpreter, stubFilePath, inputBean);
        }
    }

    /**
     * スタブ用ファイル実行
     *
     * スタブ用ファイルをスクリプトとして実行し、その標準出力をJSONとしてパースしたオブジェクトを返却する。
     * スクリプトを実行する際は、引数の inputBean をJSON化した文字列を標準入力として入力するため、
     * スクリプト内で inputBean の内容を利用することができる。
     *
     * @param interpreter スタブ用ファイルを実行するインタプリタ。実行コマンドの第一単語として使用される。
     * @param scriptPath スタブ用ファイルのパス
     * @param inputBean 入力Bean
     * @return スタブ用ファイルの標準出力をJSONとしてパースしたオブジェクト
     * @throws IOException スタブ用ファイルの読み込みや実行に失敗した場合
     * @throws InterruptedException スタブ用ファイルの実行中に割り込みが発生した場合
     */
    private Out exec(String interpreter, Path scriptPath, Object inputBean) throws IOException, InterruptedException {

        ProcessBuilder processBuilder = constructScriptProcessBuilder(interpreter, scriptPath);
        Process process = processBuilder.start();

        // 子プロセスの標準入力へ JSON 文字列を入力
        this.mapper.writeValue(process.getOutputStream(), inputBean);

        // 子プロセスの終了を待つ
        int exitCode = process.waitFor();

        if(exitCode == 0) {
            return this.mapper.readValue(process.getInputStream(), new TypeReference<Out>(){ });
        } else {
            return null;
        }

    }

    /**
     * スクリプト実行プロセス作成
     *
     * スタブ用ファイルをスクリプトとして実行するためのプロセスビルダーを作成する。
     *
     * @param interpreter スタブ用ファイルを実行するインタプリタ。実行コマンドの第一単語として使用される。
     * @param scriptPath スタブ用ファイルのパス
     * @return プロセスビルダー
     */
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

    /**
     * インタプリタ取得
     *
     * スクリプトファイル名から推測されるインタプリタ名 (もしくはパス) を出力する。
     * ただし、JSON ファイルと推測される場合は "json" を返す。
     *
     * 推測は拡張子のみを使用して行われ、ファイルの内容は検査されない。
     *
     * @param scriptPath スクリプトファイルパス
     * @return "json" もしくはインタプリタ名 (もしくはパス)
     */
    private String getInterpreter(Path scriptPath) {
        String scriptName = scriptPath.getFileName().toString();

        if(scriptName.endsWith(".py")) {
            return "python";
        } else if (scriptName.endsWith(".json")) {
            return "json";
        } else {
            throw new IllegalArgumentException();
        }
    }
}
