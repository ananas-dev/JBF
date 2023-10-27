package io.ananas.jbf;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static io.ananas.jbf.TokenType.*;

import java.util.List;

public class Compiler {
    private static final int MEMORY_VAR_INDEX = 1;
    private static final int IPTR_VAR_INDEX = 2;
    private static final int SCANNER_VAR_INDEX = 3;

    private final List<Token> tokens;
    private final ClassWriter cw;
    private MethodVisitor mv;
    private int current = 0;

    public Compiler(List<Token> tokens) {
        this.tokens = tokens;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    }

    public byte[] compile() {
        beginCode();

        // Set up the memory
        prelude();

        while (!isAtEnd()) {
            instruction();
        }

        endCode();

        return cw.toByteArray();
    }

    private void beginCode() {
        cw.visit(V1_8, ACC_PUBLIC, "BFMain", null, "java/lang/Object", null);
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC,
                "main", "([Ljava/lang/String;)V", null, null);

        mv.visitCode();
    }

    private void endCode() {
        mv.visitInsn(RETURN);
        mv.visitMaxs(0 ,0);
        mv.visitEnd();
        cw.visitEnd();
    }

    private void prelude() {
        // Create array
        mv.visitLdcInsn(30000);
        mv.visitIntInsn(NEWARRAY, T_BYTE);
        mv.visitVarInsn(ASTORE, MEMORY_VAR_INDEX);

        // Create instruction pointer
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, IPTR_VAR_INDEX);

        // Create scanner
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        mv.visitVarInsn(ASTORE, SCANNER_VAR_INDEX); // Store the Scanner object in local variable 1
    }

    private void instruction() {
        if (match(ADD))        { addInstruction();       return; }
        if (match(SUB))        { subInstruction();       return; }
        if (match(RIGHT))      { rightInstruction();     return; }
        if (match(LEFT))       { leftInstruction();      return; }
        if (match(WRITE))      { writeInstruction();     return; }
        if (match(READ))       { readInstruction();      return; }
        if (match(BEGIN_LOOP)) { beginLoopInstruction(); return; }

        throw new RuntimeException("Unknown instruction: " + peek());
    }

    private void addInstruction() {
        loadVars();
        mv.visitInsn(DUP2);
        mv.visitInsn(BALOAD);

        int value = 1;
        while (match(ADD)) value++;

        mv.visitLdcInsn(value);
        mv.visitInsn(IADD);
        mv.visitInsn(I2B);
        mv.visitInsn(BASTORE);
    }

    private void subInstruction() {
        loadVars();
        mv.visitInsn(DUP2);
        mv.visitInsn(BALOAD);

        int value = 1;
        while (match(SUB)) value++;

        mv.visitLdcInsn(value);
        mv.visitInsn(ISUB);
        mv.visitInsn(I2B);
        mv.visitInsn(BASTORE);
    }

    private void leftInstruction() {
        if (!check(LEFT)) {
            mv.visitIincInsn(IPTR_VAR_INDEX, 1);
            return;
        }

        mv.visitVarInsn(ILOAD, IPTR_VAR_INDEX);

        int value = 1;
        while (match(LEFT)) value++;

        mv.visitLdcInsn(value);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, IPTR_VAR_INDEX);
    }

    private void rightInstruction() {
        if (!check(RIGHT)) {
            mv.visitIincInsn(IPTR_VAR_INDEX, -1);
            return;
        }

        mv.visitVarInsn(ILOAD, IPTR_VAR_INDEX);

        int value = 1;
        while (match(RIGHT)) value++;

        mv.visitLdcInsn(value);
        mv.visitInsn(ISUB);
        mv.visitVarInsn(ISTORE, IPTR_VAR_INDEX);
    }

    private void writeInstruction() {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System",
                "out", "Ljava/io/PrintStream;");
        loadVars();
        mv.visitInsn(BALOAD);
        mv.visitInsn(I2C);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
                "print", "(C)V", false);
    }

    private void readInstruction() {
        loadVars();

        mv.visitVarInsn(ALOAD, SCANNER_VAR_INDEX);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner",
                "next", "()Ljava/lang/String;", false);

        // Get the first character of the string
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String",
                "charAt", "(I)C", false);

        mv.visitInsn(I2B);
        mv.visitInsn(BASTORE);
    }

    private void beginLoopInstruction() {
        Label start = new Label();
        Label end = new Label();

        mv.visitLabel(start);

        // Loop condition
        loadVars();
        mv.visitInsn(BALOAD);
        mv.visitJumpInsn(IFEQ, end);

        // Loop code
        while (!match(END_LOOP)) {
            instruction();
        }

        mv.visitJumpInsn(GOTO, start);

        mv.visitLabel(end);
    }

    private void loadVars() {
        mv.visitVarInsn(ALOAD, MEMORY_VAR_INDEX);
        mv.visitVarInsn(ILOAD, IPTR_VAR_INDEX);
    }


    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

}

