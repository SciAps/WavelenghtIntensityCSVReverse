package com.sciaps;

public class Main {

    public static void main(String[] args) {
        try {
            String inputFilePath = args[0];
            CSVParserUtil csvParserUtil = new CSVParserUtil(inputFilePath);
            csvParserUtil.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
