package expression;

import java.io.*;

/**
 * The {@link CodeGenerator} class represents a simple code generator for the expression containing only numbers,
 * operators such as "+", "*" and parentheses and ending with ";".
 * */
public class CodeGenerator
{
    /** The register stack of this simple compiler. */
    private final String[] registers = { "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7" };

    /** The lexer to tokenize text in the input stream. */
    private Lexer lexer;

    /** The pointer that points the next register can be allocated. */
    private int registerPointer;

    /**
     * Initializes a new instance of {@link CodeGenerator} with the specified {@link Lexer}.
     * @param lexer The specified {@link Lexer}.
     * */
    public CodeGenerator(Lexer lexer)
    {
        registerPointer = 0;
        this.lexer = lexer;
    }

    /**
     * Allocates a new register from the register stack.
     * */
    private String allocateRegister()
    {
        if (registerPointer >= registers.length)
        {
            System.out.println("Line: " + lexer.getLineNumber() + ", the expression is too complicated.");
            System.exit(-1);
        }

        return registers[registerPointer++];
    }

    /**
     * Frees a register to the register stack.
     * */
    private void freeRegister()
    {
        if (registerPointer > 0)
            registerPointer--;
        else
        {
            System.out.println("Line: " + lexer.getLineNumber() + ", internal error: stack underflow.");
            System.exit(-2);
        }
    }

    /**
     * Parses a "statements" with following production and generate corresponding target code.
     * <p>
     * statements -> expression ; | expression ; statements
     * <p/>
     * */
    public void statements()
    {
        String allocatedRegister = allocateRegister();

        while (!lexer.match(TokenType.END_OF_FILE))
        {
            expression(allocatedRegister);
            if (lexer.match(TokenType.SEMICOLON))
                lexer.lookNextToken();
            else if (lexer.match(TokenType.UNKNOWN_SYMBOL) ||
                    lexer.match(TokenType.MINUS) ||
                    lexer.match(TokenType.DIVIDE))
            {
                System.out.println("Error: unknown symbol: " + lexer.getSymbolText() + " at line: " + lexer.getLineNumber() + ".");
                lexer.clearLine();
            }
            else
                System.out.println("Line: " + lexer.getLineNumber() + ", missing \";\"");
        }

        freeRegister();
    }

    /**
     * Parses an "expression" with following production and generate corresponding target code.
     * <p>
     * expression -> term addExpression
     * <p/>
     * while
     * <p>
     * addExpression -> + term addExpression | Empty
     * <p/>
     * */
    private void expression(String allocatedRegister)
    {
        String allocatedRegister2;
        term(allocatedRegister);

        while (lexer.match(TokenType.PLUS))
        {
            lexer.lookNextToken();
            allocatedRegister2 = allocateRegister();
            term(allocatedRegister2);
            System.out.println(allocatedRegister + " += " + allocatedRegister2);
            freeRegister();
        }
    }

    /**
     * Parses an "term" with following production and generate corresponding target code.
     * <p>
     * term -> factor * term
     * <p/>
     * */
    private void term(String allocatedRegister)
    {
        String allocatedRegister2;
        factor(allocatedRegister);

        while (lexer.match(TokenType.TIMES))
        {
            lexer.lookNextToken();
            allocatedRegister2 = allocateRegister();
            factor(allocatedRegister2);
            System.out.println(allocatedRegister + " *=" + allocatedRegister2);
            freeRegister();
        }
    }

    /**
     * Parses a "factor" with following production and generate corresponding target code.
     * <p>
     * factor -> NUMBER_OR_ID | ( expression )
     * <p/>
     * */
    private void factor(String allocatedRegister)
    {
        if (lexer.match(TokenType.NUMBER_OR_IDENTIFIER))
        {
            System.out.println(allocatedRegister + " = " + lexer.getSymbolText());
            lexer.lookNextToken();
        }
        else if (lexer.match(TokenType.LEFT_PARENTHESES))
        {
            lexer.lookNextToken();
            expression(allocatedRegister);

            if (lexer.match(TokenType.RIGHT_PARENTHESES))
                lexer.lookNextToken();
            else
                System.out.println("Line: " + lexer.getLineNumber() + ", missing\")\"");
        }
        else
        {
            System.out.println("Line: " + lexer.getLineNumber() + ", unexpected symbol: " + lexer.getSymbolText());
            lexer.clearLine();
        }
    }

    /**
     * A unit test method for the {@link CodeGenerator} class.
     * */
    public static void main(String[] args) throws IOException
    {
        String sourceFilePath = "./out/production/CCompiler/expression/parserTest.txt";
        FileInputStream sourceFile = new FileInputStream(sourceFilePath);
        Lexer lexer = new Lexer(sourceFile);
        CodeGenerator codeGen = new CodeGenerator(lexer);
        codeGen.statements();
        lexer.close();
        sourceFile.close();
    }
}
