package dev.daweapon.data.format;

import dev.daweapon.data.DataFormat;
import dev.daweapon.data.DataReader;
import dev.daweapon.data.DataWriter;

public class BinaryFormat implements DataFormat {
    @Override
    public DataReader createReader(byte[] data) {
        return new BinaryDataReader(data);
    }

    @Override
    public DataWriter createWriter() {
        return new BinaryDataWriter();
    }

    @Override
    public byte[] toBytes(DataWriter writer) {
        if (!(writer instanceof BinaryDataWriter binaryWriter)) {
            throw new IllegalArgumentException("Expected BinaryDataWriter");
        }
        return binaryWriter.toByteArray();
    }
}
