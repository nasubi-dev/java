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
        int processedCount = 0;
        try (BufferedReader br = Files.newBufferedReader(Path.of(filepath), charset)) {
            // 先頭の行は列タイトルなので読み込んで捨てる
            br.readLine();

            String line;
            int lineNumber = 1; // ヘッダー行をスキップしたので1から開始
            int readCount = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                readCount++;
                if (lineNumber >= 1350 && lineNumber <= 1370) {
                    System.err.println("Read line " + lineNumber + " (readCount=" + readCount + "): " + line);
                    System.err.println("Line length: " + line.length());
                    if (line.length() == 0) {
                        System.err.println("Empty line detected!");
                    }
                }
                try {
                    // DEBUG: System.out.println(line);
                    PersonalInfo pi = getPersonalInfo(line);
                    data.add(pi);
                    processedCount++;
                    if (lineNumber >= 1350 && lineNumber <= 1370) {
                        System.err.println("Successfully processed line " + lineNumber);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing line " + lineNumber + ": " + line);
                    System.err.println("Exception: " + e.getMessage());
                    e.printStackTrace();
                    // エラーが発生した行はスキップして続行
                }
            }
            System.err.println("Loop exited. Total lines read: " + readCount + ", Last line number: " + lineNumber);
            System.err.println("Finished reading file. Last line number: " + lineNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("Processed lines: " + processedCount);
        System.err.println("Total data size: " + data.size());

        // データの並び替えを行う
        data.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.nameKana(), b.nameKana()));

        System.err.println("Data size after sort: " + data.size());

        // 重複データの確認
        long duplicateCount = data.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        PersonalInfo::nameKana,
                        java.util.stream.Collectors.counting()))
                .values()
                .stream()
                .filter(count -> count > 1)
                .count();
        System.err.println("Number of duplicate nameKana groups: " + duplicateCount);

        // CSVファイルの変換結果を出力しつつ、出力用のファイルのデータを生成する
        StringBuilder outputData = new StringBuilder("氏名,住所,年齢,血液型");
        System.out.println("氏名,住所,年齢,血液型");
        outputData.append(System.lineSeparator());
        int outputCount = 0;
        for (PersonalInfo pi : data) {
            outputData.append(pi.toCSVRow());
            outputData.append(System.lineSeparator());
            System.out.println(pi.toCSVRow());
            outputCount++;
        }
        System.err.println("Output count: " + outputCount);

        // ファイル書き込み
        try {
            Files.writeString(Path.of("output.csv"), outputData.toString(), Charset.defaultCharset());
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private static PersonalInfo getPersonalInfo(String line) {
        String[] cells = line.split(",");

        // Debug: Print age field value
        // System.out.println("Age field: [" + cells[10] + "] from line: " + line);

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
                cells[11]);
        return pi;
    }
}
