package eu.powet.SerialUSB.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import java.io.*;
import java.util.HashMap;


public class NativeLoader {

    public static synchronized SerialPortJNA getINSTANCE_SerialPort() {
        configureSerialPort();
        return INSTANCE_SerialPort;
    }

    private static SerialPortJNA INSTANCE_SerialPort = null;

    private static void configureSerialPort() {
        if (INSTANCE_SerialPort == null) {
            try {
                File folder = new File(System.getProperty("java.io.tmpdir") + File.separator + "kserial");
                if (folder.exists()) {
                    deleteOldFile(folder);
                }
                folder.mkdirs();
                String absolutePath = copyFileFromStream(getPath("serialposix.so"), folder.getAbsolutePath(), "serialposix" + getExtension());
                NativeLibrary.addSearchPath("serialposix", folder.getAbsolutePath());
                INSTANCE_SerialPort = (SerialPortJNA) Native.loadLibrary(absolutePath, SerialPortJNA.class, new HashMap());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getExtension() {
        if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            return ".so";
        }
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return ".dynlib";
        }
        return null;
    }

    public static String getPath(String lib) {
        if (System.getProperty("os.name").toLowerCase().contains("nux")) {
            if (is64()) {
                return "nix64/"+lib;
            } else {
                return "nix32/"+lib;
            }
        }
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return "osx/"+lib;
        }
        return null;
    }

    public static boolean is64() {
        String os = System.getProperty("os.arch").toLowerCase();
        return (os.contains("64"));
    }

    /* Utility fonctions */
    private static void deleteOldFile(File folder) {
        if (folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    deleteOldFile(f);
                }
            }
        }
        folder.delete();
    }

    private static String copyFileFromStream(String inputFile, String path, String targetName) throws IOException {
        InputStream inputStream = NativeLoader.class.getClassLoader().getResourceAsStream(inputFile);
        if (inputStream != null) {
            File copy = new File(path + File.separator + targetName);
            copy.deleteOnExit();
            OutputStream outputStream = new FileOutputStream(copy);
            byte[] bytes = new byte[1024];
            int length = inputStream.read(bytes);
            while (length > -1) {
                outputStream.write(bytes, 0, length);
                length = inputStream.read(bytes);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return copy.getAbsolutePath();
        }
        return null;
    }


}
