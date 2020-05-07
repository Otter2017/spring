import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class SpelTest {
    public static void main(String[] args) {
        testSpelStringIsEmpty();
    }

    public static class Animal {
        private String name;

        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Animal(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static void testSpel() {
        try {
            SpelExpressionParser parser = new SpelExpressionParser();
            JSONObject obj = new JSONObject();
            obj.put("name", "cat");
            obj.put("age", 3);
            String expressionString = "get('name').equals('cat') && get('age') ==3";
            Expression expression = parser.parseExpression(expressionString);
            Boolean result = expression.getValue(obj, Boolean.class);
            System.out.println(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void testSpelExpress() {
        try {
            SpelExpressionParser parser = new SpelExpressionParser();
            String expressionString = "T(java.lang.Math).random()";
            Expression expression = parser.parseExpression(expressionString);
            Double result = expression.getValue(Double.class);
            System.out.println(result);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void testSpelStringIsEmpty() {
        try {
            SpelExpressionParser parser = new SpelExpressionParser();
            String expressionString = "T(org.springframework.util.StringUtils).isEmpty('abc')";
            Expression expression = parser.parseExpression(expressionString);
            Boolean result = expression.getValue(Boolean.class);
            System.out.println(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public static void testSpelClassProperty() {
        try {
            SpelExpressionParser parser = new SpelExpressionParser();
            Animal animal = new Animal("cat", 3);
            String expressionString = "name.equals('cat') && age ==3";
            Expression expression = parser.parseExpression(expressionString);
            Boolean result = expression.getValue(animal, Boolean.class);
            System.out.println(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
