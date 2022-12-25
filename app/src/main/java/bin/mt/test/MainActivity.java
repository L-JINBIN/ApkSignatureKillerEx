package bin.mt.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {

    public static class App extends Application {
        @Override
        protected void attachBaseContext(Context base) {
            new bin.mt.signature.KillerApplication(); // 注释掉这句即可关闭过签
            super.attachBaseContext(base);
        }
    }

    static {
        System.loadLibrary("test");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView msg = findViewById(R.id.msg);

        // 以下演示了三种获取签名MD5的方式

        String signatureExpected = "3bf8931788824c6a1f2c6f6ff80f6b21";
        String signatureFromAPI = md5(signatureFromAPI());
        String signatureFromAPK = md5(signatureFromAPK());
        String signatureFromSVC = md5(signatureFromSVC());

        // 开启过签后，API与APK方式会获取到虚假的签名MD5

        // 而SVC方式总是能获取到真实的签名MD5

        SpannableStringBuilder sb = new SpannableStringBuilder();
        append(sb, "Expected: ", signatureExpected, Color.BLACK);
        append(sb, "From API: ", signatureFromAPI, signatureExpected.equals(signatureFromAPI) ? Color.BLUE : Color.RED);
        append(sb, "From APK: ", signatureFromAPK, signatureExpected.equals(signatureFromAPK) ? Color.BLUE : Color.RED);
        append(sb, "From SVC: ", signatureFromSVC, signatureExpected.equals(signatureFromSVC) ? Color.BLUE : Color.RED);

        // 当然SVC并非绝对安全，只是相对而言更加可靠，实际运用还需结合更多的手段

        msg.setText(sb);
    }

    private static void append(SpannableStringBuilder sb, String header, String value, int color) {
        int start = sb.length();
        sb.append(header).append(value).append("\n");
        int end = sb.length();
        sb.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private byte[] signatureFromAPI() {
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            return info.signatures[0].toByteArray();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] signatureFromAPK() {
        try (ZipFile zipFile = new ZipFile(getPackageResourcePath())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().matches("(META-INF/.*)\\.(RSA|DSA|EC)")) {
                    InputStream is = zipFile.getInputStream(entry);
                    CertificateFactory certFactory = CertificateFactory.getInstance("X509");
                    X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(is);
                    return x509Cert.getEncoded();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] signatureFromSVC() {
        try (ParcelFileDescriptor fd = ParcelFileDescriptor.adoptFd(openAt(getPackageResourcePath()));
             ZipInputStream zis = new ZipInputStream(new FileInputStream(fd.getFileDescriptor()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().matches("(META-INF/.*)\\.(RSA|DSA|EC)")) {
                    CertificateFactory certFactory = CertificateFactory.getInstance("X509");
                    X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(zis);
                    return x509Cert.getEncoded();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String md5(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(bytes);
            String hexDigits = "0123456789abcdef";
            char[] str = new char[digest.length * 2];
            int k = 0;
            for (byte b : digest) {
                str[k++] = hexDigits.charAt(b >>> 4 & 0xf);
                str[k++] = hexDigits.charAt(b & 0xf);
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static native int openAt(String path);

}