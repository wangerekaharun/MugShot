package com.wangerekaharun.mugshot.model

object RejectionMessage {

    enum class Category {
        NO_FACE,
        FACE_TOO_FAR,
        FACE_TOO_CLOSE,
        NOT_CENTERED,
        HEAD_TURNED,
        HEAD_TILTED,
        HEAD_PITCHED,
        NOT_SMILING,
        ALL_GOOD,
    }

    private val messages = mapOf(
        Category.NO_FACE to listOf(
            "Where did you go?",
            "I need a face to work with!",
            "Step into the frame!",
        ),
        Category.FACE_TOO_FAR to listOf(
            "Are you hiding back there?",
            "Come closer, I won't judge!",
            "Step into the spotlight!",
        ),
        Category.FACE_TOO_CLOSE to listOf(
            "Whoa, too close!",
            "Back up a bit!",
            "I can count your pores from here!",
        ),
        Category.NOT_CENTERED to listOf(
            "Move into the frame!",
            "A little to the left... or right!",
            "Center stage, please!",
        ),
        Category.HEAD_TURNED to listOf(
            "Turn and face me!",
            "I can see your ear but not your best angle",
            "Face me, I don't bite!",
        ),
        Category.HEAD_TILTED to listOf(
            "Gravity works -- use it!",
            "Level up -- literally!",
            "Straighten up!",
        ),
        Category.HEAD_PITCHED to listOf(
            "Chin up! (or down)",
            "Look straight ahead!",
            "Eyes on the prize!",
        ),
        Category.NOT_SMILING to listOf(
            "This is a mugshot, not a mug frown!",
            "Think happy thoughts!",
            "Say cheese!",
            "Come on, give me a smile!",
        ),
        Category.ALL_GOOD to listOf(
            "Perfect! Hold it...",
            "Looking great! Don't move...",
            "Yes! Stay right there...",
        ),
    )

    fun getActiveMessage(quality: FaceQuality?): Pair<Category, String> {
        if (quality == null) {
            return Category.NO_FACE to messages[Category.NO_FACE]!!.random()
        }

        val category = when {
            !quality.isCentered -> Category.NOT_CENTERED
            !quality.isFaceSizeGood && quality.faceSizeRatio < FaceQuality.MIN_FACE_SIZE -> Category.FACE_TOO_FAR
            !quality.isFaceSizeGood && quality.faceSizeRatio > FaceQuality.MAX_FACE_SIZE -> Category.FACE_TOO_CLOSE
            !quality.isYawGood -> Category.HEAD_TURNED
            !quality.isPitchGood -> Category.HEAD_PITCHED
            !quality.isTiltGood -> Category.HEAD_TILTED
            !quality.isSmiling -> Category.NOT_SMILING
            else -> Category.ALL_GOOD
        }

        return category to messages[category]!!.random()
    }
}
