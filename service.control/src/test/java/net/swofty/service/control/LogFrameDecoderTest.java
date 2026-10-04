package net.swofty.service.control;

import net.swofty.service.control.docker.LogFrameDecoder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogFrameDecoderTest {

    private static byte[] frame(int streamType, String payload) {
        byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(streamType);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write((body.length >> 24) & 0xFF);
        out.write((body.length >> 16) & 0xFF);
        out.write((body.length >> 8) & 0xFF);
        out.write(body.length & 0xFF);
        out.writeBytes(body);
        return out.toByteArray();
    }

    private static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }

    @Test
    void decodesMultiplexedFrames() {
        byte[] stream = concat(frame(1, "hello\n"), frame(2, "a warning\n"));
        assertEquals("hello\na warning\n", LogFrameDecoder.decode(stream));
    }

    @Test
    void passesThroughUnframedTtyOutput() {
        String plain = "plain tty output with no framing\n";
        assertEquals(plain, LogFrameDecoder.decode(plain.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void handlesEmptyInput() {
        assertEquals("", LogFrameDecoder.decode(new byte[0]));
        assertEquals("", LogFrameDecoder.decode(null));
    }

    @Test
    void recoversContentFromATruncatedFinalFrame() {
        byte[] good = frame(1, "complete line\n");
        byte[] truncated = concat(good, new byte[]{1, 0, 0, 0, 0, 0, 0, (byte) 200}, "cut off".getBytes(StandardCharsets.UTF_8));
        String decoded = LogFrameDecoder.decode(truncated);
        assertTrue(decoded.contains("cut off"), "truncated tail should still be surfaced: " + decoded);
    }

    @Test
    void treatsTextThatCoincidentallyStartsLowAsPlainText() {
        // A log line beginning with a byte in the 0-2 range must not be mistaken for a frame
        // header when the rest of the stream does not follow the framing.
        byte[] misleading = concat(new byte[]{1, 0, 0, 0, 0, 0, 0, 5}, "not-five-bytes-long".getBytes(StandardCharsets.UTF_8));
        String decoded = LogFrameDecoder.decode(misleading);
        assertTrue(decoded.contains("not-five"), "should not silently drop content: " + decoded);
    }
}
