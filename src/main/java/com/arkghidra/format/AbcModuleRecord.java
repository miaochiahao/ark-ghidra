package com.arkghidra.format;

import java.util.Collections;
import java.util.List;

/**
 * Module record parsed from a special LiteralArray in the .abc file.
 *
 * <p>Module records encode the import/export metadata for a module (file).
 * They are stored as LiteralArrays whose offset is referenced by a
 * {@code moduleRecordIdx} field in the class data.
 *
 * <p>Binary layout (all fields little-endian):
 * <ul>
 *   <li>num_module_requests (uint32)</li>
 *   <li>module_requests[] (uint32 each - string table offsets)</li>
 *   <li>regular_import_num (uint32)</li>
 *   <li>regular_imports[] (see {@link RegularImport})</li>
 *   <li>namespace_import_num (uint32)</li>
 *   <li>namespace_imports[] (see {@link NamespaceImport})</li>
 *   <li>local_export_num (uint32)</li>
 *   <li>local_exports[] (see {@link LocalExport})</li>
 *   <li>indirect_export_num (uint32)</li>
 *   <li>indirect_exports[] (see {@link IndirectExport})</li>
 *   <li>star_export_num (uint32)</li>
 *   <li>star_exports[] (see {@link StarExport})</li>
 * </ul>
 */
public class AbcModuleRecord {
    private final List<Long> moduleRequestOffsets;
    private final List<RegularImport> regularImports;
    private final List<NamespaceImport> namespaceImports;
    private final List<LocalExport> localExports;
    private final List<IndirectExport> indirectExports;
    private final List<StarExport> starExports;
    private final long literalArrayOffset;

    /**
     * Constructs a module record.
     *
     * @param moduleRequestOffsets string table offsets for module request paths
     * @param regularImports regular import entries
     * @param namespaceImports namespace import entries
     * @param localExports local export entries
     * @param indirectExports indirect (re-export) entries
     * @param starExports star export entries
     * @param literalArrayOffset the offset of the source LiteralArray
     */
    public AbcModuleRecord(List<Long> moduleRequestOffsets,
            List<RegularImport> regularImports,
            List<NamespaceImport> namespaceImports,
            List<LocalExport> localExports,
            List<IndirectExport> indirectExports,
            List<StarExport> starExports,
            long literalArrayOffset) {
        this.moduleRequestOffsets = Collections.unmodifiableList(moduleRequestOffsets);
        this.regularImports = Collections.unmodifiableList(regularImports);
        this.namespaceImports = Collections.unmodifiableList(namespaceImports);
        this.localExports = Collections.unmodifiableList(localExports);
        this.indirectExports = Collections.unmodifiableList(indirectExports);
        this.starExports = Collections.unmodifiableList(starExports);
        this.literalArrayOffset = literalArrayOffset;
    }

    public List<Long> getModuleRequestOffsets() {
        return moduleRequestOffsets;
    }

    public List<RegularImport> getRegularImports() {
        return regularImports;
    }

    public List<NamespaceImport> getNamespaceImports() {
        return namespaceImports;
    }

    public List<LocalExport> getLocalExports() {
        return localExports;
    }

    public List<IndirectExport> getIndirectExports() {
        return indirectExports;
    }

    public List<StarExport> getStarExports() {
        return starExports;
    }

    public long getLiteralArrayOffset() {
        return literalArrayOffset;
    }

    /**
     * A regular import entry: {@code import { importName as localName } from 'module'}.
     */
    public static class RegularImport {
        private final long localNameOffset;
        private final long importNameOffset;
        private final int moduleRequestIdx;

        public RegularImport(long localNameOffset, long importNameOffset,
                int moduleRequestIdx) {
            this.localNameOffset = localNameOffset;
            this.importNameOffset = importNameOffset;
            this.moduleRequestIdx = moduleRequestIdx;
        }

        public long getLocalNameOffset() {
            return localNameOffset;
        }

        public long getImportNameOffset() {
            return importNameOffset;
        }

        public int getModuleRequestIdx() {
            return moduleRequestIdx;
        }
    }

    /**
     * A namespace import entry: {@code import * as localName from 'module'}.
     */
    public static class NamespaceImport {
        private final long localNameOffset;
        private final int moduleRequestIdx;

        public NamespaceImport(long localNameOffset, int moduleRequestIdx) {
            this.localNameOffset = localNameOffset;
            this.moduleRequestIdx = moduleRequestIdx;
        }

        public long getLocalNameOffset() {
            return localNameOffset;
        }

        public int getModuleRequestIdx() {
            return moduleRequestIdx;
        }
    }

    /**
     * A local export entry: {@code export { localName as exportName }}.
     */
    public static class LocalExport {
        private final long localNameOffset;
        private final long exportNameOffset;

        public LocalExport(long localNameOffset, long exportNameOffset) {
            this.localNameOffset = localNameOffset;
            this.exportNameOffset = exportNameOffset;
        }

        public long getLocalNameOffset() {
            return localNameOffset;
        }

        public long getExportNameOffset() {
            return exportNameOffset;
        }
    }

    /**
     * An indirect (re-)export entry:
     * {@code export { importName as exportName } from 'module'}.
     */
    public static class IndirectExport {
        private final long exportNameOffset;
        private final long importNameOffset;
        private final int moduleRequestIdx;

        public IndirectExport(long exportNameOffset, long importNameOffset,
                int moduleRequestIdx) {
            this.exportNameOffset = exportNameOffset;
            this.importNameOffset = importNameOffset;
            this.moduleRequestIdx = moduleRequestIdx;
        }

        public long getExportNameOffset() {
            return exportNameOffset;
        }

        public long getImportNameOffset() {
            return importNameOffset;
        }

        public int getModuleRequestIdx() {
            return moduleRequestIdx;
        }
    }

    /**
     * A star export entry: {@code export * from 'module'}.
     */
    public static class StarExport {
        private final int moduleRequestIdx;

        public StarExport(int moduleRequestIdx) {
            this.moduleRequestIdx = moduleRequestIdx;
        }

        public int getModuleRequestIdx() {
            return moduleRequestIdx;
        }
    }
}
