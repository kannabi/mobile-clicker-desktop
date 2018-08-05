package com.awsm_guys.mobile_clicker.utils

import com.awsm_guys.mobile_clicker.presentation.poko.Page
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiConsumer
import io.reactivex.schedulers.Schedulers
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO


fun convertPdfToImages(file: File): List<Page> =
    PDDocument.load(file).use {
        val renderer = PDFRenderer(it)

        return@use it.pages
                .mapIndexed { i, _ ->
                    Page(
                        imgToBase64String(renderer.renderImageWithDPI(i, 200f)),
                        imgToBase64String(renderer.renderImageWithDPI(i, 50f)),
                        i
                    )
                }
    }

fun imgToBase64String(img: RenderedImage) =
        ByteArrayOutputStream().use { os ->
            ImageIO.write(img, "png", os)
            return@use Base64.getEncoder().encodeToString(os.toByteArray())
        }!!

fun makeSingle(compositeDisposable: CompositeDisposable, body: (() -> Unit)) {
    compositeDisposable.add(
            Single.fromCallable(body).subscribeOn(Schedulers.io()).subscribe()
    )
}

fun <T> makeSingle(compositeDisposable: CompositeDisposable, body: (() -> Unit),
                   onCallback: BiConsumer<in Any, in Throwable>) {
    compositeDisposable.add(
            Single.fromCallable(body).subscribeOn(Schedulers.io()).subscribe(onCallback)
    )
}