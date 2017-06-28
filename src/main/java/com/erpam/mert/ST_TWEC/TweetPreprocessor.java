package com.erpam.mert.ST_TWEC;

import com.erpam.mert.ST_TWEC.model.Tweet;
import com.utils.Utility;

import java.util.*;

/**
 * Created by mert on 25/06/2017.
 */
public class TweetPreprocessor {

    private ArrayList<Tweet> noContentTweets;
    private float preprocessThreshold = 0.6f;
    private List<Tweet> tweets;

    public TweetPreprocessor(List<Tweet> tweets) {
        this.noContentTweets = new ArrayList<>();
        this.tweets = tweets;
    }

    public TweetPreprocessor(float preprocessThreshold, List<Tweet> tweets) {

        this.noContentTweets = new ArrayList<>();
        this.preprocessThreshold = preprocessThreshold;
        this.tweets = tweets;
    }

    /**
     * Preprocessed tweets by transforming them into lower-case letters
     * removing http links and hashtags
     * removing words with document frequencies above the threshold {@preprocessThreshold}
     */
    public void preProcessTweets() {
        long startTime = System.nanoTime();
        for (int i = 0; i < tweets.size(); i++) {

            Tweet tweet = tweets.get(i);
            String processedTweet = tweet.getTweet();

            processedTweet = processedTweet.toLowerCase();
            processedTweet = removeSpecialWords(processedTweet, "http");
            processedTweet = removeSpecialWords(processedTweet, "#");
            processedTweet = removeSpecialWords(processedTweet, "@");
            processedTweet = removeRt(processedTweet);

            processedTweet = removeForeignCharacters(processedTweet);

            processedTweet = processedTweet.replaceAll("\\.|,|;|:|!|\\?|=|'|\\\\|/|(&amp;)", "");
            processedTweet = processedTweet.replaceAll("  +", " ");
            processedTweet = processedTweet.replaceAll("^ ", "");

            tweet.setProcessedTweet(processedTweet);
            if (processedTweet.length() < 10) {
                noContentTweets.add(tweet);
                tweets.remove(i);
                i--;
            }
        }

        removeTweetsByFrequency();

        long endTime = System.nanoTime();

        System.out.println("Finished preprocessing in " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }

    public List<Tweet> getNoContentTweets() {
        return noContentTweets;
    }

    private String removeRt(String tweet) {
        while (tweet.startsWith("rt ")) {
            tweet = tweet.substring(3);
        }
        while (tweet.contains(" rt ")) {
            int startIndex = tweet.indexOf(" rt");
            int endIndex = startIndex + 3;
            tweet = tweet.substring(0, startIndex) + tweet.substring(endIndex);
        }
        return tweet;
    }

    private String removeSpecialWords(String tweet, String word) {

        while (tweet.contains(word)) {
            int startIndex = tweet.indexOf(word);
            int endIndex = tweet.indexOf(" ", startIndex);
            if (endIndex == -1) {
                tweet = tweet.substring(0, startIndex);
            } else {
                tweet = tweet.substring(0, startIndex) + tweet.substring(endIndex);
            }
        }
        return tweet;
    }

    private String removeForeignCharacters(String tweet) {
        char[] tweetArray = tweet.toCharArray();
        for (int i = 0; i < tweet.length(); i++) {
            if (tweetArray[i] > 256) {
                if (tweetArray[i] != '�' && tweetArray[i] != '�' && tweetArray[i] != '�' && tweetArray[i] != '�' && tweetArray[i] != '�' && tweetArray[i] != '�')
                    tweetArray[i] = ' ';
            } else {
                if (tweetArray[i] == '?' || tweetArray[i] == '\'')
                    tweetArray[i] = ' ';
            }
        }

        return new String(tweetArray);
    }

    private void removeTweetsByFrequency() {

        HashMap<String, Integer> documentTermFrequency = new HashMap<>();
        for (Tweet tweet : tweets) {
            String[] words = tweet.getProcessedTweet().split(" ");
            HashSet<String> uniqueWords = new HashSet<String>(Arrays.asList(words));

            for (String word : uniqueWords) {
                if (documentTermFrequency.containsKey(word)) {
                    documentTermFrequency.put(word, documentTermFrequency.get(word) + 1);
                } else {
                    documentTermFrequency.put(word, 1);
                }
            }
        }

        documentTermFrequency = (HashMap<String, Integer>) Utility.sortByComparator(documentTermFrequency, false);

        for (Map.Entry<String, Integer> term : documentTermFrequency.entrySet()) {

            if (((double) term.getValue() / tweets.size()) > preprocessThreshold) {
                for (int i = 0; i < tweets.size(); i++) {
                    Tweet tweet = tweets.get(i);
                    String processedTweet = tweet.getProcessedTweet();
                    while (processedTweet.contains(term.getKey())) {
                        int startIndex = processedTweet.indexOf(term.getKey());
                        int endIndex = processedTweet.indexOf(" ", startIndex);
                        if (endIndex == -1) {
                            processedTweet = processedTweet.substring(0, startIndex);
                        } else {
                            processedTweet = processedTweet.substring(0, startIndex) + processedTweet.substring(endIndex);
                        }
                    }

                    tweet.setProcessedTweet(processedTweet);
                    if (processedTweet.length() < 5) {
                        noContentTweets.add(tweet);
                        tweets.remove(i);
                        i--;
                    }
                }
            } else {
                break;
            }
        }

    }
}
