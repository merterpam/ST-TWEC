/**
 * Created by mert on 25/06/2017.
 */

package com.erpam.mert.ST_TWEC;

import com.abahgat.suffixtree.Node;
import com.erpam.mert.ST_TWEC.model.Tweet;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TweetClusteringToolTest extends TestCase {
    public void testPopulateIndices() {
        String[] words = new String[]{"libertypike",
                "franklintn",
                "carothersjohnhenryhouse",
                "carothersezealhouse",
                "acrossthetauntonriverfromdightonindightonrockstatepark",
                "dightonma",
                "dightonrock",
                "6mineoflowgaponlowgapfork",
                "lowgapky",
                "lemasterjohnjandellenhouse",
                "lemasterhouse",
                "70wilburblvd",
                "poughkeepsieny",
                "freerhouse",
                "701laurelst",
                "conwaysc",
                "hollidayjwjrhouse",
                "mainandappletonsts",
                "menomoneefallswi",
                "mainstreethistoricdistrict",
                "addressrestricted",
                "brownsmillsnj",
                "hanoverfurnace",
                "hanoverbogironfurnace",
                "sofsavannahatfergusonaveandbethesdard",
                "savannahga",
                "bethesdahomeforboys",
                "bethesda"};

        List<Tweet> wordList = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            wordList.add(new Tweet(words[i], ""));
        }

        TweetClusteringTool in = new TweetClusteringTool(wordList);

        in.createNodeArray();
        in.populateTree();
        for (Node node : in.getNodes()) {
            HashSet<Integer> indexSet = new HashSet<>();
            for (int index : node.getIndexSet())
                indexSet.add(index);

            HashSet<Integer> fetchedIndexSet = new HashSet<>();
            fetchedIndexSet.addAll(node.fetchIndexSet());

            assertEquals(node.getIndexSet().length, indexSet.size());
            assertEquals(fetchedIndexSet, indexSet);
        }
    }
}
