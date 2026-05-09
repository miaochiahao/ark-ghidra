package com.arkghidra.decompile;

/**
 * A placeholder expression for a definefunc instruction result.
 *
 * <p>When the decompiler encounters {@code definefunc methodIdx}, it
 * produces this expression. Later, when the result is stored to a
 * variable via {@code sta vN}, the {@link BlockInstructionProcessor}
 * converts it into an appropriate function expression (arrow function,
 * anonymous function, or generator function).
 *
 * <p>If not consumed by a store, it renders as a placeholder comment.
 */
class DefineFuncExpression extends ArkTSExpression {

    private final int methodIdx;

    /**
     * Constructs a definefunc expression.
     *
     * @param methodIdx the method index from the definefunc instruction
     */
    DefineFuncExpression(int methodIdx) {
        this.methodIdx = methodIdx;
    }

    /**
     * Returns the method index.
     *
     * @return the method index
     */
    int getMethodIdx() {
        return methodIdx;
    }

    @Override
    public String toArkTS() {
        return "func_" + methodIdx;
    }
}
