public class Item { // Itemという名前のクラス（プログラムのまとまり）を作ります
    public static void main(String[] args) { // プログラムを実行したとき最初に動く場所です
        String title = "コーヒー豆を買う"; // titleという文字列（文字の並び）に「牛乳を買う」を入れます
        String html = "<li>" + title + "</li>"; // +で文字列をつなぎ、1行分のHTML（Webページ用の記述）を作ります
        System.out.println(html); // 作った文字列をターミナルに1行表示します
        boolean done = false;
        System.out.println(done);
        int count = 3;
        System.out.println("いま" + count + "件");
    } // mainメソッド（処理のまとまり）の終わりです
} // Itemクラスの終わりです
