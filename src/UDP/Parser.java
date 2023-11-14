import java.io.IOException;

public class Parser {
    private int result;
    private int pos = -1;
    private int ch;
    private final String input;

    public Parser(String inputString){
        input = inputString;
    }
    public int getResult(){
        return result;
    }

    private void nextChar(){
        if (++pos < input.length()){
            ch = input.charAt(pos);
        } else {
            ch = -1;
        }
    }

    private boolean pop(int inputCh){
        while(inputCh == ' '){
            nextChar();
        }
        if(inputCh == ch){
            nextChar();
            return true;
        } else {
            return false;
        }
    }
    public double solve(){
        nextChar();
        double x = parseExpression();
        if(pos < input.length()) throw new RuntimeException("Unexpected: " + (char)ch);
        return x;
    }

    private double parseExpression(){
        double x = parseTerm();
        while(true){
            if(pop('+')){
                x += parseTerm();
            } else if(pop('-')){
                x -= parseTerm();
            } else return x;
        }
    }

    private double parseTerm(){
        double x = parseFactor();
        while(true){
            if (pop('*')) {
                x *= parseFactor();
            } else if(pop('/')){
                x /= parseFactor();
            } else return x;
        }
    }

    private double parseFactor(){
        if(pop('+')) return +parseFactor();
        if(pop('-')) return -parseFactor();

        double x;
        int startPos = this.pos;
        if(pop('(')){
            x = parseExpression();
            if(!pop(')')){
                throw new RuntimeException("Missing ')'");
            } else if ((ch >= '0' && ch <= '9') || ch == '.'){
                while((ch >= '0' && ch <= '9') || ch =='.'){
                    nextChar();
                }
                x = Double.parseDouble(input.substring(startPos, this.pos));
            }
        }
        return x;
    }
}
