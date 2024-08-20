package no.uib.inf102.wordle.controller.AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.model.word.WordleWordList;

/**
 * This strategy finds the word within the possible words which has the highest
 * expected
 * number of green matches.
 */
public class FrequencyStrategy implements IStrategy {

    private WordleWordList guesses;

    public FrequencyStrategy() {
        reset();
    }

    @Override
    public String makeGuess(WordleWord feedback) { // Runtime of O(m*k)
        if (feedback != null)
            guesses.eliminateWords(feedback);
        List<HashMap<Character, Integer>> listOfHmWithOccurence = createMapsAndFindOccurence(guesses.possibleAnswers());
        int highestScore = 0;
        String bestWord = "";
        for (String word : guesses.possibleAnswers()) {
            int currentScore = 0;
            for (int i = 0; i < word.length(); i++) {
                currentScore += listOfHmWithOccurence.get(i).get(word.charAt(i));
            }
            if (currentScore > highestScore) {
                highestScore = currentScore;
                bestWord = word;
            }
        }
        return bestWord;
    }

    /**
     * This method is used to create a list of hashmaps that are used to contain the
     * values of the
     * frequency of the occurence of each character on different indexes.
     * 
     * @param possibleAnswers are all the answers still reimaining in the list
     * @return a list of the different hashmaps
     */
    public List<HashMap<Character, Integer>> createMapsAndFindOccurence(List<String> possibleAnswers) {
        List<HashMap<Character, Integer>> listOfMapOfIndex = new ArrayList<>(5);
        for (int i = 0; i < guesses.wordLength(); i++) {
            listOfMapOfIndex.add(new HashMap<>());
        }
        for (String word : possibleAnswers) {
            for (int i = 0; i < word.length(); i++) {
                listOfMapOfIndex.get(i).put(word.charAt(i),
                        listOfMapOfIndex.get(i).getOrDefault(word.charAt(i), 0) + 1);
            }
        }
        return listOfMapOfIndex;
    }

    @Override
    public void reset() {
        guesses = new WordleWordList();
    }
}