import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます。【1】
import java.net.InetSocketAddress; // 接続先のポート番号を扱う機能を読み込みます。【1】
import java.net.URLDecoder; // URL形式の文字を元に戻す機能を読み込みます
import java.nio.charset.StandardCharsets; // UTF-8の文字コードを扱います
import java.util.ArrayList; // 複数のTodoを並べて入れる箱を使います // ★変更
import java.util.List; // Todoを並べて扱う型を使います // ★変更

public class App { // Appという名前のプログラムを定義します。【1】
    static List<Todo> todos = new ArrayList<>(); // Todoを保存するリストです // ★変更
    static int nextId = 1; // 次に振る番号です // ★変更

    public static void main(String[] args) throws Exception { // プログラムの開始地点です。【1】
        todos.add(new Todo(nextId++, "牛乳を買う")); // 起動時のサンプルTodoを追加します // ★変更
        Todo egg = new Todo(nextId++, "卵を買う"); // 起動時のサンプルTodoを作ります // ★変更
        egg.setDone(true); // 卵を買うTodoを完了にします // ★変更
        todos.add(egg); // 卵を買うTodoを追加します // ★変更
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。【1】
        server.createContext("/", exchange -> { // 「/」へのアクセスを受け取る処理を登録します。【1】
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String method = exchange.getRequestMethod(); // GETやPOSTなどの方法を取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 既定の返答形式をプレーンテキストにします
            if (path.equals("/add") && method.equals("POST")) { // POSTで/addに来たTodoを受け取ります
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた内容をUTF-8で読み取ります
                String value = body.substring(5); // 「todo=」の5文字を取り除きます
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // URL形式の日本語を元に戻します // ★変更
                if (!title.isEmpty()) { // 入力が空でないときだけ追加します // ★変更
                    todos.add(new Todo(nextId, title)); // Todoをリストに追加します // ★変更
                    nextId++; // 次に使う番号を進めます // ★変更
                }
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先を「/」に指定します
                exchange.sendResponseHeaders(303, -1); // 「/」へ移動する応答を送ります
                exchange.close(); // このリクエストの処理を閉じます
                return; // この分岐の処理を終えます
            } else if (path.equals("/")) { // パスが「/」ならフォームとTodo一覧を表示します
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // Todo入力フォームと一覧を用意します
                for (Todo todo : todos) { // Todoを1件ずつ取り出します // ★変更
                    String mark = ""; // 完了印を空にします // ★変更
                    if (todo.isDone()) { // Todoが完了しているか調べます // ★変更
                        mark = " ✔"; // 完了印を付けます // ★変更
                    }
                    html += "<li>" + todo.getTitle() + mark + "</li>"; // Todoを一覧項目として足します // ★変更
                } // 繰り返しを終えます
                html += "</ul>"; // 一覧を閉じます
                message = html; // 組み立てたHTMLを返す中身にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 「/」の返答形式をHTMLにします
            } else { // ほかのパスには見つからない旨を返します // ★変更
                message = "ページが見つかりません"; // ★変更
            }
            byte[] body = message.getBytes("UTF-8"); // 文字をUTF-8のデータに変換します。【毎】
            exchange.sendResponseHeaders(200, body.length); // 正常な応答であることとデータの長さを送ります。【毎】
            exchange.getResponseBody().write(body); // 文字のデータを送り返します。【毎】
            exchange.getResponseBody().close(); // 送り返す処理を終えます。【毎】
        }); // 「/」へのアクセス処理の登録を終えます。【1】
        server.start(); // サーバーの待ち受けを開始します。【1】
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動したことをターミナルに表示します。【1】
    } // mainメソッドの定義を終えます。【1】
} // Appクラスの定義を終えます。【1】

class Todo { // Todo1件の情報を持ちます // ★変更
    private final int id; // Todoの番号です // ★変更
    private final String title; // やることです // ★変更
    private boolean done; // 終わったかどうかです // ★変更

    Todo(int id, String title) { // 番号とやることを受け取ります // ★変更
        this.id = id; // 番号を保存します // ★変更
        this.title = title; // やることを保存します // ★変更
        this.done = false; // 最初は未完了にします // ★変更
    } // ★変更

    int getId() { // 番号を読み出します // ★変更
        return id; // ★変更
    } // ★変更

    String getTitle() { // やることを読み出します // ★変更
        return title; // ★変更
    } // ★変更

    boolean isDone() { // 完了したかを読み出します // ★変更
        return done; // ★変更
    } // ★変更

    void setDone(boolean done) { // 完了したかを書き換えます // ★変更
        this.done = done; // ★変更
    } // ★変更
} // ★変更
