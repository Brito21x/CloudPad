package upca.example.cloudpad.models

import com.google.firebase.Timestamp

data class Note(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var content: String = "",
    var updatedAt: Timestamp? = null
)
