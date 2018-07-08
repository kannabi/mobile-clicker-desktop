package com.awsm_guys.mobile_clicker.utils

import com.awsm_guys.mobile_clicker.presentation.poko.Page
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO


fun convertPdfToImages(file: File): List<Page> {
    val document = PDDocument.load(file)
    val renderer = PDFRenderer(document)

    return document.pages
            .mapIndexed { i, _ -> Page(imgToBase64String(renderer.renderImage(i)), i) }
}

fun imgToBase64String(img: RenderedImage) =
        ByteArrayOutputStream().use { os ->
            ImageIO.write(img, "png", os)
            return@use Base64.getEncoder().encodeToString(os.toByteArray())
        }!!