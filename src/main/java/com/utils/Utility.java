package com.utils;

import com.erpam.mert.ST_TWEC.model.Tweet;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Utility {

    public static BufferedReader getStream(String fileName) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return reader;
    }

    /**
     * Reads the uploaded file and adds tweets to array.The format of the file should be:
     * Line: tweet [\t tweetLabel]
     *
     * @param reader: Reader for the uploaded file
     */

    public static ArrayList<Tweet> readStream(BufferedReader reader) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        String line;
        int tabNumber = 0;
        try {
            while ((line = reader.readLine()) != null) {
                String splitLine[] = line.split("\t");
                if (splitLine.length == 1) {
                    Tweet tweet = new Tweet(splitLine[0], "");
                    tweets.add(tweet);

                } else if (splitLine.length == 2) {
                    Tweet tweet = new Tweet(splitLine[0], splitLine[1]);
                    tweets.add(tweet);
                } else {
                    return null;
                }

                if (tabNumber == 0) {
                    tabNumber = splitLine.length;
                } else if (tabNumber != splitLine.length) {
                    return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tweets;
    }


    /**
     * Converts elapsed time from nanoseconds to seconds
     *
     * @param startTime Start Time in nanoseconds
     * @param endTime   End Time in nanoseconds
     * @return The elapsed time in seconds
     */
    public static double convertElapsedTime(long startTime, long endTime) {
        return (double) (endTime - startTime) / 1000000000.0;
    }

    /**
     * Normalize an input string
     *
     * @param in the input string to normalize
     * @return <tt>in</tt> all lower-case, without any non alphanumeric character
     */
    public static String normalize(String in) {
        StringBuilder out = new StringBuilder();
        String l = in.toLowerCase();
        for (int i = 0; i < l.length(); ++i) {
            char c = l.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Computes the set of all the substrings contained within the <tt>str</tt>
     * <p>
     * It is fairly inefficient, but it is used just in tests ;)
     *
     * @param str the string to compute substrings of
     * @return the set of all possible substrings of str
     */
    public static Set<String> getSubstrings(String str) {
        Set<String> ret = new HashSet<String>();
        // compute all substrings
        for (int len = 1; len <= str.length(); ++len) {
            for (int start = 0; start + len <= str.length(); ++start) {
                String itstr = str.substring(start, start + len);
                ret.add(itstr);
            }
        }

        return ret;
    }

    public static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order) {

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1,
                               Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
