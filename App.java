import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます。
import java.net.InetSocketAddress; // 接続先のポート番号を扱う機能を読み込みます。

public class App { // Appという名前のプログラムを定義します。
    public static void main(String[] args) throws Exception { // プログラムの開始地点です。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。
        server.createContext("/", exchange -> { // 「/」へのアクセスを受け取る処理を登録します。
            String message = "サーバーが起動しました！"; // ブラウザに返す文字を用意します。
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 返す文字の形式と文字コードを指定します。
            byte[] body = message.getBytes("UTF-8"); // 文字をUTF-8のデータに変換します。
            exchange.sendResponseHeaders(200, body.length); // 正常な応答であることとデータの長さを送ります。
            exchange.getResponseBody().write(body); // 文字のデータを送り返します。
            exchange.getResponseBody().close(); // 送り返す処理を終えます。
        }); // 「/」へのアクセス処理の登録を終えます。
        server.start(); // サーバーの待ち受けを開始します。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動したことをターミナルに表示します。
    } // mainメソッドの定義を終えます。
} // Appクラスの定義を終えます。
