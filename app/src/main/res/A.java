// Modelo Dental con animaciones
Text("Seleccione afecciones en el modelo dental:")
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .background(Color.White)
        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                if (afeccionSeleccionada != null) {
                    // Obtener el tamaño del contenedor y la imagen
                    val containerWidth = size.width
                    val containerHeight = size.height
                    val imageWidth = painterResource(id = R.drawable.modelo_dental).intrinsicSize.width
                    val imageHeight = painterResource(id = R.drawable.modelo_dental).intrinsicSize.height

                    // Calcular el factor de escalado
                    val scale = minOf(containerWidth / imageWidth, containerHeight / imageHeight)

                    // Calcular el desplazamiento (offset) para centrar la imagen
                    val offsetX = (containerWidth - imageWidth * scale) / 2
                    val offsetY = (containerHeight - imageHeight * scale) / 2

                    // Ajustar las coordenadas del clic para que coincidan con la imagen
                    val adjustedOffset = Offset(
                        ((offset.x - offsetX) / scale).coerceIn(0f, imageWidth),
                        ((offset.y - offsetY) / scale).coerceIn(0f, imageHeight)
                    )

                    puntosAfeccion = puntosAfeccion + Pair(adjustedOffset, afeccionSeleccionada!!)
                }
            }
        },
    contentAlignment = Alignment.Center
) {
    Image(
        painter = painterResource(id = R.drawable.modelo_dental),
        contentDescription = "Modelo dental",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )

    // Dibuja los puntos de afección con animaciones
    puntosAfeccion.forEachIndexed { index, (offset, color) ->
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            Box(
                modifier = Modifier
                    .offset {
                        // Ajustar el offset para que coincida con la imagen escalada
                        val containerWidth = size.width
                        val containerHeight = size.height
                        val imageWidth = painterResource(id = R.drawable.modelo_dental).intrinsicSize.width
                        val imageHeight = painterResource(id = R.drawable.modelo_dental).intrinsicSize.height
                        val scale = minOf(containerWidth / imageWidth, containerHeight / imageHeight)
                        val offsetX = (containerWidth - imageWidth * scale) / 2
                        val offsetY = (containerHeight - imageHeight * scale) / 2

                        IntOffset(
                            (offset.x * scale + offsetX).toInt(),
                            (offset.y * scale + offsetY).toInt()
                        )
                    }
                    .size(8.dp)
                    .background(color, CircleShape)
                    .clickable {
                        // Eliminar el punto al hacer clic en él
                        puntosAfeccion = puntosAfeccion.toMutableList().apply { removeAt(index) }
                    }
            )
        }
    }
}