package com.sciaps;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CSVParserUtil {

    CSVReader csvReader;
    int shotSizeHeight = 16;
    String mFilepath;

    private LinkedHashMap<String, Integer> elementIntensityIndices;
    private LinkedHashMap<String, ArrayList<String>> elementWavelengths;
    private LinkedHashMap<Integer, ArrayList<Double>> elementWavelengthIntensities;

    File originalWavelengthCSV;

    public CSVParserUtil(String filepath) throws Exception {
        mFilepath = filepath;

        originalWavelengthCSV = new File(mFilepath);
        if (!originalWavelengthCSV.exists()) {
            throw new Exception("Input File does not exist!");
        }

        FileReader fr = new FileReader(originalWavelengthCSV);
        BufferedReader br = new BufferedReader(fr);
        Reader reader = br;

        csvReader = new CSVReader(reader);
        elementWavelengths = new LinkedHashMap<String, ArrayList<String>>();
        elementWavelengthIntensities = new LinkedHashMap<Integer, ArrayList<Double>>();
        elementIntensityIndices = new LinkedHashMap<String, Integer>();
    }

    private void readCSV() throws Exception{
        String[] nextRecord;
        boolean isFirstItem = true;
        int rowCount = 0;
        while ((nextRecord = csvReader.readNext()) != null) {
            rowCount++;

            if (isFirstItem) {
                for (int i=2;i < nextRecord.length;i++) {
                    String[] elementSplit = nextRecord[i].split(" ");
                    ArrayList<String> elementsWavelength = elementWavelengths.get(elementSplit[0]);

                    String elementKey = elementSplit[0] + " " + elementSplit[1].replace("nm", "");
                    elementIntensityIndices.put(elementKey, i);

                    if (elementsWavelength == null) {
                        ArrayList<String> wavelength = new ArrayList<String>();
                        wavelength.add(elementSplit[1].replace("nm", ""));
                        elementWavelengths.put(elementSplit[0], wavelength);
                    } else {
                        elementsWavelength.add(elementSplit[1].replace("nm", ""));
                    }
                }

                isFirstItem = false;
                continue;
            } else {
                for (int i=2;i < nextRecord.length;i++) {
                    ArrayList<Double> wavelengthIntensitities = elementWavelengthIntensities.get(i);
                    if (wavelengthIntensitities == null) {
                        ArrayList<Double> intensity = new ArrayList<Double>();

                        if (nextRecord[i] == null || nextRecord[i].trim().length() == 0) {
                            intensity.add(null);
                        } else {
                            intensity.add(Double.parseDouble(nextRecord[i]));
                        }

                        elementWavelengthIntensities.put(i, intensity);
                    } else {
                        if (nextRecord[i] == null || nextRecord[i].trim().length() == 0) {
                            wavelengthIntensitities.add(null);
                        } else {
                            wavelengthIntensitities.add(Double.parseDouble(nextRecord[i]));
                        }
                    }
                }
            }
        }

        double rowLength = (double) (rowCount-1);
        shotSizeHeight = (int) Math.sqrt(rowLength);
    }

    private void createReverseCSV() throws Exception {
        if (originalWavelengthCSV.getParent() != null) {
            Writer writer = Files.newBufferedWriter(Paths.get(originalWavelengthCSV.getParent() + "/" + "new_shots.csv"));
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            ArrayList<String> header = new ArrayList<String>();
            header.add("X");
            header.add("Y");

            for (Map.Entry<String, Integer> item : elementIntensityIndices.entrySet()) {
                String key = item.getKey();
                header.add(key+"nm");
            }
            String[] headerArr = new String[header.size()];
            headerArr = header.toArray(headerArr);
            csvWriter.writeNext(headerArr);

            for (int i = 0;i < shotSizeHeight;i++) {
                for (int j = 0;j < shotSizeHeight;j++) {
                    String[] row = new String[elementIntensityIndices.size() + 2];
                    row[0] = String.valueOf(j + 1);
                    row[1] = String.valueOf(i + 1);
                    int total = (shotSizeHeight * shotSizeHeight);

                    for (int z = 0;z < elementIntensityIndices.size();z++) {
                        String element = String.valueOf(elementIntensityIndices.entrySet().toArray()[z]);
                        String index = element.split("=")[1];
                        ArrayList<Double> elementWavelengths = elementWavelengthIntensities.get(Integer.valueOf(index));
                        row[z + 2] = String.valueOf(elementWavelengths.get(total + (-i*shotSizeHeight) - (shotSizeHeight) + j));
                    }

                    csvWriter.writeNext(row);
                }
            }

            csvWriter.flush();
            csvWriter.close();
        }
    }

    public void process() throws Exception {
        readCSV();
        createReverseCSV();
    }
}
