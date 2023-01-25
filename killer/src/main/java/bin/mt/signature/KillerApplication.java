package bin.mt.signature;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class KillerApplication extends Application {
    public static final String URL = "https://github.com/L-JINBIN/ApkSignatureKillerEx";

    static {
        String packageName = "bin.mt.signature";
        String signatureData = "MIICwzCCAaugAwIBAgIERUjRgzANBgkqhkiG9w0BAQsFADASMRAwDgYDVQQDEwdBbmRyb2lkMB4X\n" +
                "DTIyMTIyNDE0NDkzMloXDTQ3MTIxODE0NDkzMlowEjEQMA4GA1UEAxMHQW5kcm9pZDCCASIwDQYJ\n" +
                "KoZIhvcNAQEBBQADggEPADCCAQoCggEBAKjVjd0eL4NPJW4uBR40hDkHtwdTQ7INP3hqgIs7U/kM\n" +
                "gck2MtNIFSPYJVDZKwuLWgZLAKSDu9607indxUWftfTwJ9ynUfzoVq39+RAkRqe/XnL5WdLM0v5H\n" +
                "CRtJW/nPBevunhJdoelCJJY1MsAl+WJCpSdfkkkeC0uXYeVYzCVwietIMfHEJOdEvjdXna0mdfuR\n" +
                "1NqA85K8RGj9FLEdOKy0ZnMQbHzCp1/FwJSXpOqAuoKsttrmAji7FfsqXVRhk+dTBBGybCzVtaDH\n" +
                "sIGyKzdsF2mKUPL3f0Q8XLKbkHRLmHGdVQlysIrrH7kn6Bx82cZTuYdPBUkrBO6w2NdMa+UCAwEA\n" +
                "AaMhMB8wHQYDVR0OBBYEFNL0ebiSTntg/5Hcar3/MEUdlYRHMA0GCSqGSIb3DQEBCwUAA4IBAQBg\n" +
                "v60JCBcJT+unHuVJge2wqEWjoUXV4JJG0Vn6kURbfiiC2rAtFOq6CFk+50HXyg2ZahosQ4ZPf8oT\n" +
                "yG1/+JQaw9QUvB4TtwwdCr9i9IvAjjAFT6ariY0bOJNJvTjsmHJMptjNFQt4DPdveuknQv3Ztemb\n" +
                "5BaxlpTegSZzL1ReOpKIygWf7qTqDnTtZsipt/OMttkn/dnhA9iiGJ5Jy+HLXQOc7+QgTYGPyAX5\n" +
                "2IcWd9l5OrWShpflwsNHsAAU5MMAO/sWR/F/7zxKa50Ve67ta/7rUOkkcD3D0taUBsUeAo6n6rSs\n" +
                "9Rk4tPEQRm59UJoof9cho7PxsMcTGb9UiNuJ\n";
        killPM(packageName, signatureData);
        killOpen(packageName);
    }

    private static void killPM(String packageName, String signatureData) {
        Signature fakeSignature = new Signature(Base64.decode(signatureData, Base64.DEFAULT));
        Parcelable.Creator<PackageInfo> originalCreator = PackageInfo.CREATOR;
        Parcelable.Creator<PackageInfo> creator = new Parcelable.Creator<PackageInfo>() {
            @Override
            public PackageInfo createFromParcel(Parcel source) {
                PackageInfo packageInfo = originalCreator.createFromParcel(source);
                if (packageInfo.packageName.equals(packageName)) {
                    if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                        packageInfo.signatures[0] = fakeSignature;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (packageInfo.signingInfo != null) {
                            Signature[] signaturesArray = packageInfo.signingInfo.getApkContentsSigners();
                            if (signaturesArray != null && signaturesArray.length > 0) {
                                signaturesArray[0] = fakeSignature;
                            }
                        }
                    }
                }
                return packageInfo;
            }

            @Override
            public PackageInfo[] newArray(int size) {
                return originalCreator.newArray(size);
            }
        };
        try {
            findField(PackageInfo.class, "CREATOR").set(null, creator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("Landroid/os/Parcel;", "Landroid/content/pm", "Landroid/app");
        }
        try {
            Object cache = findField(PackageManager.class, "sPackageInfoCache").get(null);
            //noinspection ConstantConditions
            cache.getClass().getMethod("clear").invoke(cache);
        } catch (Throwable ignored) {
        }
        try {
            Map<?, ?> mCreators = (Map<?, ?>) findField(Parcel.class, "mCreators").get(null);
            //noinspection ConstantConditions
            mCreators.clear();
        } catch (Throwable ignored) {
        }
        try {
            Map<?, ?> sPairedCreators = (Map<?, ?>) findField(Parcel.class, "sPairedCreators").get(null);
            //noinspection ConstantConditions
            sPairedCreators.clear();
        } catch (Throwable ignored) {
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class)) {
                    break;
                }
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw e;
        }
    }

    private static void killOpen(String packageName) {
        try {
            System.loadLibrary("SignatureKiller");
        } catch (Throwable e) {
            System.err.println("Load SignatureKiller library failed");
            return;
        }
        String apkPath = getApkPath(packageName);
        if (apkPath == null) {
            System.err.println("Get apk path failed");
            return;
        }
        File apkFile = new File(apkPath);
        File repFile = new File(getDataFile(packageName), "origin.apk");
        try (ZipFile zipFile = new ZipFile(apkFile)) {
            String name = "assets/SignatureKiller/origin.apk";
            ZipEntry entry = zipFile.getEntry(name);
            if (entry == null) {
                System.err.println("Entry not found: " + name);
                return;
            }
            if (!repFile.exists() || repFile.length() != entry.getSize()) {
                try (InputStream is = zipFile.getInputStream(entry); OutputStream os = new FileOutputStream(repFile)) {
                    byte[] buf = new byte[102400];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        os.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        hookApkPath(apkFile.getAbsolutePath(), repFile.getAbsolutePath());
    }

    @SuppressLint("SdCardPath")
    private static File getDataFile(String packageName) {
        String username = Environment.getExternalStorageDirectory().getName();
        if (username.matches("\\d+")) {
            File file = new File("/data/user/" + username + "/" + packageName);
            if (file.canWrite()) {
                return file;
            }
        }
        return new File("/data/data/" + packageName);
    }

    private static String getApkPath(String packageName) {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/maps"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split("\\s+");
                String path = arr[arr.length - 1];
                if (isApkPath(packageName, path)) {
                    return path;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isApkPath(String packageName, String path) {
        if (!path.startsWith("/") || !path.endsWith(".apk")) {
            return false;
        }
        String[] splitStr = path.substring(1).split("/", 6);
        int splitCount = splitStr.length;
        if (splitCount == 4 || splitCount == 5) {
            if (splitStr[0].equals("data") && splitStr[1].equals("app") && splitStr[splitCount - 1].equals("base.apk")) {
                return splitStr[splitCount - 2].startsWith(packageName);
            }
            if (splitStr[0].equals("mnt") && splitStr[1].equals("asec") && splitStr[splitCount - 1].equals("pkg.apk")) {
                return splitStr[splitCount - 2].startsWith(packageName);
            }
        } else if (splitCount == 3) {
            if (splitStr[0].equals("data") && splitStr[1].equals("app")) {
                return splitStr[2].startsWith(packageName);
            }
        } else if (splitCount == 6) {
            if (splitStr[0].equals("mnt") && splitStr[1].equals("expand") && splitStr[3].equals("app") && splitStr[5].equals("base.apk")) {
                return splitStr[4].endsWith(packageName);
            }
        }
        return false;
    }

    private static native void hookApkPath(String apkPath, String repPath);
}
