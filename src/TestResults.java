// Copyright (c) 2023 Matyas Urban. Licensed under the MIT license.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TestResults {
    String aggregatedString;
    double passRate;

    /**
     * Class constructor
     * @param input Test-results in form of text for the app to aggregate.
     */
    public TestResults(String input) {
        Object[] result = aggregateTestResults(input);
        this.aggregatedString = String.valueOf(result[0]);
        this.passRate = ((Number)result[1]).doubleValue();
    }

    /**
     * Method to generate statistics based on the test results in form of text
     * @param fileContent test results for analysis
     * @return Object[ String with a report from test aggregation, Double pass rate (used for a colored bar to visualize success) ]
     */
    private Object[] aggregateTestResults(String fileContent) {
        // Regex to match velid test result lines
        // startPattern example: testStarted id="1" name="test1"
        // finishPattern example 1: testFinished id="1" duration="100" result=OK
        // finishPattern example 2: testFinished id="1" duration="100" result=FAIL
        // finishPattern example 3: testFinished id="1" duration="100" result=FAIL error="Error Message"
        // we allow for any number of whitespaces around the equality sign
        Pattern startPattern = Pattern.compile("testStarted id\\s*=\\s*\"(\\d+)\" name\\s*=\\s*\"(.+?)\"");
        Pattern finishPattern = Pattern.compile("testFinished id\\s*=\\s*\"(\\d+)\" duration\\s*=\\s*\"(\\d+)\" result\\s*=\\s*(\\w+)(?: error\\s*=\\s*\"(.+?)\")?");
        // to enhance time complexity, we will be storing values into a hash map
        // key = String test id
        // value = ArrayList[String test name, String test duration, String test result, optional String test error]
        // thanks to using HashMap, when we encounter testFinished, we do not have to iterate over the entire array to attach results O(n)
        // but we can do this in O(1) thanks to the properties of a hashmap
        HashMap<String, ArrayList<String>> testInfoMap = new HashMap<>();
        int omittedLines = 0;
        int startedLines = 0;
        int finishedLines = 0;
        // get individual lines from input for the analysis
        String[] lines = fileContent.split(System.lineSeparator());
        // iterate over the lines
        for (String line : lines) {
            Matcher startMatcher = startPattern.matcher(line);
            Matcher finishMatcher = finishPattern.matcher(line);

            // if it's recognized as a valid testStarted line
            if (startMatcher.find()) {
                String id = startMatcher.group(1);
                String testName = startMatcher.group(2);

                testInfoMap.putIfAbsent(id, new ArrayList<>());
                testInfoMap.get(id).add("Name: " + testName);
            // if it's recognized as a valid testFinished line
            } else if (finishMatcher.find()) {
                String id = finishMatcher.group(1);
                String duration = finishMatcher.group(2);
                String result = finishMatcher.group(3);
                String error = finishMatcher.group(4);
                // if we encounter testFinished after testStarted, we add the data to the associated key id
                if (testInfoMap.containsKey(id)) {
                    testInfoMap.get(id).add("Duration: " + duration + " ms");
                    testInfoMap.get(id).add("Result: " + result);
                    if (error != null) {
                        testInfoMap.get(id).add("Error: " + error);
                    }
                // else we omit
                } else {
                    omittedLines++;
                }
            // else we omit this line and later on let the user know
            } else {
                omittedLines++;
            }
        }
        // if testStarted line has no associated testFinish line, we omit it
        // so

        int totalTests = testInfoMap.size();
        int successfulTests = 0;
        int failedTests = 0;
        double totalDuration = 0;
        int testsWithError = 0;

        // iterate over individual tests
        for (ArrayList<String> testInfo : testInfoMap.values()) {
            // if the testInfo contains just the testName, it means that
            // a) testStarted appeared after testFinished (handled on line 73)
            // b) the file does not have associated testFinished data
            // we thus want to ignore this testStarted line
            if (testInfo.size()==1){
                totalTests--;
                omittedLines++;
            }

            boolean success = false;
            boolean hasError = false;
            double duration = 0;

            // iterate over info items for a current test
            for (String info : testInfo) {
                if (info.contains("Result: OK")) {
                    success = true;
                } else if (info.startsWith("Duration: ")) {
                    // get just the number from ("Duration: " + duration + " ms")
                    duration = Double.parseDouble(info.substring(10, info.length() - 3));
                } else if (info.startsWith("Error: ")) { // Check for the error string
                    hasError = true; // Update the hasError variable if an error is found
                }
            }

            if(hasError){
                testsWithError++;
            }

            if (success) {
                successfulTests++;
            } else {
                failedTests++;
            }

            totalDuration += duration;
        }

        // return if no valid test result lines were detected
        if (totalTests==0){
            return new Object[]{"No tests to aggregate.\nNumber of omitted lines: "+omittedLines,0};
        }

        // calculate the pass rate and average duration
        double passRate = ((double) successfulTests)/((double) totalTests);
        double avgDuration = totalDuration / totalTests;

        // put together the report
        String result = "Total number of tests: " + totalTests + "\n"
                + "Number of successful tests: " + successfulTests + "\n"
                + "Number of failed tests: " + failedTests + "\n"
                + "Number of failed tests with errors: " + testsWithError + "\n"
                + "Pass rate: " + String.format("%.2f", passRate*100) + "%\n"
                + "Average test duration: " + String.format("%.2f", avgDuration) + " ms\n"
                + "Total duration of running all tests: " + String.format("%.2f", totalDuration) + " ms\n"
                + "Number of omitted lines: " + omittedLines;

        return new Object[]{result, passRate};
    }

    /**
     * Method to generate random test results for demonstration of this app
     * @return multiple lines of random test results
     */
    public static String getRandomTestsString() {
        Random random = new Random();
        StringBuilder randomTestsBuilder = new StringBuilder();

        // decide on the random number of tests (100-2000)
        int numberOfTests = 100 + random.nextInt(1901);
        // decide on the random pass rate (ratio of successful to failed tests) (0-1)
        double passRate = random.nextDouble();
        // calculate the number of successful tests
        int successCount = (int) Math.round(passRate * numberOfTests);

        // create and add to string builder a tests
        // firstly the successful ones
        // then the failed ones
        for (int i = 0; i < numberOfTests; i++) {
            int id = i + 1;
            String name = "Test" + id;
            // if the iteration is below successCount, this random test will be a success
            boolean isSuccess = i < successCount;
            // random duration (3-100ms)
            int duration = 3 + random.nextInt(98);
            // if we're creating failed tests (all successful ones have already been generated)
            // decide at random whether this failed test will have an error message
            boolean hasError = !isSuccess && random.nextBoolean();
            String error = hasError ? "Sample error message" : "";

            randomTestsBuilder.append("testStarted id=\"").append(id).append("\" name = \"").append(name).append("\"\n");
            randomTestsBuilder.append("testFinished id=\"").append(id).append("\" duration=\"").append(duration).append("\" result=").append(isSuccess ? "OK" : "FAIL");

            if (hasError) {
                randomTestsBuilder.append(" error=\"").append(error).append("\"");
            }

            randomTestsBuilder.append("\n");
        }

        return randomTestsBuilder.toString();
    }
}