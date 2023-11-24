import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Map<String, Integer> AppsPerCat = new HashMap<>();
        Map<String, Integer> MostApps = new HashMap<>();
        Map<String, Integer> DeveloperApps = new HashMap<>();
        Map<String, Double> AppPrices = new HashMap<>();

        File inputFile = new File("Google Play Store Apps.csv");
        Scanner s = new Scanner(inputFile);
        s.nextLine();

        ArrayList<String> badLines = new ArrayList<>();
        Long freeInstalls = 0L;
        Long paidInstalls = 0L;

        while (s.hasNextLine()) {
            String line = s.nextLine();
            try {
                String[] lineParts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                String AppName = lineParts[0].trim();
                String AppId = lineParts[1].trim();
                String Category = lineParts[2].trim();
                String DeveloperEmail = lineParts[15].trim();
                String DeveloperId= lineParts[13].trim();

                double price = Double.parseDouble(lineParts[9].trim());
                AppPrices.put(AppName, price);
                boolean isFree = Boolean.parseBoolean(lineParts[8].trim());

                AppsPerCat.put(Category, AppsPerCat.getOrDefault(Category, 0) + 1);

                String CleanedComp = getCleanedCompany(AppId);
                MostApps.put(CleanedComp, MostApps.getOrDefault(CleanedComp, 0) + 1);

                if (isFree) {
                    freeInstalls += Long.parseLong(lineParts[6].trim());

                } else {
                    paidInstalls += Long.parseLong(lineParts[6].trim());

                }

                if (!ShouldSkipDeveloper(DeveloperEmail, AppId)) {
                    MostApps.put(AppId, MostApps.getOrDefault(AppId, 0) + 1);
                    DeveloperApps.put(DeveloperId, DeveloperApps.getOrDefault(DeveloperId, 0) + 1);
                }
            } catch (Exception e) {
                badLines.add(line);
            }
        }
        s.close();
        List<String> AppsToBuy = CalculateAppsToBuy(AppPrices, 1000);
        List<String> AppsToBuy2 = CalculateAppsToBuy(AppPrices, 10000);
        SaveBadLines(badLines);
        SaveToReport1(AppsPerCat);
        SaveToReport2(MostApps);
        SaveToReport3(DeveloperApps);
        SaveToReport4(AppsToBuy);
        SaveToReport5(AppsToBuy2);
        SaveToReport6(freeInstalls, paidInstalls);
    }

    private static void SaveToReport1(Map<String, Integer> AppsPerCat) throws IOException {
        try(FileWriter fw = new FileWriter("Report 1 task.csv")) {
            fw.write("category, number of apps\n");
            for (Map.Entry<String, Integer> entry : AppsPerCat.entrySet()) {
                fw.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        }
    }

    private static void SaveToReport2(Map<String, Integer> appsPerCompany) throws IOException {
        List<Map.Entry<String, Integer>> sortedCompanies = new ArrayList<>(appsPerCompany.entrySet());
        sortedCompanies.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        try (FileWriter fw = new FileWriter("Report 2 task.csv")) {
            fw.write("company, number of apps\n");
            for (int i = 0; i < Math.min(sortedCompanies.size(), 100); i++) {
                Map.Entry<String, Integer> entry = sortedCompanies.get(i);

                fw.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        }
    }
    private static String getCleanedCompany(String rawCompany) {

        String[] parts = rawCompany.split("\\.");
        String LastWord = parts[parts.length-1];
        String SecondLastWord = parts[parts.length-2];
        return LastWord + "." + SecondLastWord;
    }

    private static void SaveBadLines(ArrayList<String> badLines) throws IOException{
        try (FileWriter fw = new FileWriter( "Bad Lines.csv")) {

            for (String line : badLines) {
                fw.write(line + "\n");
            }
        }
    }
    private static boolean ShouldSkipDeveloper(String developerEmail, String AppId) {
        String[] parts = AppId.split("\\s+");
        if (parts.length >= 2) {
            return developerEmail.toLowerCase().contains(parts[1].toLowerCase());
        }
        return false;
    }
    private static void SaveToReport3(Map<String, Integer> DeveloperApps) throws IOException{
        List<Map.Entry<String, Integer>> sortedDevelopers = new ArrayList<>(DeveloperApps.entrySet());
        sortedDevelopers.sort(Map.Entry.comparingByValue());
        Collections.reverse(sortedDevelopers);

        try (FileWriter fw = new FileWriter("Report 3 task.csv")) {
            fw.write("Developer, number of apps\n");
            for (int i = 0; i < Math.min(sortedDevelopers.size(), 3); i++) {
                Map.Entry<String, Integer> entry = sortedDevelopers.get(i);
                fw.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        }
    }
    private static List<String> CalculateAppsToBuy(Map<String, Double> appPrices, double budget) {
        List<String> appsBought = new ArrayList<>();
        double remainingBudget = budget;

        appPrices = SortByPriceDescending(appPrices);
        for (Map.Entry<String, Double> entry : appPrices.entrySet()) {
            if (remainingBudget >= entry.getValue()) {
                remainingBudget -= entry.getValue();
                appsBought.add(entry.getKey());
            } else {
                break;
            }
        }

        return appsBought;
    }

    private static Map<String, Double> SortByPriceDescending(Map<String, Double> appPrices) {
        Map<String, Double> sortedPrices = new HashMap<>(appPrices);
        sortedPrices.entrySet().stream().sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        return sortedPrices;
    }

    private static void SaveToReport4(List<String> appsBought) throws IOException {
        try(FileWriter fw = new FileWriter("Report 4.0 task.csv")) {
            fw.write("Apps that can be bought with $1000\n");
            for (String appName : appsBought) {
                fw.write(appName + "\n");
            }
        }
    }
    private static void SaveToReport5(List<String> appsBought) throws IOException {
        try(FileWriter fw = new FileWriter("Report 4.1 task.csv")) {
            fw.write("Apps that can be bought with $10 000\n");
            for (String appName : appsBought) {
                fw.write(appName + "\n");
            }
        }
    }
    private static void SaveToReport6(long freeInstalls, long paidInstalls) throws IOException {
        try (FileWriter fw = new FileWriter("Report 5 task.csv")) {
            fw.write("Type, Number of Installs\n");
            fw.write("Free, " + freeInstalls + "\n");
            fw.write("Paid, " + paidInstalls + "\n");
        }
    }

}

