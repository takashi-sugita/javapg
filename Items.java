public class Items {
    public static void main(String[] args) {
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除する" };
        boolean[] done = { true, false, false, false };

        for (int i = 0; i < todos.length; i++) {
            if (!todos[i].isEmpty()) {
                if (done[i]) {
                    System.out.println("<li>[済] " + todos[i] + "</li>");
                } else {
                    System.out.println("<li>" + todos[i] + "</li>");
                }
            }
        }
    }
}