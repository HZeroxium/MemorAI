package com.example.memorai.utils

import ja.burhanrashid52.photoeditor.shape.ShapeType
import ja.burhanrashid52.photoeditor.shape.ShapeType.Brush

// Wrapper để Java có thể sử dụng ShapeType
class ShapeTypeWrapper {

    companion object {
        @JvmStatic
        fun brush(): ShapeType = Brush

        @JvmStatic
        fun oval(): ShapeType = ShapeType.Oval

        @JvmStatic
        fun rectangle(): ShapeType = ShapeType.Rectangle

        @JvmStatic
        fun line(): ShapeType = ShapeType.Line

        @JvmStatic
        fun arrow(): ShapeType = ShapeType.Arrow()
    }
}
