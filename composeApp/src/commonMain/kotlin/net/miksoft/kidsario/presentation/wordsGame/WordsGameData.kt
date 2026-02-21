package net.miksoft.kidsario.presentation.wordsGame

import kotlin.random.Random

enum class WordLanguage(val displayName: String) {
    ENGLISH("English"),
    GREEK("Greek")
}

data class WordOption(
    val word: String,
    val isCorrect: Boolean
)

object WordsGameData {
    private val englishWords = listOf(
        "Apple", "Ant", "Airplane", "Alligator",
        "Ball", "Bear", "Banana", "Bicycle",
        "Cat", "Cake", "Car", "Cloud",
        "Dog", "Duck", "Dragon", "Drum",
        "Elephant", "Eagle", "Earth", "Egg",
        "Fish", "Flower", "Frog", "Fox",
        "Giraffe", "Goat", "Guitar", "Grapes",
        "Hat", "Horse", "House", "Honey",
        "Ice", "Igloo", "Island", "Insect",
        "Jelly", "Jacket", "Juice", "Jungle",
        "Kite", "Koala", "Key", "Kangaroo"
    )

    private val greekWords = listOf(
        "Αρκούδα", "Αγελάδα", "Αστέρι", "Αυγό",
        "Βάρκα", "Βάτραχος", "Βουνό", "Βιβλίο",
        "Γάτα", "Γέφυρα", "Γράμμα", "Γλάρος",
        "Δάσος", "Δελφίνι", "Δέντρο", "Δράκος",
        "Ελέφαντας", "Ελάφι", "Εικόνα", "Ελιά",
        "Ζέβρα", "Ζαχαρωτό", "Ζουζούνι", "Ζωγραφιά",
        "Ηλιοτρόπιο", "Ημέρα", "Ηχώ", "Ηφαίστειο",
        "Θάλασσα", "Θησαυρός", "Θάμνος", "Θρόνος"
    )

    fun wordsFor(language: WordLanguage): List<String> = when (language) {
        WordLanguage.ENGLISH -> englishWords
        WordLanguage.GREEK -> greekWords
    }

    fun pickTargetLetter(words: List<String>, random: Random = Random.Default): Char {
        val safeWords = words.filter { it.isNotBlank() }
        return safeWords.random(random).first().uppercaseChar()
    }

    fun buildOptions(
        targetLetter: Char,
        words: List<String>,
        optionCount: Int = 3,
        random: Random = Random.Default
    ): List<WordOption> {
        val correctWords = words.filter { startsWithLetter(it, targetLetter) }
        val wrongWords = words.filterNot { startsWithLetter(it, targetLetter) }
        val correctWord = correctWords.random(random)
        val wrongChoices = wrongWords.shuffled(random).take(optionCount - 1)
        val options = (wrongChoices + correctWord).shuffled(random)
        return options.map { word -> WordOption(word = word, isCorrect = startsWithLetter(word, targetLetter)) }
    }

    fun startsWithLetter(word: String, targetLetter: Char): Boolean {
        val first = word.firstOrNull() ?: return false
        return first.uppercaseChar() == targetLetter.uppercaseChar()
    }
}