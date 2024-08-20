package no.uib.inf102.wordle.controller.AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import no.uib.inf102.wordle.model.word.AnswerType;
import no.uib.inf102.wordle.model.word.WordleCharacter;
import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.model.word.WordleWordList;

public class EntropyStrategy implements IStrategy {

    private WordleWordList guesses;
    private HashSet<Character> usedLettersExcludeWrongPos;
    private HashMap<Character, Integer> mustHaveLetters;
    private HashSet<Character> correctLetters;
    private HashSet<Integer> indexOfCorrectLetters; 
    private int guessCount;
    private int correctCount; 
    private String first;

    public EntropyStrategy() {
        reset();
    }

    @Override
    public String makeGuess(WordleWord feedback) { 
        if (feedback == null) {
            if (first == null) {
                first = findBestWord(guesses.possibleAnswers());
                guessCount++;
                return first; 
            }
            guessCount++;
            for (int i=0; i<first.length(); i++) {
                usedLettersExcludeWrongPos.add(first.charAt(i));
            }
            return first;
        }
        updateCorrectCount(feedback);
        guesses.eliminateWords(feedback);
        List<String> possibleWords = guesses.possibleAnswers();

        if (correctCount > 2 && guessCount != 5) {
            guessCount++;
            return getFillerWord(feedback);
        }
        
        if (guessCount < 2) {
            possibleWords = noUsedLetters(noRepeatingLetters(guesses.possibleAnswers()), feedback);
            if (possibleWords.isEmpty()) {
                possibleWords = guesses.possibleAnswers();
            }
        }

        guessCount++;
        return findBestWord(possibleWords);
    }

    /**
     * Used to find the best possible word to guess based on entropy, where
     * we want to eliminate the most possible answers from each guess
     * @param possibleWords is the list of possible answers
     * @return a word to guess
     */
    private String findBestWord(List<String> possibleWords) {
        double highestEntropy = Double.MIN_VALUE;
        String bestGuess = null;
        for (String word : possibleWords) {
            double currentEntropy = calculateEntropy(word, possibleWords);
            if (currentEntropy > highestEntropy) {
                highestEntropy = currentEntropy;
                bestGuess = word;
            }
        }
        if (bestGuess == null) return possibleWords.get(0);
        for (int i=0; i<bestGuess.length(); i++) {
            usedLettersExcludeWrongPos.add(bestGuess.charAt(i));
        }
        return bestGuess;
    }

    /**
     * This method is used to calculate the entropy of each word based on a pattern calculation 
     * @param guess is the word we want to check the entropy of 
     * @param possibleWords is a list of all possible answers
     * @return the entropy of the word
     */
    private static double calculateEntropy(String guess, List<String> possibleWords) {
        HashMap<String, Integer> feedbackResult = new HashMap<>();
        for (String word : possibleWords) {
            String feedbackPattern = wordPattern(guess, word);
            feedbackResult.put(feedbackPattern, feedbackResult.getOrDefault(feedbackPattern, 0) + 1);
        }
        double entropy = 0.0;
        for (Integer value : feedbackResult.values()) {
            double probability = (double) value / possibleWords.size();
            entropy += -probability * (Math.log(probability) / Math.log(2)); // Shannon's entropiformel
        }
        return entropy;
    }

    /**
     * This method is used to create a pattern between the word we want to guess
     * and all the words in the possible answers list to see which pattern occurs the most
     * @param guess is the word we want to guess
     * @param word is a word from the list of possible answers
     * @return the pattern as a string
     */
    private static String wordPattern(String guess, String word) {
        StringBuilder indexPattern = new StringBuilder();
        for (int i = 0; i < guess.length(); i++) {
            if (guess.charAt(i) == word.charAt(i)) {
                indexPattern.append("C");
            }
            else {
                indexPattern.append("W");
            }
        }
        return indexPattern.toString();
    }

    /**
     * This method is used to remove all words from a 'copy' of the list of possible answers 
     * so that we can use it to find a guess to eliminiate even more answers
     * @param possibleWords is a list of all possible answers
     * @return a new list of possible answers
     */
    private List<String> noRepeatingLetters(List<String> possibleWords) {
        List<String> newList = new ArrayList<>();
        for (String word : possibleWords) {
            HashSet<Character> wordLetters = new HashSet<>(5);
            boolean isPossible = true;
            for (int i=0; i<word.length(); i++) {
                if (wordLetters.contains(word.charAt(i))) {
                    isPossible = false;
                }
                wordLetters.add(word.charAt(i));
            }
            if (isPossible) {
                newList.add(word);
            }
        }
        return newList;
    }

