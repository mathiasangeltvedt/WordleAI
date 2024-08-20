# Runtime Analysis
The runtime is expressed using these three parameters:
   * `n` - number of words in the list allWords
   * `m` - number of words in the list possibleWords
   * `k` - number of letters in the wordleWords


## matchWord
* `WordleAnswer::matchWord`: O(k)
    * *Insert description of why the method has the given runtime*
    The matchWord method has a runtime of O(k), where k is the length of a WordleWord. The reason for this is that in the matchWord method, we loop through the length of the answer. matchWord also has a helper method I've created, which also has a runtime of O(k). In the helper method, we also loop through the length of the answer, which is k. Therefore, the runtime of the entire matchWord method is O(k).
    Since k is the length of a WordleWord, which is always of length 5, we can actually say that the runtime of matchWord is a constant. However, to generalize it for all types of words, we state that the runtime is O(k).


## EliminateStrategy
* `WordleWordList::eliminateWords`: O(m*k)
    * *Insert description of why the method has the given runtime*
    The eliminateWords method has a runtime of O(mk), where m is the length of the possibleAnswers list, and k is the length of a WordleWord, which, in our case, is always 5, but we want to generalize it for all types of words. The reason we have a runtime of O(mk) is because eliminateWords contains a for-loop that iterates over all elements in the possibleAnswers list. For each element in this method, it uses the isPossibleWord method, which in turn uses the matchWord method we created earlier. As described, the matchWord method has a runtime of O(k), and since it is nested inside the for-loop for possibleAnswers, we have a nested loop where the outer loop has a runtime of O(m), and the inner loop has a runtime of O(k), resulting in a runtime of O(m*k).

## FrequencyStrategy
* `FrequencyStrategy::makeGuess`: O(m*k)
    * *Insert description of why the method has the given runtime*
    The makeGuess method has a runtime of O(m*k), where m is the length of the list of possibleAnswers, and k is the length of a WordleWord. The makeGuess method utilizes two external methods: eliminateWords and createMapsAndFindOccurrence.
    As described, eliminateWords has a runtime of O(mk), while the createMapsAndFindOccurrence method has a runtime of O(m). The reason for this is that the createMapsAndFindOccurrence method contains two loops: one that iterates over the length of a WordleWord, which has a runtime of O(k), and another that iterates over the elements in possibleAnswers, which has a runtime of O(m). Therefore, in the worst case, the runtime of this method is O(mk).
    The makeGuess method itself also has a nested loop, where the outer loop iterates over all elements in possibleAnswers, which has a runtime of O(m), and the inner loop iterates over the length of a WordleWord, which has a runtime of O(k). Thus, the runtime of the nested loop is O(m*k).
    All of this together results in a worst-case runtime of O(m*k) for the makeGuess method.

# Make your own (better) AI
For this task you do not need to give a runtime analysis. 
Instead, you must explain your code. What was your idea for getting a better result? What is your strategy?

*Write your answer here*
I have used entropy to improve my code. The first thing I do here is to check if feedback exists or not (== null). If feedback doesn't exist, I will then check if the first guess is cached, i.e., if it's the first game we're playing or not (first == null). If it's the first game and 'first' is null, I use the findBestWord method, which I will explain in more detail later, to find the best first word (which in my case is always 'slate'). If we are not on the first game, 'first' will be cached from previous games, and I don't need to find the best first word, which saves a lot of runtime since it's before guesses.possibleAnswers() is at its largest.

If feedback exists, I update the correctCounter using the feedback. Here, we check which letters are correct and whether this letter already exists in the HashSet of correct letters. We increase the counter by 1 each time we add a letter to the set.

The next thing it does is to eliminate words from guesses.possibleAnswers() based on the feedback. Then we create a variable to hold the remaining possible answers.

Next, we check if the number of correct letters is 3 or more, and guessCount is not 5 because we don't want to use a filler word on our last guess. If this is true, we return the word we get from the getFillerWord() method:
getFillerWord() - This method creates two HashSets: one with the indexes where there are incorrect letters and one with the letters from these indexes in all words in the remaining possible answers. It then goes through all the words we can guess but excludes words with repeating letters, so we use the noRepeatingLetters method, which will be explained a bit later. It checks which of these words contains the most letters from the words we have stored in the letter HashSet. If we find a word with more letters than the current most letters, we store this word as the best filler and the number of letters as the current most letters. Finally, we check if the most letters are 2 or less, because this won't give us enough information, and therefore we can only call the findBestWord method with guesses.possibleAnswers(). Otherwise, we return the best filler word.

The next thing that happens is that we check if guessCount is less than 2. If it is, we want to guess a word that can provide us with the most information (i.e., a word with letters we haven't used and without repeating letters). For this, I use two helper methods: noRepeatingLetters() and noUsedLetters().
noRepeatingLetters() - This method simply goes through the list of all possible answers, checks if a word has repeating letters, and if it doesn't, we can use the word and add it to the list of new possible answers. If the word has repeating letters, we exclude it.
noUsedLetters() - This method has two helper methods: createHashes() and createPossibleAnswersBasedOnFeedback(). createHashes() updates HashMaps and HashSets with information about the index of the correct letters we have, so we don't guess words with yellow letters here, information about which letters have been used except for those in the wrong position, and also information about which letters are correct but in the wrong position that we need to use in the next guess. createPossibleAnswersBasedOnFeedback() goes through each word in the list of remaining possible answers that we got from noRepeatingLetters and checks the following criteria:

Whether we use a correct or incorrect letter
Whether we have used all the letters that are in the wrong position, and that these are not in the same wrong position or a position where we already know there is a correct letter.
If all the requirements are met, we know that we can use this word, and thus we add the word to the new list of possible answers. Otherwise, we exclude this word. The new list after all this is gone through is what we save back in the possibleWords variable.
Finally, if none of these cases apply, we call the findBestWord method on the list of current possible answers. Here, we keep two variables, highestEntropy and bestGuess. We then go through all the words in the list of remaining possible answers. For each word, we calculate the entropy of the word against all the other words in the remaining possible answers. What the calculateEntropy method does is that it checks each word in the list of possible answers against the rest of the possible answers and checks which word is most likely to eliminate the most remaining words based on a pattern calculation. The method returns the entropy, and back in the findBestWord method, we check if the entropy of the word we checked is greater than the current best word's entropy. If it is, we update the highest entropy and set the best word to be this word. If bestGuess is still null, it's because there's only one word left in the list of possible answers, and then we can simply return that word. Otherwise, we add the letters in the best word to the set of used letters and return the word.