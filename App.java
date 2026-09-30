import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます。【1】
import java.net.InetSocketAddress; // 接続先のポート番号を扱う機能を読み込みます。【1】

public class App { // Appという名前のプログラムを定義します。【1】
    public static void main(String[] args) throws Exception { // プログラムの開始地点です。【1】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。【1】
        server.createContext("/", exchange -> { // 「/」へのアクセスを受け取る処理を登録します。【1】
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String message;
            if (path.equals("/hello")) { // パスが「/hello」か比べます
                message = "こんにちは！";
            } else if (path.equals("/bye")) { // パスが「/bye」か比べます
                message = "さようなら！";
            } else if (path.equals("/menu")) { // パスが「/menu」か比べます
                message = "今日の定食はカレー";
            } else {
                message = "ページが見つかりません";
            }
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 返す文字の形式と文字コードを指定します。【毎】
            byte[] body = message.getBytes("UTF-8"); // 文字をUTF-8のデータに変換します。【毎】
            exchange.sendResponseHeaders(200, body.length); // 正常な応答であることとデータの長さを送ります。【毎】
            exchange.getResponseBody().write(body); // 文字のデータを送り返します。【毎】
            exchange.getResponseBody().close(); // 送り返す処理を終えます。【毎】
        }); // 「/」へのアクセス処理の登録を終えます。【1】
        server.start(); // サーバーの待ち受けを開始します。【1】
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動したことをターミナルに表示します。【1】
    } // mainメソッドの定義を終えます。【1】
} // Appクラスの定義を終えます。【1】
