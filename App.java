import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます。【1】
import java.net.InetSocketAddress; // 接続先のポート番号を扱う機能を読み込みます。【1】
import java.net.URLDecoder; // URL形式の文字を元に戻す機能を読み込みます
import java.nio.charset.StandardCharsets; // UTF-8の文字コードを扱います
import java.util.ArrayList; // 複数の文字列を並べて入れる箱を使います
import java.util.List; // 文字列を並べて扱う型を使います

public class App { // Appという名前のプログラムを定義します。【1】
    public static void main(String[] args) throws Exception { // プログラムの開始地点です。【1】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。【1】
        List<String> todos = new ArrayList<>(); // 追加したTodoを保存するリストを用意します
        server.createContext("/", exchange -> { // 「/」へのアクセスを受け取る処理を登録します。【1】
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String method = exchange.getRequestMethod(); // GETやPOSTなどの方法を取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 既定の返答形式をプレーンテキストにします
            if (path.equals("/add") && method.equals("POST")) { // POSTで/addに来たTodoを受け取ります
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた内容をUTF-8で読み取ります
                String value = body.substring(5); // 「todo=」の5文字を取り除きます
                String todo = URLDecoder.decode(value, StandardCharsets.UTF_8); // URL形式の日本語を元に戻します
                if (!todo.isEmpty()) { // 入力が空でないときだけ追加します
                    todos.add(todo); // Todoをリストに追加します
                }
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先を「/」に指定します
                exchange.sendResponseHeaders(303, -1); // 「/」へ移動する応答を送ります
                exchange.close(); // このリクエストの処理を閉じます
                return; // この分岐の処理を終えます
            } else if (path.equals("/")) { // パスが「/」ならフォームとTodo一覧を表示します
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // Todo入力フォームと一覧を用意します
                for (String todo : todos) { // Todoを1件ずつ取り出します
                    html += "<li>" + todo + "</li>"; // Todoを一覧項目として足します
                } // 繰り返しを終えます
                html += "</ul>"; // 一覧を閉じます
                message = html; // 組み立てたHTMLを返す中身にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // 「/」の返答形式をHTMLにします
            } else if (path.equals("/hello")) { // パスが「/hello」か比べます
                String query = exchange.getRequestURI().getRawQuery(); // URLの「?」より後ろを取り出します
                String name = query == null || query.equals("name=") ? "ゲスト"
                        : URLDecoder.decode(query.substring(5), "UTF-8"); // 名前がない場合は「ゲスト」、ある場合はURL形式から戻します
                System.out.println("name = " + name);
                message = "こんにちは、" + name + "さん！"; // 名前を挨拶に加えます
            } else if (path.equals("/bye")) { // パスが「/bye」か比べます
                message = "さようなら！";
            } else if (path.equals("/menu")) { // パスが「/menu」か比べます
                message = "今日の定食はカレー";
            } else {
                message = "ページが見つかりません";
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
