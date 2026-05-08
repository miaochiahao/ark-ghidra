package com.arkghidra.analyzer;

import ghidra.program.model.data.BooleanDataType;
import ghidra.program.model.data.CategoryPath;
import ghidra.program.model.data.DataType;
import ghidra.program.model.data.DataTypeConflictHandler;
import ghidra.program.model.data.DataTypeManager;
import ghidra.program.model.data.DoubleDataType;
import ghidra.program.model.data.FloatDataType;
import ghidra.program.model.data.IntegerDataType;
import ghidra.program.model.data.LongDataType;
import ghidra.program.model.data.PointerDataType;
import ghidra.program.model.data.StringDataType;
import ghidra.program.model.listing.Program;
import ghidra.util.Msg;
import ghidra.util.exception.DuplicateNameException;

/**
 * Creates ArkTS-specific data types in the program's data type manager.
 *
 * <p>Defines primitive types that correspond to the ArkTS runtime
 * representation and places them under a top-level "ArkTS" category
 * so they are easy to find in the Ghidra data type browser.</p>
 */
public class ArkTSDataTypeManager {

    private static final String OWNER = ArkTSDataTypeManager.class.getSimpleName();
    private static final CategoryPath ARKTS_CATEGORY = new CategoryPath("/ArkTS");

    private final Program program;

    public ArkTSDataTypeManager(Program program) {
        this.program = program;
    }

    /**
     * Creates all ArkTS data types in the program's data type manager.
     * Types that already exist are silently kept (KEEP_HANDLER with
     * conflict resolution that keeps the existing type).
     */
    public void createDataTypes() {
        DataTypeManager dtm = program.getDataTypeManager();
        int tx = program.startTransaction("Add ArkTS data types");
        try {
            addType(dtm, new BooleanDataType(dtm));
            addType(dtm, new IntegerDataType(dtm));
            addType(dtm, new LongDataType(dtm));
            addType(dtm, new FloatDataType(dtm));
            addType(dtm, new DoubleDataType(dtm));
            addType(dtm, new PointerDataType(new StringDataType(dtm)));

            Msg.info(OWNER, "ArkTS data types created successfully");
        } finally {
            program.endTransaction(tx, true);
        }
    }

    private void addType(DataTypeManager dtm, DataType baseType) {
        DataType dt = baseType.clone(dtm);
        try {
            dt.setCategoryPath(ARKTS_CATEGORY);
        } catch (DuplicateNameException e) {
            Msg.warn(OWNER, "Category path conflict for " + dt.getName(), e);
        }
        dtm.resolve(dt, DataTypeConflictHandler.KEEP_HANDLER);
    }
}
