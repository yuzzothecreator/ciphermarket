package com.ciphermarket.api.upload.scan;

import com.ciphermarket.api.config.ClamAvProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
public class ClamAvScanner {

    private final ClamAvProperties properties;

    public ClamAvScanner(ClamAvProperties properties) {
        this.properties = properties;
    }

    public ScanResult scan(InputStream input) {
        if (!properties.enabled()) {
            return ScanResult.skipped("ClamAV disabled");
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(properties.host(), properties.port()), 10_000);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            out.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read == 0) {
                    continue;
                }
                out.write(intToBigEndian(read));
                out.write(buffer, 0, read);
            }
            out.write(new byte[]{0, 0, 0, 0});
            out.flush();

            String response = readResponse(in);
            if (response.contains("OK")) {
                return ScanResult.clean();
            }
            if (response.contains("FOUND")) {
                return ScanResult.infected(response.trim());
            }
            return ScanResult.error(response.trim());
        } catch (IOException ex) {
            return ScanResult.error("ClamAV scan failed: " + ex.getMessage());
        }
    }

    private static byte[] intToBigEndian(int value) {
        return new byte[]{
                (byte) ((value >>> 24) & 0xFF),
                (byte) ((value >>> 16) & 0xFF),
                (byte) ((value >>> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    private static String readResponse(InputStream in) throws IOException {
        byte[] buf = new byte[2048];
        int len = in.read(buf);
        if (len <= 0) {
            return "";
        }
        return new String(buf, 0, len, StandardCharsets.US_ASCII);
    }
}
