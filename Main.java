
import java.util.ArrayList; // Listを使うための道具を読み込みます（複数の値を並べて扱えます）
import java.util.List; // Listという型を使うための道具を読み込みます

public class Main { // Mainという名前のクラス（プログラムをまとめる箱）を作ります
    public static void main(String[] args) {
        List<Todo> todos = new ArrayList<>(); // Todoを2件入れるList（複数の値を並べて持つもの）を作ります
        todos.add(new Todo("牛乳を買う", true)); // 未完了のTodoを1件追加します
        todos.add(new Todo("ゴミを出す", true)); // 完了済みのTodoを1件追加します
        todos.add(new Todo("パンを買う", false));
        for (Todo todo : todos) { // ListからTodoを1件ずつ取り出して繰り返します
            System.out.println(todo.toItem()); // toItem()で作った文字列をターミナルに1行で表示します
        }
    }
}

class Todo { // Todoという名前のクラス（Todoの情報と処理をまとめる箱）を作ります
    private String title; // title（Todoの題名）を保存する場所です
    private boolean done; // done（済んだかどうか）を保存する場所です

    Todo(String title, boolean done) { // titleとdoneを受け取って、このTodoを作ります
        this.title = title; // 受け取った題名をこのTodoのtitleに保存します
        this.done = done; // 受け取った完了状態をこのTodoのdoneに保存します
    }

    String toItem() { // Todoを<li>...</li>の形の文字列に変えるメソッド（処理）です
        if (done) { // doneがtrue（済んでいる）なら、[済]を付けた行を返します
            return "<li>[完了] " + title + "</li>"; // [完了]付きの文字列を返します
        } else {
            return "<li>" + title + "</li>";
        }
        // toItem()の処理はここまでです
    }
    // Todoクラスはここまでです
}