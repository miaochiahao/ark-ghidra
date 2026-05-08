package com.arkghidra.format;

/**
 * Header of a Panda .abc file.
 * Located at offset 0, 4-byte aligned.
 */
public class AbcHeader {
    public static final byte[] MAGIC = {'P', 'A', 'N', 'D', 'A', 0, 0, 0};
    public static final int HEADER_SIZE = 8 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4;
    public static final int VERSION_V2 = 2;

    private final byte[] magic;
    private final int checksum;
    private final byte[] version;
    private final long fileSize;
    private final long foreignOff;
    private final long foreignSize;
    private final long numClasses;
    private final long classIdxOff;
    private final long numLnps;
    private final long lnpIdxOff;
    private final long numLiteralArrays;
    private final long literalArrayIdxOff;
    private final long numIndexRegions;
    private final long indexSectionOff;

    public AbcHeader(byte[] magic, int checksum, byte[] version, long fileSize,
            long foreignOff, long foreignSize, long numClasses, long classIdxOff,
            long numLnps, long lnpIdxOff, long numLiteralArrays,
            long literalArrayIdxOff, long numIndexRegions, long indexSectionOff) {
        this.magic = magic;
        this.checksum = checksum;
        this.version = version;
        this.fileSize = fileSize;
        this.foreignOff = foreignOff;
        this.foreignSize = foreignSize;
        this.numClasses = numClasses;
        this.classIdxOff = classIdxOff;
        this.numLnps = numLnps;
        this.lnpIdxOff = lnpIdxOff;
        this.numLiteralArrays = numLiteralArrays;
        this.literalArrayIdxOff = literalArrayIdxOff;
        this.numIndexRegions = numIndexRegions;
        this.indexSectionOff = indexSectionOff;
    }

    public boolean hasValidMagic() {
        if (magic.length != MAGIC.length) {
            return false;
        }
        for (int i = 0; i < MAGIC.length; i++) {
            if (magic[i] != MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    public int getVersionNumber() {
        return ((version[0] - '0') * 1000) + ((version[1] - '0') * 100)
                + ((version[2] - '0') * 10) + (version[3] - '0');
    }

    public byte[] getMagic() {
        return magic;
    }
    public int getChecksum() {
        return checksum;
    }
    public byte[] getVersion() {
        return version;
    }
    public long getFileSize() {
        return fileSize;
    }
    public long getForeignOff() {
        return foreignOff;
    }
    public long getForeignSize() {
        return foreignSize;
    }
    public long getNumClasses() {
        return numClasses;
    }
    public long getClassIdxOff() {
        return classIdxOff;
    }
    public long getNumLnps() {
        return numLnps;
    }
    public long getLnpIdxOff() {
        return lnpIdxOff;
    }
    public long getNumLiteralArrays() {
        return numLiteralArrays;
    }
    public long getLiteralArrayIdxOff() {
        return literalArrayIdxOff;
    }
    public long getNumIndexRegions() {
        return numIndexRegions;
    }
    public long getIndexSectionOff() {
        return indexSectionOff;
    }
}
