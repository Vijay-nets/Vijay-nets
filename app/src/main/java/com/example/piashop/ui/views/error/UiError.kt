package com.example.piashop.ui.views.error

import android.util.Patterns
import androidx.core.text.isDigitsOnly


// Error Handling
interface FieldStatus {
    val status : Boolean
}
interface FieldError : FieldStatus {
    val description : String
}

data class Error(val errorMessage: String) : FieldError {
    override val description: String = errorMessage
    override val status: Boolean = false
}

object Success : FieldStatus {
    override val status: Boolean = true
}

/**
 * Basic validation logic
 */
val validation : (String, String, Int) -> FieldStatus = { field, text, characterLimit ->
    with(text){
        when {
            isEmpty() -> Error("Input should not be empty")
            !isDigitsOnly() -> Error("Input should be number")
            characterLimit == -1 -> Success
            length == characterLimit -> Success
            else -> Error("Input $field should be $characterLimit digits")
        }
    }
}

/**
 * Url validation logic
 */
val urlValidation : (String, String) -> FieldStatus = { field, text ->
    with(text){
        when {
            isEmpty() -> Error("Input should not be empty")
            Patterns.WEB_URL.matcher(text).matches() -> Success
            else -> Error("Input $field should be url")
        }
    }
}

// end region
