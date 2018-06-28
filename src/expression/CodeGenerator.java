package expression;

public class CodeGenerator
{
    private Lexer lexer;

    private final String[] registers = {"t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"};
    private int registerPointer;

    public CodeGenerator(Lexer lexer)
    {
        registerPointer = 0;
        this.lexer = lexer;
    }

    private String allocateRegister()
    {
        if (registerPointer >= registers.length)
        {
            System.out.println("Line: + " + lexer.getLineNumber() + "Your expression is too complicated: ");
            System.exit(-1);
        }

        String register = registers[registerPointer++];
        return register;
    }

    private void freeRegister()
    {
        if (registerPointer > 0)
            registerPointer--;
        else
        {
            System.out.println("Line: " + lexer.getLineNumber() + "Internal error: stack underflow: ");
            System.exit(-2);
        }
    }

    public void statements()
    {
        String allocatedRegister = allocateRegister();
        expression(allocatedRegister);

        while (!lexer.match(Lexer.EOI))
        {
            if (lexer.match(Lexer.SEMICOLON))
                lexer.getNextToken();
            else
                System.out.println("Line: " + lexer.getLineNumber() + ", missing \";\"");

            expression(allocatedRegister);
        }

        freeRegister();
    }

    private void expression(String allocatedRegister)
    {
        String allocatedRegister2;
        term(allocatedRegister);

        while (lexer.match(Lexer.PLUS))
        {
            lexer.getNextToken();
            allocatedRegister2 = allocateRegister();
            term(allocatedRegister2);
            System.out.println(allocatedRegister + " += " + allocatedRegister2);
            freeRegister();
        }
    }

    private void term(String allocatedRegister)
    {
        String allocatedRegister2;
        factor(allocatedRegister);

        while (lexer.match(Lexer.TIMES))
        {
            lexer.getNextToken();
            allocatedRegister2 = allocateRegister();
            factor(allocatedRegister2);
            System.out.println(allocatedRegister + " *= " + allocatedRegister2);
            freeRegister();
        }
    }

    private void factor(String allocatedRegister)
    {
        if (lexer.match(Lexer.NUMBER_OR_IDENTIFIER))
        {
            System.out.println(allocatedRegister + " += " + lexer.getSymbolText());
            lexer.getNextToken();
        }
        else if (lexer.match(Lexer.LEFT_PARENTHESES))
        {
            lexer.getNextToken();
            expression(allocatedRegister);

            if (lexer.match(Lexer.RIGHT_PARENTHESES))
                lexer.getNextToken();
            else
                System.out.println("Line: " + lexer.getLineNumber() + ", missing \")\"");
        }
        else
            System.out.println("Line: " + lexer.getLineNumber() + ", unexpected symbol: " + lexer.getSymbolText());
    }

    public static void main(String[] args)
    {
        Lexer lexer = new Lexer();
        CodeGenerator codeGen = new CodeGenerator(lexer);
        codeGen.statements();
    }
}
