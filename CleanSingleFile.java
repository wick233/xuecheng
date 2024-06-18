import java.io.File;
import java.util.HashSet;

/**
 * @Descriptions:
 * @Author: Twithu
 * @Date: 2024/6/18 下午 03:42
 * @Version: 1.0
 */
public class CleanSingleFile {
    public static void main(String[] args) {

        String foldPath = args[0];

        File folder = new File(foldPath);
        String[] names = folder.list();

        HashSet<String> result = new HashSet<>();

        for (String name : names) {
            name = name.replace(".jpg", "").replace(".rw2", "");
            if (result.contains(name)) {
                result.remove(name);
            } else result.add(name);
        }

        for (String name : result){
            name = foldPath + "\\" + name + ".rw2";
            File file = new File(name);
            if (file.exists()) file.delete();
        }

        System.out.println("Success");

    }
}
