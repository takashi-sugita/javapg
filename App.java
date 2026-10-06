import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection; // ★ SQLiteに接続するための型
import java.sql.DriverManager; // ★ JDBC接続を作るための型
import java.sql.PreparedStatement; // ★ SQLに値を安全に渡すための型
import java.sql.ResultSet; // ★ SELECTの結果を読むための型
import java.sql.SQLException; // ★ SQL処理の例外を扱うための型
import java.sql.Statement; // ★ テーブル作成SQLを実行するための型
import java.io.IOException; // ★ SQLエラーをHTTP処理へ伝えるための型
import java.util.ArrayList; // ★ SELECT結果を一覧にまとめるために使用
import java.util.List; // ★ Todo一覧の型

public class App {
    static final String DB_URL = "jdbc:sqlite:todos.db"; // ★ SQLiteデータベースの場所

    public static void main(String[] args) throws Exception {
        initializeDatabase(); // ★ 起動時にDBとtodos表を用意する

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/todos", exchange -> { // APIのURLに対する処理を登録する
            if (!exchange.getRequestURI().getPath().equals("/api/todos")) { // URLが完全一致するか確かめる
                exchange.sendResponseHeaders(404, -1); // 一致しないURLには「見つからない」を返す
                exchange.close(); // 通信を閉じる
                return; // ここで処理を終える
            } // URL確認の終わり
            if (!exchange.getRequestMethod().equals("GET")) { // GETで呼ばれたか確かめる
                exchange.sendResponseHeaders(405, -1); // GET以外には「許可されていない方法」を返す
                exchange.close(); // 通信を閉じる
                return; // ここで処理を終える
            } // リクエスト方法確認の終わり
            List<Todo> todos = loadTodos(); // SQLiteから全Todoを読み込む
            StringBuilder json = new StringBuilder("["); // JSON配列の文字列を作り始める
            for (int i = 0; i < todos.size(); i++) { // Todoを先頭から順に処理する
                if (i > 0) { // 2件目以降か確かめる
                    json.append(","); // Todo同士の間にカンマを入れる
                } // カンマ処理の終わり
                Todo todo = todos.get(i); // 今処理しているTodoを取り出す
                json.append("{\"title\":\"").append(escapeJson(todo.getTitle())) // タイトルをJSON用にエスケープする
                        .append("\",\"done\":").append(todo.isDone()).append("}"); // 完了状態を加えて項目を閉じる
            } // Todo一覧の処理の終わり
            json.append("]"); // JSON配列を閉じる
            byte[] body = json.toString().getBytes(StandardCharsets.UTF_8); // JSONをUTF-8のバイト列にする
            exchange.getResponseHeaders().set("Content-Type", "application/json"); // Content-Typeを指定どおりにする
            exchange.sendResponseHeaders(200, body.length); // 成功と本文の長さを返す
            exchange.getResponseBody().write(body); // JSON本文を送る
            exchange.close(); // 通信を閉じる
        }); // APIのURLに対する処理の登録を終える
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

            if (path.equals("/add") && method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String value = body.substring(5);
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8);
                if (!title.isEmpty()) {
                    addTodo(title); // ★ INSERT文で新しいTodoをDBに追加する
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/done") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        markDone(id); // ★ UPDATE文でDB上のTodoを完了にする
                    } catch (NumberFormatException e) {
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/delete") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        deleteTodo(id); // ★ DELETE文でDBからTodoを削除する
                    } catch (NumberFormatException e) {
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/")) {
                String html = "<!doctype html><html lang='ja'><head><meta charset='UTF-8'>"
                        + "<title>わたしのTodo</title><style>body { max-width: 640px; margin: 24px auto; "
                        + "padding: 0 16px; font-size: 16px; }</style></head><body>"
                        + "<h1>わたしのTodo</h1>"
                        + "<form method='post' action='/add'><input name='todo'><button>追加</button></form>";
                List<Todo> todos = loadTodos(); // ★ SELECT文でDBから一覧を読み込む
                if (todos.isEmpty()) {
                    html += "<p>やることは、いまゼロです</p>";
                } else {
                    html += "<ul>";
                    for (Todo todo : todos) {
                        String mark = "";
                        String title = todo.getTitle();
                        if (todo.isDone()) {
                            mark = " ✅";
                            title = "<span style='color: #888; text-decoration: line-through;'>"
                                    + title + "</span>";
                        }
                        html += "<li>" + title + mark + " <a href='/done?id=" + todo.getId()
                                + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                    }
                    html += "</ul>";
                }
                html += "</body></html>";
                message = html;
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }

    static String escapeJson(String value) { // JSON文字列内で特別な意味を持つ文字を変換する
        StringBuilder escaped = new StringBuilder(); // 変換後の文字列をためる
        for (int i = 0; i < value.length(); i++) { // 文字を1つずつ調べる
            char character = value.charAt(i); // 今調べている文字を取り出す
            switch (character) { // 特別な文字かどうかで処理を分ける
                case '"': escaped.append("\\\""); break; // 引用符をエスケープする
                case '\\': escaped.append("\\\\"); break; // バックスラッシュをエスケープする
                case '\n': escaped.append("\\n"); break; // 改行をエスケープする
                case '\r': escaped.append("\\r"); break; // 復帰文字をエスケープする
                case '\t': escaped.append("\\t"); break; // タブをエスケープする
                case '\b': escaped.append("\\b"); break; // バックスペースをエスケープする
                case '\f': escaped.append("\\f"); break; // フォームフィードをエスケープする
                default: // 上記以外の文字を処理する
                    if (character < 0x20) { // JSONでそのまま使えない制御文字か調べる
                        escaped.append(String.format("\\u%04x", (int) character)); // Unicode形式に変換する
                    } else { // 通常の文字の場合
                        escaped.append(character); // 文字をそのまま追加する
                    } // 制御文字の確認を終える
            } // 文字ごとの変換を終える
        } // 全文字の変換を終える
        return escaped.toString(); // 変換済みの文字列を返す
    } // JSONエスケープメソッドの終わり

    static void initializeDatabase() throws SQLException { // ★ 起動時にtodos表を準備する
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteへ接続する
             Statement statement = connection.createStatement()) { // ★ CREATE TABLEを実行する文を作る
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS todos ("
                    + "id INTEGER PRIMARY KEY, title TEXT, done INTEGER)"); // ★ 無い場合だけtodos表を作成する
        } // ★ 接続と文を閉じる
    } // ★ initializeDatabaseメソッドの終わり

    static void addTodo(String title) throws IOException { // ★ TodoをDBへ追加するメソッド
        String sql = "INSERT INTO todos (title, done) VALUES (?, 0)"; // ★ 追加用SQLを用意する
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteへ接続する
             PreparedStatement statement = connection.prepareStatement(sql)) { // ★ 値を渡すSQL文を準備する
            statement.setString(1, title); // ★ 1つ目の?にTodo名を設定する
            statement.executeUpdate(); // ★ INSERTを実行する
        } catch (SQLException e) { // ★ SQLエラーを受け取る
            throw new IOException(e); // ★ HTTP処理へエラーを伝える
        } // ★ SQL処理を終える
    } // ★ addTodoメソッドの終わり

    static void markDone(int id) throws IOException { // ★ Todoを完了状態にするメソッド
        String sql = "UPDATE todos SET done = 1 WHERE id = ?"; // ★ 完了更新用SQLを用意する
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteへ接続する
             PreparedStatement statement = connection.prepareStatement(sql)) { // ★ 値を渡すSQL文を準備する
            statement.setInt(1, id); // ★ 1つ目の?にTodo番号を設定する
            statement.executeUpdate(); // ★ UPDATEを実行する
        } catch (SQLException e) { // ★ SQLエラーを受け取る
            throw new IOException(e); // ★ HTTP処理へエラーを伝える
        } // ★ SQL処理を終える
    } // ★ markDoneメソッドの終わり

    static void deleteTodo(int id) throws IOException { // ★ TodoをDBから削除するメソッド
        String sql = "DELETE FROM todos WHERE id = ?"; // ★ 削除用SQLを用意する
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteへ接続する
             PreparedStatement statement = connection.prepareStatement(sql)) { // ★ 値を渡すSQL文を準備する
            statement.setInt(1, id); // ★ 1つ目の?にTodo番号を設定する
            statement.executeUpdate(); // ★ DELETEを実行する
        } catch (SQLException e) { // ★ SQLエラーを受け取る
            throw new IOException(e); // ★ HTTP処理へエラーを伝える
        } // ★ SQL処理を終える
    } // ★ deleteTodoメソッドの終わり

    static List<Todo> loadTodos() throws IOException { // ★ DBからTodo一覧を読み込むメソッド
        List<Todo> todos = new ArrayList<>(); // ★ 読み込んだTodoを格納する一覧
        String sql = "SELECT id, title, done FROM todos ORDER BY id"; // ★ 一覧取得用SQLを用意する
        try (Connection connection = DriverManager.getConnection(DB_URL); // ★ SQLiteへ接続する
             PreparedStatement statement = connection.prepareStatement(sql); // ★ SELECT文を準備する
             ResultSet result = statement.executeQuery()) { // ★ SELECTを実行して結果を受け取る
            while (result.next()) { // ★ 結果の行を1件ずつ読む
                Todo todo = new Todo(result.getInt("id"), result.getString("title")); // ★ IDとタイトルでTodoを作る
                todo.setDone(result.getInt("done") == 1); // ★ 完了状態を設定する
                todos.add(todo); // ★ 一覧へ追加する
            } // ★ 結果の全行を読み終える
        } catch (SQLException e) { // ★ SQLエラーを受け取る
            throw new IOException(e); // ★ HTTP処理へエラーを伝える
        } // ★ SQL処理を終える
        return todos; // ★ 読み込んだ一覧を返す
    } // ★ loadTodosメソッドの終わり
} // Appクラスの終わり

class Todo { // Todo1件分のデータ
    private final int id; // Todoの番号
    private final String title; // Todoのタイトル
    private boolean done; // 完了状態

    Todo(int id, String title) { // 番号とタイトルでTodoを作る
        this.id = id; // 番号を保存する
        this.title = title; // タイトルを保存する
        this.done = false; // 最初は未完了にする
    } // コンストラクタの終わり

    int getId() { // 番号を読み出す
        return id; // 番号を返す
    } // getIdの終わり

    String getTitle() { // タイトルを読み出す
        return title; // タイトルを返す
    } // getTitleの終わり

    boolean isDone() { // 完了状態を読み出す
        return done; // 完了状態を返す
    } // isDoneの終わり

    void setDone(boolean done) { // 完了状態を設定する
        this.done = done; // 状態を保存する
    } // setDoneの終わり
} // Todoクラスの終わり