    /**
     * THis method is used to remove all words containing letters that are used, excluding 
     * lettes that are correct but in the wrong position. 
     * @param possibleWords is a list of all possible answers
     * @param feedback is the feedback from the previous guess
     * @return a list of new possible answers
     */
    private List<String> noUsedLetters(List<String> possibleWords, WordleWord feedback) { // Runtime of O(k*m)
        createHashes(feedback);
        return createPossibleAnswersBasedOnFeedback(possibleWords);
    }

    /**
     * This method is used to update all the HashSets and HashMaps containing information 
     * about which characters are correct, wrong, wrong position etc.
     * @param feedback is the feedback from the previous guess
     */
    private void createHashes(WordleWord feedback) {
        for (WordleCharacter c : feedback) {
            int index = 0;
            if (c.answerType == AnswerType.CORRECT) {
                indexOfCorrectLetters.add(index);
            }
            if (c.answerType != AnswerType.WRONG_POSITION) {
                usedLettersExcludeWrongPos.add(c.letter);
            }
            else {
                mustHaveLetters.put(c.letter, index);
            }
            index++;
        }
    }

    /**
     * This method is used to create the list we want to return in the method noUsedLetters.
     * We remove all words with correct letters, wrong letters, and correct letters that were in 
     * the wwrong position that are still in the same position or on the position of a correct letter.
     * @param possibleWords is the list of possible answers
     * @return a new list of possible answers.
     */
    private List<String> createPossibleAnswersBasedOnFeedback(List<String> possibleWords) {
        List<String> newPossibleWords = new ArrayList<>();
        for (String word : possibleWords) {
            boolean possibleWord = true;
            int count = 0;
            for (int i = 0; i < word.length(); i++) {
                if (usedLettersExcludeWrongPos.contains(word.charAt(i))) {
                    possibleWord = false;
                    break;
                }
                else if (mustHaveLetters.containsKey(word.charAt(i))) { count++;
                    if (mustHaveLetters.get(word.charAt(i)) == i) {
                        possibleWord = false;
                    }
                    else if (indexOfCorrectLetters.contains(mustHaveLetters.get(word.charAt(i)))) {
                        possibleWord = false;
                    }
                }
            }
            if (count != mustHaveLetters.size()) {
                possibleWord = false;
            }
            if (possibleWord) {
                newPossibleWords.add(word);
            }
        }
        return newPossibleWords;
    }

    /**
     * This method is used to find the best filler word from all words
     * to can be guessed based on the remaining words in the list of possible answers, 
     * and the feedback of the previous guess
     * @param feedback is the feedback from the previous guess
     * @return a filler word
     */
    private String getFillerWord(WordleWord feedback) {
        HashSet<Integer> indexes = new HashSet<>();
        HashSet<Character> chars = new HashSet<>();
        int index = 0;
        for (WordleCharacter c : feedback) {
            if (c.answerType == AnswerType.WRONG) {
                indexes.add(index);
            }
            index++;
        }
        for (String word : guesses.possibleAnswers()) {
            for (int i=0; i<word.length(); i++) {
                if (indexes.contains(i) && !chars.contains(word.charAt(i)) && !correctLetters.contains(word.charAt(i))) {
                    chars.add(word.charAt(i));
                }
            }
        }
        int mostLetters = 0;
        String bestFiller = null; 
        for (String word : noRepeatingLetters(guesses.getAllWords())) {
            int currentLetters = 0;
            for (int i=0; i<word.length(); i++) {
                if (chars.contains(word.charAt(i))) {
                    currentLetters++;
                }
            }
            if (currentLetters > mostLetters) {
                mostLetters = currentLetters;
                bestFiller = word;
            }
        }
        if (mostLetters < 3) {
            return findBestWord(guesses.possibleAnswers());
        }
        return bestFiller;
    }

    /**
     * This method is used to update the HashSet containing the correct
     * letters, and update the counter of correct letters
     * @param feedback is the feedback from the previous guess
     */
    private void updateCorrectCount(WordleWord feedback) {
        for (WordleCharacter c : feedback) {
            if (c.answerType == AnswerType.CORRECT) {   
                if (!correctLetters.contains(c.letter)) {
                    correctLetters.add(c.letter);
                    correctCount++;
                }
            }
        }
    }

    @Override
    public void reset() {
        guesses = new WordleWordList();
        usedLettersExcludeWrongPos = new HashSet<>();
        mustHaveLetters = new HashMap<>();
        correctLetters = new HashSet<>();
        indexOfCorrectLetters = new HashSet<>();
        guessCount = 0;
        correctCount = 0; 
    }
}