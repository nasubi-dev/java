package TroubleShootQuiz1.src;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CSVFileConverter {
    public static void main(String[] args) {

        String filepath = "personal_information.csv";
        Charset charset = Charset.forName("Shift-JIS");

        // 読み込んだCSVデータの格納先
        List<PersonalInfo> data = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Path.of(filepath), charset)) {
            // 先頭の行は列タイトルなので読み込んで捨てる
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                //DEBUG: System.out.println(line);
                PersonalInfo pi = getPersonalInfo(line);
                data.add(pi);
            }
        } catch (IOException e) {
            e.getStackTrace();
        }

        // データの並び替えを行う
        data.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.nameKana(), b.nameKana()));

        // CSVファイルの変換結果を出力しつつ、出力用のファイルのデータを生成する
        StringBuilder outputData = new StringBuilder("氏名,住所,年齢,血液型");
        System.out.println("氏名,住所,年齢,血液型");
        outputData.append(System.lineSeparator());
        data.forEach((pi) -> {
            outputData.append(pi.toCSVRow());
            outputData.append(System.lineSeparator());

            System.out.println(pi.toCSVRow());
        });

        // ファイル書き込み
        try {
            Files.writeString(Path.of("output.csv"), outputData.toString(), Charset.defaultCharset());
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private static PersonalInfo getPersonalInfo(String line) {
        String[] cells = line.split(",");

        PersonalInfo pi = new PersonalInfo(
                cells[1],
                cells[2],
                cells[3],
                cells[4],
                cells[5],
                cells[6],
                cells[7],
                cells[8],
                cells[9],
                Integer.parseInt(cells[10]),
                cells[11]
        );
        return pi;
    }
}
