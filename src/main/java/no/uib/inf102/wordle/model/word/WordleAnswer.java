package no.uib.inf102.wordle.model.word;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import no.uib.inf102.wordle.resources.GetWords;

/**
 * This class represents an answer to a Wordle puzzle.
 * 
 * The answer must be one of the words in the LEGAL_WORDLE_LIST.
 */
public class WordleAnswer {

    private final String WORD;

    private static Random random = new Random();

    /**
     * Creates a WordleAnswer object with a given word.
     * @param answer
     */
    public WordleAnswer(String answer) {
        this.WORD = answer.toLowerCase();
    }

    /**
     * Creates a WordleAnswer object with a random word from the answer word list
     */
    public WordleAnswer() {
        this(random);
    }

    /**
     * Creates a WordleAnswer object with a random word from the answer word list
     * using a specified random object.
     * This gives us the opportunity to set a seed so that tests are repeatable.
     */
    public WordleAnswer(Random random) {
        this(getRandomWordleAnswer(random));
	}

    /**
     * Gets a random wordle answer
     * 
     * @param random
     * @return
     */
    private static String getRandomWordleAnswer(Random random) {
        int randomIndex = random.nextInt(GetWords.ANSWER_WORDS_LIST.size());
        String newWord = GetWords.ANSWER_WORDS_LIST.get(randomIndex);
        return newWord;
    }

    /**
     * Guess the Wordle answer. Checks each character of the word guess and gives
     * feedback on which that is in correct position, wrong position and which is
     * not in the answer word.
     * This is done by updating the AnswerType of each WordleCharacter of the
     * WordleWord.
     * 
     * @param wordGuess
     * @return wordleWord with updated answertype for each character.
     */
    public WordleWord makeGuess(String wordGuess) {
        if (!GetWords.isLegalGuess(wordGuess))
            throw new IllegalArgumentException("The word '" + wordGuess + "' is not a legal guess");

        WordleWord guessFeedback = matchWord(wordGuess, WORD);
        return guessFeedback;
    }

    /**
     * Generates a WordleWord showing the match between <code>guess</code> and
     * <code>answer</code>
     * 
     * @param guess
     * @param answer
     * @return
     */
    public static WordleWord matchWord(String guess, String answer) { // Runtime of O(k)
        if (guess.length() != answer.length())
            throw new IllegalArgumentException("Guess and answer must have same number of letters but guess = " + guess
                    + " and answer = " + answer);

        HashMap<Character, Integer> wordMap = new HashMap<>();
        for (int i = 0; i < answer.length(); i++) {
            char c = answer.charAt(i);
            wordMap.put(c, wordMap.getOrDefault(c, 0) + 1);
        }
        
        AnswerType[] feedback = checkFeedbackForCharacter(guess, answer, wordMap);

        return new WordleWord(guess,feedback);
    }

    /**
     * Checks the feedback to see if the different characters in the guess are correct, wrong or wrong_pos
     * @param guess is the guessed word
     * @param answer is the answer
     * @param wordMap is a HashMap containing information about each character in the answer
     * @return feedback result from the guess
     */
    private static AnswerType[] checkFeedbackForCharacter(String guess, String answer, HashMap<Character, Integer> wordMap) {
        AnswerType[] feedback = new AnswerType[5];
        // Check if the character is in the correct position
        for (int i = 0; i<answer.length(); i++) {
            if (Objects.equals(guess.charAt(i), answer.charAt(i))) {
                wordMap.put(guess.charAt(i), wordMap.get(guess.charAt(i)) - 1);
                feedback[i] = AnswerType.CORRECT;
            }
        }

        // Check if the character is in the wrong pos or just wrong
        for (int i=0; i<answer.length(); i++) {
            char character = guess.charAt(i);
            if (feedback[i] == AnswerType.CORRECT) {
                continue;
            }
            if (answer.contains(String.valueOf(character)) && wordMap.get(character) != 0) {
                wordMap.put(guess.charAt(i), wordMap.get(guess.charAt(i)) - 1);
                feedback[i] = AnswerType.WRONG_POSITION;
            }
            else {
                feedback[i] = AnswerType.WRONG;
            }
        }
        return feedback;
    }
}