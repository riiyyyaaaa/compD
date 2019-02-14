import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


//プロパティファイルから設定を読み出す
//resourseフォルダにpropertiesファイルを入れておく
public class PropertyUtil {
    private static final String cd = new File(".").getAbsoluteFile().getParent();
    private static final String INIT_FILE_PATH = cd + "\\src\\resourse\\compDetection.properties";
    private static final Properties properties;

    private PropertyUtil() throws Exception {
    }

    static {
        properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream((INIT_FILE_PATH));
            properties.load(inputStream);
        }catch (IOException e) {
            System.out.println(String.format("fail to read file"));
        }
    }

    public static String getProperty(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getProperty(final String key) {
        return getProperty(key, "");
    }
}
